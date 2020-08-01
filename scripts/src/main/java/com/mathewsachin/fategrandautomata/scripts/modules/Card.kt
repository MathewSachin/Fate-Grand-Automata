package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.*
import com.mathewsachin.fategrandautomata.scripts.enums.BattleNoblePhantasmEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class Card(fgAutomataApi: IFGAutomataApi) : IFGAutomataApi by fgAutomataApi {
    private lateinit var autoSkill: AutoSkill
    private lateinit var battle: Battle

    private lateinit var cardPriority: CardPriorityPerWave

    private val commandCards = mutableMapOf<CardScore, MutableList<CommandCard>>()
    private val remainingCards = mutableSetOf<CommandCard>()

    fun init(AutoSkillModule: AutoSkill, BattleModule: Battle) {
        autoSkill = AutoSkillModule
        battle = BattleModule

        initCardPriorityArray()
        resetCommandCards()
    }

    private fun initCardPriorityArray() {
        val priority = prefs.selectedAutoSkillConfig.cardPriority

        cardPriority = CardPriorityPerWave.of(priority)
    }

    private fun getCardAffinity(commandCard: CommandCard): CardAffinityEnum {
        val region = commandCard.affinityRegion

        if (region.exists(images.weak)) {
            return CardAffinityEnum.Weak
        }

        if (region.exists(images.resist)) {
            return CardAffinityEnum.Resist
        }

        return CardAffinityEnum.Normal
    }

    private fun getCardType(commandCard: CommandCard): CardTypeEnum {
        val region = commandCard.typeRegion

        val stunRegion = region.copy(
            Y = 930,
            Width = 248,
            Height = 188
        )

        if (stunRegion.exists(images.stun)) {
            return CardTypeEnum.Unknown
        }

        if (region.exists(images.buster)) {
            return CardTypeEnum.Buster
        }

        if (region.exists(images.art)) {
            return CardTypeEnum.Arts
        }

        if (region.exists(images.quick)) {
            return CardTypeEnum.Quick
        }

        val msg = "Failed to determine Card type $region"
        toast(msg)
        logger.debug(msg)

        return CardTypeEnum.Unknown
    }

    fun readCommandCards() {
        commandCards.clear()

        screenshotManager.useSameSnapIn {
            for (cardSlot in CommandCard.list) {
                val type = getCardType(cardSlot)
                val affinity =
                    if (type == CardTypeEnum.Unknown)
                        CardAffinityEnum.Normal // Couldn't detect card type, so don't care about affinity
                    else getCardAffinity(cardSlot)

                val score = CardScore(
                    type,
                    affinity
                )

                if (!commandCards.containsKey(score)) {
                    commandCards[score] = mutableListOf()
                }

                commandCards[score]?.add(cardSlot)
            }
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
        for (npCard in NoblePhantasm.list) {
            npCard.clickLocation.click()
        }
    }

    fun clickCommandCards(Clicks: Int) {
        cardPriority.atWave(battle.currentStage)
            .mapNotNull { commandCards[it] }
            .flatten()
            .filter { it in remainingCards }
            .take(Clicks)
            .forEach {
                it.clickLocation.click()

                remainingCards.remove(it)
            }
    }

    fun resetCommandCards() {
        commandCards.clear()

        remainingCards.addAll(CommandCard.list)
    }
}