package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.CardScore
import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.BattleNoblePhantasmEnum
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.ScriptExitException
import mu.KotlinLogging

private const val dummyNormalAffinityChar = 'X'
private const val cardPriorityErrorString = "Battle_CardPriority Error at '"

const val cardPriorityStageSeparator = "\n"

fun getCardScores(Priority: String): List<CardScore> {
    val scores = Priority
        .splitToSequence(',')
        .map { it.trim().toUpperCase() }
        .map {
            when (it.length) {
                1 -> "$dummyNormalAffinityChar$it"
                2 -> it
                else -> throw ScriptExitException("$cardPriorityErrorString${it}': Invalid card length.")
            }
        }
        .map {
            val cardType = when (it[1]) {
                'B' -> CardTypeEnum.Buster
                'A' -> CardTypeEnum.Arts
                'Q' -> CardTypeEnum.Quick
                else -> throw ScriptExitException("$cardPriorityErrorString${it[1]}': Only 'B', 'A' and 'Q' are valid card types.")
            }

            val cardAffinity = when (it[0]) {
                'W' -> CardAffinityEnum.Weak
                'R' -> CardAffinityEnum.Resist
                dummyNormalAffinityChar -> CardAffinityEnum.Normal
                else -> throw ScriptExitException("$cardPriorityErrorString${it[0]}': Only 'W', and 'R' are valid card affinities.")
            }

            CardScore(
                cardType,
                cardAffinity
            )
        }
        .toList()

    if (scores.size != 9) {
        throw ScriptExitException("$cardPriorityErrorString': Expected 9 cards, but ${scores.size} found.")
    }

    return scores
}

private val logger = KotlinLogging.logger {}

class Card(fgAutomataApi: IFGAutomataApi) : IFGAutomataApi by fgAutomataApi {
    private lateinit var autoSkill: AutoSkill
    private lateinit var battle: Battle

    private lateinit var cardPriorityArray: List<List<CardScore>>

    private val commandCards = mutableMapOf<CardScore, MutableList<Int>>()
    private var remainingCards = mutableSetOf<Int>()

    val alreadyClicked get() = 5 - remainingCards.size

    fun init(AutoSkillModule: AutoSkill, BattleModule: Battle) {
        autoSkill = AutoSkillModule
        battle = BattleModule

        resetCommandCards()
        initCardPriorityArray()
    }

    private fun initCardPriorityArray() {
        val priority = prefs.selectedAutoSkillConfig.cardPriority

        if (priority.length == 3) {
            initCardPriorityArraySimple(priority)
        } else initCardPriorityArrayDetailed(priority)
    }

    private fun initCardPriorityArraySimple(Priority: String) {
        val detailedPriority = Priority
            .map { "W$it, $dummyNormalAffinityChar$it, R$it" }
            .joinToString()

        initCardPriorityArrayDetailed(detailedPriority)
    }

    private fun initCardPriorityArrayDetailed(Priority: String) {
        cardPriorityArray = Priority
            .split(cardPriorityStageSeparator)
            .map {
                getCardScores(it)
                    // Give minimum priority to unknown
                    .plus(CardScore(CardTypeEnum.Unknown, CardAffinityEnum.Normal))
            }
    }

    private fun getCardAffinity(Region: Region): CardAffinityEnum {
        if (Region.exists(images.weak)) {
            return CardAffinityEnum.Weak
        }

        if (Region.exists(images.resist)) {
            return CardAffinityEnum.Resist
        }

        return CardAffinityEnum.Normal
    }

    private fun getCardType(Region: Region): CardTypeEnum {
        val stunRegion = Region.copy(
            Y = 930,
            Width = 248,
            Height = 188
        )

        if (stunRegion.exists(images.stun)) {
            return CardTypeEnum.Unknown
        }

        if (Region.exists(images.buster)) {
            return CardTypeEnum.Buster
        }

        if (Region.exists(images.art)) {
            return CardTypeEnum.Arts
        }

        if (Region.exists(images.quick)) {
            return CardTypeEnum.Quick
        }

        val msg = "Failed to determine Card type $Region"
        toast(msg)
        logger.debug(msg)

        return CardTypeEnum.Unknown
    }

