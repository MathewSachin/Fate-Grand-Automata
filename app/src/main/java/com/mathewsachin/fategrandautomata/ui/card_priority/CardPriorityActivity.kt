package com.mathewsachin.fategrandautomata.ui.card_priority

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.defaultCardPriority
import com.mathewsachin.fategrandautomata.scripts.CardPriority
import com.mathewsachin.fategrandautomata.scripts.CardPriorityPerWave
import com.mathewsachin.fategrandautomata.scripts.CardScore
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.AutoSkillItemActivity
import com.mathewsachin.fategrandautomata.util.appComponent
import kotlinx.android.synthetic.main.card_priority.*
import javax.inject.Inject

class CardPriorityActivity : AppCompatActivity() {
    private lateinit var cardScores: MutableList<MutableList<CardScore>>
    private lateinit var autoSkillPref: IAutoSkillPreferences

    @Inject
    lateinit var preferences: IPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.card_priority)

        appComponent.inject(this)

        val autoSkillKey = intent.getStringExtra(AutoSkillItemActivity::autoSkillItemKey.name)
            ?: throw IllegalArgumentException("Missing AutoSkill item key in intent")
        autoSkillPref = preferences.forAutoSkillConfig(autoSkillKey)

        var cardPriority = autoSkillPref.cardPriority

        // Handle simple mode and empty string
        if (cardPriority.length == 3 || cardPriority.isBlank()) {
            cardPriority =
                defaultCardPriority
        }

        cardScores = CardPriorityPerWave.of(cardPriority)
            .map { it.toMutableList() }
            .toMutableList()

        val adapter = CardPriorityListAdapter(cardScores)

        val recyclerView = card_priority_list
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        card_priority_add_btn.setOnClickListener {
            cardScores.add(mutableListOf<CardScore>().apply {
                addAll(cardScores[0])
            })

            adapter.notifyItemInserted(cardScores.lastIndex)
        }

        card_priority_rm_btn.setOnClickListener {
            if (cardScores.size > 1) {
                cardScores.removeAt(cardScores.lastIndex)

                adapter.notifyItemRemoved(cardScores.lastIndex + 1)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        val value = CardPriorityPerWave.from(
            cardScores.map {
                CardPriority.from(it)
            }
        ).toString()

        autoSkillPref.cardPriority = value
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.card_priority_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_card_priority_info -> {
                AlertDialog.Builder(this)
                    .setMessage("W: Weak (Effective)\nR: Resistive\n\nB: Buster\nA: Arts\nQ: Quick")
                    .setTitle("Info")
                    .setPositiveButton(android.R.string.yes, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
