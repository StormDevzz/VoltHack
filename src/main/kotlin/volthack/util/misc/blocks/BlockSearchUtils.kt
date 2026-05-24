package volthack.util.misc.blocks

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks

object BlockSearchUtils {
    private val mc get() = Minecraft.getInstance()
    private val level get() = mc.level
    private val player get() = mc.player

    fun findWaterInRange(range: Int): List<BlockPos> {
        val p = player ?: return emptyList()
        val world = level ?: return emptyList()
        val results = mutableListOf<BlockPos>()
        val pos = p.blockPosition()
        for (dx in -range..range) {
            for (dy in (-range).coerceAtLeast(-64)..range.coerceAtMost(64)) {
                for (dz in -range..range) {
                    val bp = pos.offset(dx, dy, dz)
                    if (world.getBlockState(bp).block == Blocks.WATER) {
                        results.add(bp)
                    }
                }
            }
        }
        return results
    }

    fun findSpongeInHotbar(): Int {
        val p = player ?: return -1
        for (i in 0..8) {
            if (p.inventory.getItem(i).item == Blocks.SPONGE.asItem()) return i
        }
        return -1
    }
}
