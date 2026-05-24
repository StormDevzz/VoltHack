package volthack.util.misc.blocks

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluids

object BlockFluidUtils {
    private val mc get() = Minecraft.getInstance()
    private val level get() = mc.level

    fun getWaterSourcesInRange(center: BlockPos, range: Int): List<BlockPos> {
        val world = level ?: return emptyList()
        val sources = mutableListOf<BlockPos>()
        for (dx in -range..range) {
            for (dy in -range..range) {
                for (dz in -range..range) {
                    val pos = center.offset(dx, dy, dz)
                    val state = world.getBlockState(pos)
                    if (isWaterSource(state, pos)) sources.add(pos)
                }
            }
        }
        return sources
    }

    private fun isWaterSource(state: BlockState, pos: BlockPos): Boolean {
        if (state.block != Blocks.WATER) return false
        return try {
            val fluidState = state.getFluidState()
            fluidState.isSource && fluidState.type == Fluids.WATER
        } catch (_: Exception) {
            false
        }
    }

    fun drainWater(pos: BlockPos): Boolean {
        val world = level ?: return false
        if (world.getBlockState(pos).block != Blocks.WATER) return false
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3)
        return true
    }

    fun isAdjacentToWater(pos: BlockPos): Boolean {
        for (dir in net.minecraft.core.Direction.values()) {
            if (isWaterSource(pos.relative(dir))) return true
        }
        return false
    }

    private fun isWaterSource(pos: BlockPos): Boolean {
        val world = level ?: return false
        val state = world.getBlockState(pos)
        return isWaterSource(state, pos)
    }
}
