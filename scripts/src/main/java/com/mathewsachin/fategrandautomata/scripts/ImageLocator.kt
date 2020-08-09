package com.mathewsachin.fategrandautomata.scripts

import javax.inject.Inject

class ImageLocator @Inject constructor(val imgLoader: IImageLoader) {
    private fun load(path: String) = imgLoader.loadRegionPattern(path)

    fun loadSupportPattern(path: String) = imgLoader.loadSupportPattern(path)

    val battle get() = load("battle.png")

    val targetDanger get() = load("target_danger.png")

    val targetServant get() = load("target_servant.png")

    val buster get() = load("buster.png")

    val art get() = load("art.png")

    val quick get() = load("quick.png")

    val weak get() = load("weak.png")

    val resist get() = load("resist.png")

    val friend get() = load("friend.png")

    val guest get() = load("guest.png")

    val follow get() = load("follow.png")

    val limitBroken get() = load("limitbroken.png")

    val supportScreen get() = load("support_screen.png")

    val supportRegionTool
        get() = load(
            "support_region_tool.png"
        )

    val storySkip get() = load("storyskip.png")

    val menu get() = load("menu.png")

    val stamina get() = load("stamina.png")

    val result get() = load("result.png")

    val bond get() = load("bond.png")

    val bond10Reward get() = load("ce_reward.png")

    val friendRequest get() = load("friendrequest.png")

    val confirm get() = load("confirm.png")

    val questReward get() = load("questreward.png")

    val retry get() = load("retry.png")

    val withdraw get() = load("withdraw.png")

    val finishedLotteryBox
        get() = load(
            "lottery.png"
        )

    val presentBoxFull get() = load("StopGifts.png")

    val masterExp get() = load("master_exp.png")

    // TODO: Verify the CN image
    val masterLvlUp get() = load("master_lvl_up.png")

    val matRewards get() = load("mat_rewards.png")

    val gudaFinalRewards
        get() = load(
            "guda_final_rewards.png"
        )

    val inventoryFull get() = load("inven_full.png")

    val fpSummonContinue
        get() = load(
            "fp_continue.png"
        )

    val skillTen get() = load("skill_ten.png")

    val stun get() = load("stun.png")

    val selectedParty get() = load("selected_party.png")

    val ceDrop get() = load("ce_drop.png")

    val friendSummon get() = load("friend_summon.png")

    val dropScrollbar get() = load("drop_scrollbar.png")
}