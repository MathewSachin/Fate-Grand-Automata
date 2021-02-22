package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.ui.prefs.compose.FgaTheme
import com.mathewsachin.fategrandautomata.ui.prefs.compose.PreferenceGroup
import com.mathewsachin.fategrandautomata.ui.prefs.compose.SeekBarPreference
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FineTuneSettingsFragment : Fragment() {
    val vm: FineTuneSettingsViewModel by viewModels()

    @Inject
    lateinit var prefs: PrefsCore

    @Composable
    fun Pref<Int>.FineTuneSeekBar(
        @StringRes title: Int,
        @DrawableRes icon: Int,
        valueRange: IntRange = 0..100,
        valueRepresentation: (Int) -> String = { it.toString() }
    ) {
        SeekBarPreference(
            title = stringResource(title),
            summary = "Default: ${valueRepresentation(defaultValue)}",
            icon = vectorResource(icon),
            valueRange = valueRange,
            valueRepresentation = valueRepresentation,
            state = vm.getState(this)
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                FgaTheme {
                    Box {
                        ScrollableColumn(
                            modifier = Modifier.padding(bottom = 60.dp)
                        ) {
                            SupportGroup(prefs)
                            SimilarityGroup(prefs)
                            ClicksGroup(prefs)
                            SwipesGroup(prefs)
                            WaitGroup(prefs)
                            Spacer(Modifier.padding(30.dp))
                        }

                        ExtendedFloatingActionButton(
                            text = {
                                Text(
                                    stringResource(R.string.fine_tune_menu_reset_to_defaults),
                                    color = Color.White
                                )
                            },
                            onClick = { vm.resetAll() },
                            icon = {
                                Icon(
                                    vectorResource(R.drawable.ic_refresh),
                                    tint = Color.White
                                )
                            },
                            backgroundColor = colorResource(R.color.colorPrimary),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(32.dp)
                        )
                    }
                }
            }
        }

    @Composable
    fun SupportGroup(prefs: PrefsCore) {
        PreferenceGroup(title = stringResource(R.string.p_fine_tune_support)) {
            prefs.supportSwipesPerUpdate.FineTuneSeekBar(
                title = R.string.p_fine_tune_support_swipes_per_update,
                icon = R.drawable.ic_swipe,
                valueRange = 0..35
            )

            prefs.supportMaxUpdates.FineTuneSeekBar(
                title = R.string.p_fine_tune_support_max_updates,
                icon = R.drawable.ic_refresh,
                valueRange = 0..50
            )
        }
    }

    @Composable
    fun SimilarityGroup(prefs: PrefsCore) {
        PreferenceGroup(title = stringResource(R.string.p_fine_tune_similarity)) {
            prefs.minSimilarity.FineTuneSeekBar(
                title = R.string.p_fine_tune_min_similarity,
                icon = R.drawable.ic_image_search,
                valueRange = 50..100,
                valueRepresentation = { "$it%" }
            )

            prefs.mlbSimilarity.FineTuneSeekBar(
                title = R.string.p_fine_tune_mlb_similarity,
                icon = R.drawable.ic_star,
                valueRange = 50..100,
                valueRepresentation = { "$it%" }
            )

            prefs.stageCounterSimilarity.FineTuneSeekBar(
                title = R.string.p_fine_tune_stage_counter_similarity,
                icon = R.drawable.ic_counter,
                valueRange = 50..100,
                valueRepresentation = { "$it%" }
            )
        }
    }

    @Composable
    fun ClicksGroup(prefs: PrefsCore) {
        PreferenceGroup(title = stringResource(R.string.p_fine_tune_clicks)) {
            prefs.clickWaitTime.FineTuneSeekBar(
                title = R.string.p_fine_tune_wait_after_clicking,
                icon = R.drawable.ic_click,
                valueRange = 0..2000,
                valueRepresentation = { "${it}ms" }
            )

            prefs.clickDuration.FineTuneSeekBar(
                title = R.string.p_fine_tune_click_duration,
                icon = R.drawable.ic_click,
                valueRange = 1..200,
                valueRepresentation = { "${it}ms" }
            )

            prefs.clickDelay.FineTuneSeekBar(
                title = R.string.p_fine_tune_click_delay,
                icon = R.drawable.ic_click,
                valueRange = 0..50,
                valueRepresentation = { "${it}ms" }
            )
        }
    }

    @Composable
    fun SwipesGroup(prefs: PrefsCore) {
        PreferenceGroup(title = stringResource(R.string.p_fine_tune_swipes)) {
            prefs.swipeWaitTime.FineTuneSeekBar(
                title = R.string.p_fine_tune_wait_after_swiping,
                icon = R.drawable.ic_swipe,
                valueRange = 50..3000,
                valueRepresentation = { "${it}ms" }
            )

            prefs.swipeDuration.FineTuneSeekBar(
                title = R.string.p_fine_tune_swipe_duration,
                icon = R.drawable.ic_swipe,
                valueRange = 50..1000,
                valueRepresentation = { "${it}ms" }
            )

            prefs.swipeMultiplier.FineTuneSeekBar(
                title = R.string.p_fine_tune_swipe_multiplier,
                icon = R.drawable.ic_swipe,
                valueRange = 50..200,
                valueRepresentation = { "${it}%" }
            )
        }
    }

    @Composable
    fun WaitGroup(prefs: PrefsCore) {
        PreferenceGroup(title = stringResource(R.string.p_fine_tune_wait)) {
            prefs.skillDelay.FineTuneSeekBar(
                title = R.string.p_fine_tune_skill_delay,
                icon = R.drawable.ic_wand,
                valueRange = 0..2000,
                valueRepresentation = { "${it}ms" }
            )

            prefs.waitBeforeTurn.FineTuneSeekBar(
                title = R.string.p_fine_tune_wait_before_turn,
                icon = R.drawable.ic_time,
                valueRange = 0..2000,
                valueRepresentation = { "${it}ms" }
            )

            prefs.waitBeforeCards.FineTuneSeekBar(
                title = R.string.p_fine_tune_wait_before_cards,
                icon = R.drawable.ic_card,
                valueRange = 0..6000,
                valueRepresentation = { "${it}ms" }
            )

            prefs.waitMultiplier.FineTuneSeekBar(
                title = R.string.p_fine_tune_wait_multiplier,
                icon = R.drawable.ic_time,
                valueRange = 50..200,
                valueRepresentation = { "${it}%" }
            )
        }
    }
}