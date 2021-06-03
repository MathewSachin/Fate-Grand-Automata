package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.Region
import javax.inject.Inject
import kotlin.time.seconds

class AutoGiftBox @Inject constructor(
    exitManager: ExitManager,
    fgAutomataApi: IFgoAutomataApi
) : EntryPoint(exitManager), IFgoAutomataApi by fgAutomataApi {
    class ExitException(val pickedStacks: Int): Exception()

    companion object {
        const val maxClickCount = 99
        const val maxNullStreak = 3
    }

    override fun script(): Nothing {
        var clickCount = 0
        var aroundEnd = false
        var nullStreak = 0

        val xpOffsetX = (game.scriptArea.find(images[Images.GoldXP]) ?: game.scriptArea.find(images[Images.SilverXP]))
            ?.Region?.center?.X
            ?: throw Exception("Couldn't find Embers on screen. This shouldn't happen.")

        val checkRegion = Region(xpOffsetX + 1320, 350, 140, 1500)
        val scrollEndRegion = Region(100 + checkRegion.X, 1421, 320, 19)

        while (clickCount < maxClickCount) {
            val picked = useSameSnapIn {
                if (!aroundEnd) {
                    // The scrollbar end position matches before completely at end
                    // a few items can be left off if we're not careful
                    aroundEnd = images[Images.GiftBoxScrollEnd] in scrollEndRegion
                }

                pickGifts(checkRegion)
            }

            clickCount += picked

            swipe(
                game.giftBoxSwipeStart,
                game.giftBoxSwipeEnd
            )

            if (aroundEnd) {
                // Once we're around the end, stop after we don't pick anything consecutively
                if (picked == 0) {
                    ++nullStreak
                } else nullStreak = 0

                if (nullStreak >= maxNullStreak) {
                    break
                }

                // Longer animations. At the end, items pulled up and released.
                1.seconds.wait()
            }
        }

        /*
           clickCount can be higher than maxClickCount when the script is close to the limit and
           finds multiple collectible stacks on the screen. FGO will not register the extra clicks.
         */
        throw ExitException(clickCount.coerceAtMost(maxClickCount))
    }

    // Return picked count
    private fun pickGifts(checkRegion: Region): Int {
        var clickCount = 0

        for (gift in checkRegion.findAll(images[Images.GiftBoxCheck]).sorted()) {
            val countRegion = when (prefs.gameServer) {
                GameServerEnum.Jp, GameServerEnum.Tw, GameServerEnum.Cn -> -940
                GameServerEnum.En -> -830
                GameServerEnum.Kr -> -960
            }.let { x -> Region(x, -120, 300, 100) } + gift.Region.location

            val iconRegion = Region(-1480, -116, 300, 240) + gift.Region.location

            val gold = images[Images.GoldXP] in iconRegion
            val silver = !gold && images[Images.SilverXP] in iconRegion

            if (gold || silver) {
                if (gold) {
                    val count = mapOf(
                        1 to images[Images.ExpX1],
                        2 to images[Images.ExpX2],
                        3 to images[Images.ExpX3],
                        4 to images[Images.ExpX4]
                    ).entries.firstOrNull { (_, pattern) ->
                        countRegion.exists(pattern, Similarity = 0.87)
                    }?.key

                    if (count == null || count > prefs.maxGoldEmberSetSize) {
                        continue
                    }
                }

                gift.Region.click()
                ++clickCount
            }
        }

        return clickCount
    }
}