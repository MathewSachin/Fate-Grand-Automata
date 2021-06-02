package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.ExitManager
import javax.inject.Inject
import kotlin.time.seconds

/**
 * Continually opens lottery boxes until either the present box is full or there is no currency left.
 */
class AutoLottery @Inject constructor(
    exitManager: ExitManager,
    fgAutomataApi: IFgoAutomataApi
) : EntryPoint(exitManager), IFgoAutomataApi by fgAutomataApi {
    sealed class ExitReason {
        object ResetDisabled: ExitReason()
        object PresentBoxFull: ExitReason()
    }

    class ExitException(val reason: ExitReason): Exception()

    private fun spin() {
        // Don't increase this too much or you'll regret when you're not able to stop the script
        // And your phone won't let you press anything
        game.lotterySpinClick.click(25)
    }

    private fun reset() {
        if (prefs.preventLotteryBoxReset) {
            throw ExitException(ExitReason.ResetDisabled)
        }

        game.lotteryResetClick.click()
        0.5.seconds.wait()

        game.lotteryResetConfirmationClick.click()
        3.seconds.wait()

        game.lotteryResetCloseClick.click()
        2.seconds.wait()
    }

    override fun script(): Nothing {
        while (true) {
            useSameSnapIn {
                when {
                    images.finishedLotteryBox in game.lotteryFinishedRegion -> reset()
                    images.presentBoxFull in game.lotteryFullPresentBoxRegion -> {
                        throw ExitException(ExitReason.PresentBoxFull)
                    }
                    else -> spin()
                }
            }
        }
    }
}
