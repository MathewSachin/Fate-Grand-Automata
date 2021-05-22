package com.mathewsachin.fategrandautomata.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.launch
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mathewsachin.fategrandautomata.BuildConfig
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerService
import com.mathewsachin.fategrandautomata.accessibility.TapperService
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.wantsMediaProjectionToken
import com.mathewsachin.fategrandautomata.ui.*
import com.mathewsachin.fategrandautomata.ui.prefs.Preference
import com.mathewsachin.fategrandautomata.util.StorageProvider
import com.mathewsachin.fategrandautomata.util.nav
import com.mathewsachin.fategrandautomata.util.registerPersistableDirPicker
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import timber.log.info
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {
    val vm: MainSettingsViewModel by activityViewModels()

    @Inject
    lateinit var storageProvider: StorageProvider

    @Inject
    lateinit var prefs: IPreferences

    private val pickDir = registerPersistableDirPicker {
        storageProvider.setRoot(it)

        serviceToggleBtnOnClick()
    }

    fun goToBattleConfigList() {
        val action = MainFragmentDirections
            .actionMainFragmentToBattleConfigListFragment()

        nav(action)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setContent {
                FgaScreen {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        item {
                            Heading(stringResource(R.string.app_name)) {
                                item {
                                    HeadingButton(
                                        text = "Build: ${BuildConfig.VERSION_CODE}",
                                        onClick = {
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(getString(R.string.link_releases))
                                            )

                                            startActivity(intent)
                                        }
                                    )
                                }

                                item {
                                    HeadingButton(
                                        text = stringResource(R.string.p_nav_troubleshoot),
                                        onClick = {
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(getString(R.string.link_troubleshoot))
                                            )

                                            startActivity(intent)
                                        }
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Preference(
                                        title = stringResource(R.string.p_battle_config),
                                        summary = stringResource(R.string.p_battle_config_summary),
                                        icon = icon(R.drawable.ic_formation),
                                        onClick = {
                                            if (vm.ensureRootDir(pickDir, requireContext())) {
                                                goToBattleConfigList()
                                            }
                                        }
                                    )

                                    Divider()

                                    Preference(
                                        title = stringResource(R.string.p_more_options),
                                        icon = icon(R.drawable.ic_dots_horizontal),
                                        onClick = {
                                            val action = MainFragmentDirections
                                                .actionMainFragmentToMoreSettingsFragment()

                                            nav(action)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    val serviceStarted by vm.serviceStarted

                    val backgroundColor by animateColorAsState(
                        if (serviceStarted)
                            MaterialTheme.colors.error
                        else MaterialTheme.colors.secondary
                    )

                    val foregroundColor =
                        if (serviceStarted)
                            MaterialTheme.colors.onError
                        else MaterialTheme.colors.onSecondary

                    ExtendedFloatingActionButton(
                        text = {
                            Text(
                                stringResource(if (serviceStarted) R.string.stop_service else R.string.start_service),
                                color = foregroundColor
                            )
                        },
                        onClick = { serviceToggleBtnOnClick() },
                        icon = {
                            Icon(
                                painterResource(if (serviceStarted) R.drawable.ic_close else R.drawable.ic_launch),
                                contentDescription = "Toggle service",
                                tint = foregroundColor
                            )
                        },
                        backgroundColor = backgroundColor,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    )
                }
            }
        }

    val startMediaProjection = registerForActivityResult(StartMediaProjection()) { intent ->
        if (intent == null) {
            Timber.info { "MediaProjection cancelled by user" }
            ScriptRunnerService.stopService(requireContext())
        } else {
            ScriptRunnerService.mediaProjectionToken = intent
        }
    }

    private fun checkAccessibilityService(): Boolean {
        if (TapperService.isRunning)
            return true

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.accessibility_disabled_title)
            .setMessage(R.string.accessibility_disabled_message)
            .setPositiveButton(R.string.accessibility_disabled_go_to_settings) { _, _ ->
                // Open Accessibility Settings
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                toggling = true
                startActivity(intent)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()

        return false
    }

    private fun checkCanUseOverlays(): Boolean {
        val context = requireContext()

        if (!Settings.canDrawOverlays(context)) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.draw_overlay_disabled_title)
                .setMessage(R.string.draw_overlay_disabled_message)
                .setPositiveButton(R.string.draw_overlay_disabled_go_to_settings) { _, _ ->
                    // Open overlay settings
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    toggling = true
                    startActivity(intent)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            return false
        }

        return true
    }

    private var toggling = false

    private fun serviceToggleBtnOnClick() {
        toggling = false

        if (!checkCanUseOverlays()
            || !checkAccessibilityService()
        )
            return

        if (!vm.ensureRootDir(pickDir, requireContext())) {
            return
        }

        if (ScriptRunnerService.serviceStarted.value) {
            ScriptRunnerService.stopService(requireContext())
        } else {
            ScriptRunnerService.startService(requireContext())

            if (prefs.wantsMediaProjectionToken) {
                if (ScriptRunnerService.mediaProjectionToken == null) {
                    startMediaProjection.launch()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (toggling || (vm.autoStartService && !ScriptRunnerService.serviceStarted.value)) {
            serviceToggleBtnOnClick()
        }
    }
}
