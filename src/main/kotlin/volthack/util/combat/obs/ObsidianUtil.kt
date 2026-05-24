package volthack.util.combat.obs

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB

object ObsidianUtil {
    fun isObsidianOrBedrock(pos: BlockPos, world: Level): Boolean {
        val block = world.getBlockState(pos).block
        return block == Blocks.OBSIDIAN || block == Blocks.BEDROCK
    }

    fun canPlaceCrystal(pos: BlockPos, world: Level): Boolean {
        if (!isObsidianOrBedrock(pos, world)) return false
        val up = pos.above()
        val up2 = pos.above(2)

        val state1 = world.getBlockState(up)
        if (!state1.isAir && state1.block != Blocks.FIRE) return false

        val state2 = world.getBlockState(up2)
        if (!state2.isAir && state2.block != Blocks.FIRE) return false

        val box = AABB(
            up.x.toDouble(), up.y.toDouble(), up.z.toDouble(),
            up.x + 1.0, up.y + 2.0, up.z + 1.0
        )
        val entities = world.getEntities(null, box)
        if (entities.isNotEmpty()) return false

        return true
    }
}
