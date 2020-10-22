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
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.util.initWith
import com.mathewsachin.fategrandautomata.util.makeNumeric
import com.mathewsachin.fategrandautomata.util.stringRes
import dagger.hilt.android.AndroidEntryPoint
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

@AndroidEntryPoint
class RefillSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.refill_preferences, rootKey)

        findPreference<EditTextPreference>(getString(prefKeys.pref_refill_repetitions))?.makeNumeric()
        findPreference<EditTextPreference>(getString(R.string.pref_limit_runs))?.makeNumeric()
        findPreference<EditTextPreference>(getString(R.string.pref_limit_mats))?.makeNumeric()

        findPreference<MultiSelectListPreference>(getString(R.string.pref_refill_resource))
            ?.initWith<RefillResourceEnum> { it.stringRes }

        findPreference<ListPreference>(getString(R.string.pref_limit_mat_by))
            ?.initWith<MaterialEnum> { it.stringRes }
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
            MaterialEnum.Proof -> R.drawable.mat_proof
            MaterialEnum.Bone -> R.drawable.mat_bone
            MaterialEnum.Fang -> R.drawable.mat_fang
            MaterialEnum.Dust -> R.drawable.mat_dust
            MaterialEnum.Chain -> R.drawable.mat_chain
            MaterialEnum.Fluid -> R.drawable.mat_fluid
            MaterialEnum.Seed -> R.drawable.mat_seed
            MaterialEnum.GhostLantern -> R.drawable.mat_ghost_lantern
            MaterialEnum.Feather -> R.drawable.mat_feather
            MaterialEnum.Page -> R.drawable.mat_page
            MaterialEnum.Magatama -> R.drawable.mat_magatama
            MaterialEnum.GiantRing -> R.drawable.mat_giant_ring
            MaterialEnum.Claw -> R.drawable.mat_claw
            MaterialEnum.Heart -> R.drawable.mat_heart
            MaterialEnum.SpiritRoot -> R.drawable.mat_spirit_root
            MaterialEnum.Scarab -> R.drawable.mat_scarab
            MaterialEnum.DragonScale -> R.drawable.mat_scale
            MaterialEnum.Gallstone -> R.drawable.mat_gall_stone
            MaterialEnum.Gear -> R.drawable.mat_gear
            MaterialEnum.Grease -> R.drawable.mat_grease
            MaterialEnum.HomunculusBaby -> R.drawable.mat_homunculus
            // TODO: Other Mats
            else -> R.drawable.ic_diamond
        }
}
