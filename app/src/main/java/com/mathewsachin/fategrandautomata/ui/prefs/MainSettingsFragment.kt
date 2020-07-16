package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences

class MainSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey)

        findPreference<Preference>(getString(R.string.pref_nav_refill))?.let {
            it.fragment = RefillSettingsFragment::class.java.name
        }

        findPreference<Preference>(getString(R.string.pref_nav_auto_skill))?.let {
            it.fragment = AutoSkillSettingsFragment::class.java.name
        }

        findPreference<Preference>(getString(R.string.pref_nav_more))?.let {
            it.fragment = MoreSettingsFragment::class.java.name
        }
    }

    override fun onResume() {
        super.onResume()

        findPreference<Preference>(getString(R.string.pref_nav_refill))?.let {
            val prefs = Preferences.Refill
            it.summary = when (prefs.enabled) {
                true -> "${prefs.resource} x${prefs.repetitions}"
                false -> "OFF"
            }
        }
    }
}
