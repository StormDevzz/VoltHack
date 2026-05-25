package volthack.util.world.highway

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand

object HighwayMiner {
    fun mine(pos: BlockPos): Boolean {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return false
        val world = mc.level ?: return false
        val gameMode = mc.gameMode ?: return false

        val state = world.getBlockState(pos)
        if (state.isAir || state.canBeReplaced()) return false

        HighwayRotation.rotateTo(pos)

        // Dynamically find the best tool slot in the hotbar based on destroy speed
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

        val oldSlot = player.inventory.selectedSlot
        if (bestSlot != -1) {
            player.inventory.selectedSlot = bestSlot
        }

        player.swing(InteractionHand.MAIN_HAND)
        gameMode.destroyBlock(pos)

        player.inventory.selectedSlot = oldSlot

        return true
    }
}