    private var commandCardGroups: List<List<Int>> = emptyList()
    private var commandCardGroupedWithNp: List<List<Int>> = emptyList()
    var firstNp = -1

    fun readCommandCards() {
        commandCards.clear()

        screenshotManager.useSameSnapIn {
            for (cardSlot in game.battleCardAffinityRegionArray.indices) {
                val type = getCardType(game.battleCardTypeRegionArray[cardSlot])
                val affinity =
                    if (type == CardTypeEnum.Unknown)
                        CardAffinityEnum.Normal // Couldn't detect card type, so don't care about affinity
                    else getCardAffinity(game.battleCardAffinityRegionArray[cardSlot])

                val score = CardScore(
                    type,
                    affinity
                )

                if (!commandCards.containsKey(score)) {
                    commandCards[score] = mutableListOf()
                }

                commandCards[score]?.add(cardSlot)
            }

            commandCardGroups = groupByFaceCard()
            commandCardGroupedWithNp = groupNpsWithFaceCards(commandCardGroups)
        }
    }

    val canClickNpCards: Boolean
        get() {
            val weCanSpam = prefs.castNoblePhantasm == BattleNoblePhantasmEnum.Spam
            val weAreInDanger = prefs.castNoblePhantasm == BattleNoblePhantasmEnum.Danger
                    && battle.hasChosenTarget

            return (weCanSpam || weAreInDanger) && autoSkill.isFinished
        }

    fun clickNpCards() {
        for (npCard in game.battleNpCardClickArray) {
            npCard.click()
        }
    }

    fun clickCommandCards(Clicks: Int) {
        if (Clicks <= 0) {
            return
        }

        val cardPriorityIndex = battle.currentStage.coerceIn(cardPriorityArray.indices)
        var clicksLeft = Clicks

        fun List<Int>.clickAll(): List<Int> {
            this.forEach { game.battleCommandCardClickArray[it].click() }
            remainingCards.removeAll(this)
            clicksLeft -= this.size

            return this
        }

        fun clickCardsOrderedByPriority(filter: (Int) -> Boolean = { true }) =
            cardPriorityArray[cardPriorityIndex]
                .mapNotNull { commandCards[it] }
                .flatten()
                .filter { it in remainingCards && filter(it) }
                .take(clicksLeft)
                .clickAll()

        when (prefs.braveChains) {
            BraveChainEnum.AfterNP -> {
                if (firstNp in commandCardGroupedWithNp.indices) {
                    clickCardsOrderedByPriority {
                        it in commandCardGroupedWithNp[firstNp]
                    }
                }
            }
            BraveChainEnum.Avoid -> {
                if (commandCardGroups.size > 1
                    && remainingCards.isNotEmpty()
                    && clicksLeft > 1
                ) {
                    var lastGroup = if (firstNp in commandCardGroupedWithNp.indices) {
                        commandCardGroupedWithNp[firstNp]
                    } else {
                        cardPriorityArray[cardPriorityIndex]
                            .mapNotNull { commandCards[it] }
                            .flatten()
                            .filter { it in remainingCards }
                            .take(1)
                            .clickAll()
                            .map { m -> commandCardGroups.firstOrNull { m in it } }
                            .firstOrNull() ?: emptyList()
                    }

                    if (lastGroup.isNotEmpty()) {
                        while (clicksLeft > 0) {
                            val picked = cardPriorityArray[cardPriorityIndex]
                                .mapNotNull { commandCards[it] }
                                .flatten()
                                .filter { it in remainingCards && it !in lastGroup }
                                .take(1)
                                .clickAll()

                            if (picked.isEmpty()) {
                                break
                            } else {
                                lastGroup = commandCardGroups.first { picked[0] in it }
                            }
                        }
                    }
                }
            }
        }

        firstNp = -1

        clickCardsOrderedByPriority()
    }

    fun resetCommandCards() {
        commandCards.clear()

        remainingCards = game.battleCardAffinityRegionArray.indices.toMutableSet()
    }
}