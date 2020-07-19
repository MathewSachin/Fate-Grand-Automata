package com.mathewsachin.fategrandautomata.prefs

import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.modules.limitBrokenCharacter
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.fategrandautomata.scripts.supportCeFolder
import com.mathewsachin.fategrandautomata.scripts.supportFriendFolder
import com.mathewsachin.fategrandautomata.scripts.supportServantImgFolder
import java.io.File

class SupportPreferences(val prefs: SharedPreferenceDelegation) :
    ISupportPreferences {
    override val friendNames by prefs.stringSet(R.string.pref_support_friend_names)
        .map { friendSet ->
            val friendImgFolderName = supportFriendFolder.name

            val friendNames = friendSet
                .map { "${friendImgFolderName}/$it" }

            friendNames.joinToString()
        }

    override val preferredServants by prefs.stringSet(R.string.pref_support_pref_servant)
        .map { servantSet ->
            val servants = mutableListOf<String>()

            for (servantEntry in servantSet) {
                val dir = File(supportServantImgFolder, servantEntry)

                if (dir.isDirectory && dir.exists()) {
                    val fileNames = dir.listFiles()
                        .filter { it.isFile }
                        // Give priority to later ascensions
                        .sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name })
                        .map { "${servantEntry}/${it.name}" }

                    servants.addAll(fileNames)
                } else servants.add(servantEntry)
            }

            val servantImgFolderName = supportServantImgFolder.name

            servants.joinToString { "${servantImgFolderName}/$it" }
        }

    override val mlb by prefs.bool(R.string.pref_support_pref_ce_mlb)

    override val preferredCEs by prefs.stringSet(R.string.pref_support_pref_ce).map { ceSet ->
        val ceImgFolderName = supportCeFolder.name

        var ces = ceSet
            .map { "${ceImgFolderName}/$it" }

        if (mlb) {
            ces = ces.map { "${limitBrokenCharacter}${it}" }
        }

        ces.joinToString()
    }

    override val friendsOnly by prefs.bool(R.string.pref_support_friends_only)

    override val selectionMode by prefs.enum(
        R.string.pref_support_mode,
        SupportSelectionModeEnum.Preferred
    )

    override val fallbackTo by prefs.enum(
        R.string.pref_support_mode,
        SupportSelectionModeEnum.Manual
    )
}