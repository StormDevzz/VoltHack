package volthack.util.world.highway

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction

object HighwayBlockPos {
    fun getTargetCenter(playerPos: BlockPos, dir: Direction, distance: Int): BlockPos {
        return playerPos.relative(dir, distance)
    }

    fun getBuildBlocks(playerPos: BlockPos, dir: Direction, distance: Int): List<BlockPos> {
        val center = getTargetCenter(playerPos, dir, distance)
        return HighwaySchematic.getLayoutOffsets(dir).map { center.offset(it.x, it.y, it.z) }
    }

    fun getMineBlocks(playerPos: BlockPos, dir: Direction, distance: Int): List<BlockPos> {
        val center = getTargetCenter(playerPos, dir, distance)
        return HighwaySchematic.getTunnelOffsets(dir).map { center.offset(it.x, it.y, it.z) }
    }
}
