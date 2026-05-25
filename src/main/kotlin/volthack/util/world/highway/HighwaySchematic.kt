package volthack.util.world.highway

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction

object HighwaySchematic {
    fun getLayoutOffsets(dir: Direction): List<BlockPos> {
        val list = mutableListOf<BlockPos>()
        val right = dir.clockWise

        // 1. Floor (3 wide, 1 block below feet)
        for (w in -1..1) {
            list.add(BlockPos(0, -1, 0).relative(right, w))
        }

        // 2. Walls (2 high, at left and right boundaries)
        if (HighwayConfig.buildWalls) {
            for (y in 0..1) {
                list.add(BlockPos(0, y, 0).relative(right, -2))
                list.add(BlockPos(0, y, 0).relative(right, 2))
            }
        }

        // 3. Ceiling (3 wide, 2 blocks above feet)
        if (HighwayConfig.buildCeiling) {
            for (w in -1..1) {
                list.add(BlockPos(0, 2, 0).relative(right, w))
            }
        }

        return list
    }

    fun getTunnelOffsets(dir: Direction): List<BlockPos> {
        // Space that needs to be cleared for the player to pass (3x3 tunnel)
        val list = mutableListOf<BlockPos>()
        val right = dir.clockWise

        for (y in 0..2) {
            for (w in -1..1) {
                list.add(BlockPos(0, y, 0).relative(right, w))
            }
        }
        return list
    }
}
