package com.mathewsachin.fategrandautomata.ui.support_img_namer

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerDialog
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerUserInterface
import com.mathewsachin.fategrandautomata.scripts.entrypoints.getCeImgPath
import com.mathewsachin.fategrandautomata.scripts.entrypoints.getFriendImgPath
import com.mathewsachin.fategrandautomata.scripts.entrypoints.getServantImgPath

// *, ?, \, |, / are special characters in Regex and need to be escaped using \
private const val InvalidChars = """<>"\|:\*\?\\\/"""
private const val FileNameRegex = """[^\.\s$InvalidChars][^$InvalidChars]*"""

val ServantRegex = Regex("""$FileNameRegex(/$FileNameRegex)?""")
val CeRegex = Regex(FileNameRegex)

private const val InvalidCharsMsg = "<, >, \", |, :, *, ?, \\, /"
const val ServantInvalidMsg =
    "You're not allowed to specify more than 1 folder, files cannot start with a period or space, and these symbols cannot be used: $InvalidCharsMsg"
const val CeOrFriendInvalidMsg =
    "You're not allowed to specify folders, files cannot start with a period or space, and these symbols cannot be used: $InvalidCharsMsg"

private fun getSupportEntries(
    Frame: View,
    storageDirs: StorageDirs
): List<SupportImgEntry> {
    val tempDir = storageDirs.supportImgTempDir

    val servant0 = SupportImgEntry(
        getServantImgPath(
            tempDir,
            0
        ),
        storageDirs.supportServantImgFolder,
        Frame.findViewById(R.id.support_img_servant_0),
        ServantRegex, ServantInvalidMsg
    )
    val servant1 = SupportImgEntry(
        getServantImgPath(
            tempDir,
            1
        ),
        storageDirs.supportServantImgFolder,
        Frame.findViewById(R.id.support_img_servant_1),
        ServantRegex, ServantInvalidMsg
    )

    val ce0 = SupportImgEntry(
        getCeImgPath(
            tempDir,
            0
        ),
        storageDirs.supportCeFolder,
        Frame.findViewById(R.id.support_img_ce_0),
        CeRegex, CeOrFriendInvalidMsg
    )
    val ce1 = SupportImgEntry(
        getCeImgPath(
            tempDir,
            1
        ),
        storageDirs.supportCeFolder,
        Frame.findViewById(R.id.support_img_ce_1),
        CeRegex, CeOrFriendInvalidMsg
    )

    val friend0 = SupportImgEntry(
        getFriendImgPath(
            tempDir,
            0
        ),
        storageDirs.supportFriendFolder,
        Frame.findViewById(R.id.support_img_friend_0),
        CeRegex, CeOrFriendInvalidMsg
    )
    val friend1 = SupportImgEntry(
        getFriendImgPath(
            tempDir,
            1
        ),
        storageDirs.supportFriendFolder,
        Frame.findViewById(R.id.support_img_friend_1),
        CeRegex, CeOrFriendInvalidMsg
    )

    return listOf(servant0, servant1, ce0, ce1, friend0, friend1)
}

fun showSupportImageNamer(UI: ScriptRunnerUserInterface, storageDirs: StorageDirs) {
    val frame = FrameLayout(UI.Service)

    val inflater = LayoutInflater.from(UI.Service)
    inflater.inflate(R.layout.support_img_namer, frame)

    val entryList = getSupportEntries(frame, storageDirs)

    ScriptRunnerDialog(UI).apply {
        autoDismiss = false

        setTitle("Pick what you want")
        setView(frame)

        setPositiveButton("Done") {
            if (entryList.all { it.isValid() }) {
                if (entryList.all { it.rename() }) {
                    hide()
                }
            }
        }

        setNegativeButton(UI.Service.getString(android.R.string.cancel)) { hide() }

        show()
    }
}