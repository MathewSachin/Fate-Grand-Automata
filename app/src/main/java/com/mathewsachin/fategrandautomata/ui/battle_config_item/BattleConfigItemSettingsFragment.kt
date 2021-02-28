package com.mathewsachin.fategrandautomata.ui.battle_config_item

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.pref_support.PreferredSupportViewModel
import com.mathewsachin.fategrandautomata.ui.prefs.*
import com.mathewsachin.fategrandautomata.util.nav
import com.mathewsachin.fategrandautomata.util.stringRes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.error
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BattleConfigItemSettingsFragment : Fragment() {
    @Inject
    lateinit var preferences: IPreferences

    @Inject
    lateinit var prefsCore: PrefsCore

    val vm: BattleConfigItemViewModel by viewModels()
    val supportViewModel: PreferredSupportViewModel by activityViewModels()

    val args: BattleConfigItemSettingsFragmentArgs by navArgs()

    val battleConfigExport = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        if (uri != null) {
            try {
                val values = preferences.forBattleConfig(args.key).export()
                val gson = Gson()
                val json = gson.toJson(values)

                requireContext().contentResolver.openOutputStream(uri)?.use { outStream ->
                    outStream.writer().use { it.write(json) }
                }
            } catch (e: Exception) {
                Timber.error(e) { "Failed to export" }

                val msg = getString(R.string.battle_config_item_export_failed)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private lateinit var battleConfig: IBattleConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        battleConfig = preferences.forBattleConfig(args.key)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            val config = prefsCore.forBattleConfig(args.key)

            setContent {
                FgaTheme {
                    LazyColumn {
                        item {
                            config.name.EditTextPreference(
                                title = stringResource(R.string.p_battle_config_name)
                            )

                            Divider()
                        }

                        item {
                            val cmd by config.skillCommand.collect()
                            val cmdDialog = editTextDialog(
                                title = stringResource(R.string.p_battle_config_cmd),
                                value = cmd,
                                valueChange = { config.skillCommand.set(it) }
                            )

                            Preference(
                                title = stringResource(R.string.p_battle_config_cmd),
                                summary = cmd,
                                onClick = {
                                    val action = BattleConfigItemSettingsFragmentDirections
                                        .actionBattleConfigItemSettingsFragmentToBattleConfigMakerActivity(args.key)

                                    nav(action)
                                }
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_terminal),
                                    contentDescription = "Show Textbox for editing Skill command",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable { cmdDialog.show() }
                                )
                            }

                            Divider()
                        }

                        item {
                            config.notes.EditTextPreference(
                                title = stringResource(R.string.p_battle_config_notes)
                            )

                            Divider()
                        }

                        item {
                            val cardPriority by config.cardPriority.collect()

                            Preference(
                                title = stringResource(R.string.p_battle_config_card_priority),
                                summary = cardPriority,
                                onClick = {
                                    val action = BattleConfigItemSettingsFragmentDirections
                                        .actionBattleConfigItemSettingsFragmentToCardPriorityFragment(args.key)

                                    nav(action)
                                }
                            )

                            Divider()
                        }

                        item {
                            Row {
                                Box(modifier = Modifier.weight(1f)) {
                                    config.party.ListPreference(
                                        title = stringResource(R.string.p_battle_config_party),
                                        entries = (-1..9).associateWith { it.partyString }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    config.materials.MultiSelectListPreference(
                                        title = stringResource(R.string.p_mats),
                                        entries = MaterialEnum.values()
                                            .associateWith { getString(it.stringRes) }
                                    )
                                }
                            }

                            Divider()
                        }

                        item {
                            Preference(
                                title = stringResource(R.string.p_spam_spam),
                                onClick = {
                                    val action = BattleConfigItemSettingsFragmentDirections
                                        .actionBattleConfigItemSettingsFragmentToSpamSettingsFragment(args.key)

                                    nav(action)
                                }
                            )

                            Divider()
                        }

                        item {
                            val preferredSummary by vm.preferredMessage.collectAsState("")

                            SupportGroup(
                                config = config,
                                goToPreferred = {
                                    val action = BattleConfigItemSettingsFragmentDirections
                                        .actionBattleConfigItemSettingsFragmentToPreferredSupportSettingsFragment(args.key)

                                    nav(action)
                                },
                                preferredSummary = preferredSummary,
                                friendEntries = supportViewModel.friends
                            )

                            Divider()
                        }

                        item {
                            ShuffleCardsGroup(config)
                        }
                    }
                }
            }
        }

    val Int.partyString get() = when (this) {
        -1 -> getString(R.string.p_not_set)
        else -> getString(R.string.p_party_number, this + 1)
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            if (supportViewModel.shouldExtractSupportImages) {
                performSupportImageExtraction()
            } else supportViewModel.refresh(requireContext())
        }
    }

    private suspend fun performSupportImageExtraction() {
        val msg = try {
            supportViewModel.extract(requireContext())

            getString(R.string.support_imgs_extracted)
        } catch (e: Exception) {
            getString(R.string.support_imgs_extract_failed).also { msg ->
                Timber.error(e) { msg }
            }
        }

        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.battle_config_item_menu, menu)
        inflater.inflate(R.menu.support_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_support_extract_defaults -> {
                lifecycleScope.launch {
                    performSupportImageExtraction()
                }
                true
            }
            R.id.action_battle_config_delete -> {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.battle_config_item_delete_confirm_message)
                    .setTitle(R.string.battle_config_item_delete_confirm_title)
                    .setPositiveButton(R.string.battle_config_item_delete_confirm_ok) { _, _ -> deleteItem(args.key) }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                true
            }
            R.id.action_battle_config_export -> {
                battleConfigExport.launch("${battleConfig.name}.fga")
                true
            }
            R.id.action_battle_config_copy -> {
                val guid = UUID.randomUUID().toString()
                preferences.addBattleConfig(guid)
                val newConfig = preferences.forBattleConfig(guid)

                val map = battleConfig.export()
                newConfig.import(map)
                newConfig.name = getString(R.string.battle_config_item_copy_name, newConfig.name)

                val action = BattleConfigItemSettingsFragmentDirections
                    .actionBattleConfigItemSettingsFragmentSelf(guid)

                nav(action)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun deleteItem(battleConfigKey: String) {
        preferences.removeBattleConfig(battleConfigKey)

        findNavController().popBackStack()
    }
}