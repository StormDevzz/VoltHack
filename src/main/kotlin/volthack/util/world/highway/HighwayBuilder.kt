package volthack.util.world.highway

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

object HighwayBuilder {
    fun build(pos: BlockPos, slot: Int): Boolean {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return false
        val world = mc.level ?: return false
        val gameMode = mc.gameMode ?: return false

        val state = world.getBlockState(pos)
        if (!state.isAir && !state.canBeReplaced()) return false

        HighwayRotation.rotateTo(pos)

        val oldSlot = player.inventory.selectedSlot
        player.inventory.selectedSlot = slot

        val hitResult = BlockHitResult(
            Vec3(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5),
            Direction.UP,
            pos,
            false
        )
        gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hitResult)
        player.swing(InteractionHand.MAIN_HAND)

        player.inventory.selectedSlot = oldSlot

        return true
    }
}
