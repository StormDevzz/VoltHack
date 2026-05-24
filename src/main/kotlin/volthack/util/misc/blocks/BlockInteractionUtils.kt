package volthack.util.misc.blocks

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Blocks

object BlockInteractionUtils {
    private val mc get() = Minecraft.getInstance()
    private val player get() = mc.player

    fun placeBlock(pos: BlockPos, hand: InteractionHand = InteractionHand.MAIN_HAND): Boolean {
        val p = player ?: return false
        val world = mc.level ?: return false
        val hitResult = mc.hitResult

        val lookingAt = when {
            hitResult != null && hitResult.type == net.minecraft.world.phys.HitResult.Type.BLOCK -> {
                (hitResult as net.minecraft.world.phys.BlockHitResult).blockPos
            }
            else -> null
        }

        val face = if (lookingAt != null) {
            Direction.UP
        } else Direction.UP

        val vec3 = net.minecraft.world.phys.Vec3.atCenterOf(pos)
        val blockHitResult = net.minecraft.world.phys.BlockHitResult(
            vec3, face, pos, false
        )

        mc.gameMode?.useItemOn(p, hand, blockHitResult)
        return true
    }

    fun selectSpongeInHotbar(): Boolean {
        val slot = BlockSearchUtils.findSpongeInHotbar()
        if (slot == -1) return false
        player?.inventory?.selectedSlot = slot
        return true
    }

    fun hasSponge(): Boolean {
        val p = player ?: return false
        for (i in 0..35) {
            if (p.inventory.getItem(i).item == Blocks.SPONGE.asItem()) return true
        }
        return false
    }

    fun getSpongeCount(): Int {
        val p = player ?: return 0
        var count = 0
        for (i in 0..35) {
            val stack = p.inventory.getItem(i)
            if (stack.item == Blocks.SPONGE.asItem()) count += stack.count
        }
        return count
    }
}
