package volthack.modules.player

import net.minecraft.client.Minecraft
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module

object AutoTool : Module("AutoTool", "Automatically switches to the best tool when breaking blocks", Category.PLAYER) {
    private val silent by boolean("Silent Switch", false, "Swaps back to original item when done breaking")

    private var originalSlot = -1
    private var wasDigging = false

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val world = mc.level ?: return

        val hit = mc.hitResult
        val isDigging = mc.gameMode?.isDestroying == true || 
            (mc.options.keyAttack.isDown && hit?.type == HitResult.Type.BLOCK)

        if (isDigging && hit is BlockHitResult) {
            val pos = hit.blockPos
            val state = world.getBlockState(pos)
            
            var bestSlot = -1
            var bestSpeed = 1.0f

            for (i in 0..8) {
                val stack = player.inventory.getItem(i)
                val speed = stack.getDestroySpeed(state)
                if (speed > bestSpeed) {
                    bestSpeed = speed
                    bestSlot = i
                }
            }

            if (bestSlot != -1) {
                if (!wasDigging) {
                    originalSlot = player.inventory.selectedSlot
                    wasDigging = true
                }
                if (player.inventory.selectedSlot != bestSlot) {
                    player.inventory.selectedSlot = bestSlot
                }
            }
        } else {
            if (wasDigging) {
                if (silent && originalSlot in 0..8 && player.inventory.selectedSlot != originalSlot) {
                    player.inventory.selectedSlot = originalSlot
                }
                wasDigging = false
                originalSlot = -1
            }
        }
    }
}
