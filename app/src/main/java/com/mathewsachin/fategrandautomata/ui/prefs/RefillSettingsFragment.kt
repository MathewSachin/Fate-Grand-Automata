package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.util.makeNumeric
import dagger.hilt.android.AndroidEntryPoint
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

@AndroidEntryPoint
class RefillSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.refill_preferences, rootKey)

        findPreference<EditTextPreference>(getString(prefKeys.pref_refill_repetitions))?.makeNumeric()
        findPreference<EditTextPreference>(getString(R.string.pref_limit_runs))?.makeNumeric()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vm: MainSettingsViewModel by activityViewModels()

        findPreference<EditTextPreference>(getString(R.string.pref_refill_repetitions))?.let {
            vm.refillRepetitions.observe(viewLifecycleOwner) { repetitions ->
                it.text = repetitions.toString()
            }
        }

        findPreference<MultiSelectListPreference>(getString(R.string.pref_refill_resource))?.let {
            vm.refillResources.observe(viewLifecycleOwner) { refillResourcesMsg ->
                it.summary = refillResourcesMsg
            }
        }

        findPreference<ListPreference>(getString(R.string.pref_limit_mat_by))?.let {
            vm.limitedMat.observe(viewLifecycleOwner) { mat ->
                it.icon = ContextCompat.getDrawable(requireContext(), mat.drawable)
            }
        }
    }

    private val MaterialEnum.drawable
        get() = when (this) {
            MaterialEnum.Claw -> R.drawable.mat_claw
            MaterialEnum.Dust -> R.drawable.mat_dust
            MaterialEnum.Fang -> R.drawable.mat_fang
            MaterialEnum.Heart -> R.drawable.mat_heart
            MaterialEnum.Seed -> R.drawable.mat_seed
        }
}
