package com.mathewsachin.fategrandautomata.ui.card_priority

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.prefs.defaultCardPriority
import com.mathewsachin.fategrandautomata.scripts.models.CardPriority
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave
import javax.inject.Inject

class CardPriorityViewModel @Inject constructor(
    val prefsCore: PrefsCore
) : ViewModel() {
    var key: String = ""

    val cardPriorityItems: MutableList<CardPriorityListItem> by lazy {
        var cardPriority = autoSkillPref.cardPriority.get()

        // Handle simple mode and empty string
        if (cardPriority.length == 3 || cardPriority.isBlank()) {
            cardPriority =
                defaultCardPriority
        }

        CardPriorityPerWave.of(cardPriority)
            .map { it.toMutableList() }
            .map { CardPriorityListItem(it, false) }
            .toMutableList()
    }

    private val autoSkillPref by lazy { prefsCore.forAutoSkillConfig(key) }

    val experimental by lazy {
        autoSkillPref.experimental
            .asFlow()
            .asLiveData()
    }

    fun setExperimental(value: Boolean) = autoSkillPref.experimental.set(value)

    fun save() {
        val value = CardPriorityPerWave.from(
            cardPriorityItems.map {
                CardPriority.from(it.scores)
            }
        ).toString()

        autoSkillPref.cardPriority.set(value)
    }
}