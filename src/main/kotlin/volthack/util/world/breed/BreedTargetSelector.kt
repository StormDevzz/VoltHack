package volthack.util.world.breed

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.animal.Animal

object BreedTargetSelector {
    fun findBreedTargets(): List<Animal> {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return emptyList()
        val world = mc.level ?: return emptyList()
        val range = BreedConfig.range

        return world.entitiesForRendering()
            .filterIsInstance<Animal>()
            .filter { it.distanceToSqr(player) <= range * range }
            .filter { BreedFilter.isBreedable(it) }
    }
}
