package volthack.util.misc.blocks

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.state.BlockState

object BlockUtils {
    private val mc get() = Minecraft.getInstance()
    private val level get() = mc.level

    fun getBlock(pos: BlockPos): BlockState? = level?.getBlockState(pos)
    fun getBlockAt(x: Int, y: Int, z: Int): BlockState? = level?.getBlockState(BlockPos(x, y, z))

    fun isLiquid(pos: BlockPos): Boolean {
        val state = getBlock(pos) ?: return false
        return state.block is LiquidBlock
    }

    fun isWater(pos: BlockPos): Boolean {
        val state = getBlock(pos) ?: return false
        return state.block == Blocks.WATER
    }

    fun isAir(pos: BlockPos): Boolean {
        val state = getBlock(pos) ?: return false
        return state.isAir
    }

    fun isReplaceable(pos: BlockPos): Boolean {
        val state = getBlock(pos) ?: return false
        return state.isAir || state.block is LiquidBlock
    }
}
