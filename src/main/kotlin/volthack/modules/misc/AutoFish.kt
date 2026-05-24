package volthack.modules.misc

import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.misc.fish.FishUtils

object AutoFish : Module("AutoFish", "Automatically catches fish for you", Category.MISC) {
    private val autoCast by boolean("Auto Cast", true, "Automatically recast after catching")
    private val castDelay by int("Cast Delay (ms)", 500, 100, 2000, "Delay before recasting")

    private var lastCastTime = 0L
    private var wasBiting = false
    private var caught = 0

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val hook = FishUtils.getFishingHook()

        if (hook == null) {
            if (autoCast && System.currentTimeMillis() - lastCastTime > castDelay) {
                mc.gameMode?.useItem(player, InteractionHand.MAIN_HAND)
                lastCastTime = System.currentTimeMillis()
            }
            return
        }

        val isBiting = FishUtils.hasBite()
        if (isBiting && !wasBiting) {
            mc.gameMode?.useItem(player, InteractionHand.MAIN_HAND)
            caught++
            lastCastTime = System.currentTimeMillis()
        }
        wasBiting = isBiting
    }

    override fun onDisable() {
        wasBiting = false
        caught = 0
    }
}
