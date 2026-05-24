package volthack.util.combat.crystal

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.boss.enderdragon.EndCrystal
import net.minecraft.world.phys.Vec3

object CrystalUtil {
    fun findCrystalsInRange(range: Float): List<EndCrystal> {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return emptyList()
        val world = mc.level ?: return emptyList()

        return world.entitiesForRendering()
            .filterIsInstance<EndCrystal>()
            .filter { player.distanceTo(it) <= range }
    }

    fun isWithinPlaceRange(pos: BlockPos, range: Float): Boolean {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return false
        val eyePos = player.eyePosition
        val center = Vec3(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
        return eyePos.distanceTo(center) <= range
    }
}
