package volthack.util.misc.fish

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.projectile.FishingHook

object FishUtils {
    private val mc get() = Minecraft.getInstance()
    private val player get() = mc.player

    fun getFishingHook(): FishingHook? {
        val p = player ?: return null
        val world = mc.level ?: return null
        return world.entitiesForRendering()
            .filterIsInstance<FishingHook>()
            .find { it.owner == p }
    }

    fun isHookInWater(): Boolean {
        val hook = getFishingHook() ?: return false
        return hook.isInWater
    }

    fun hasBite(): Boolean {
        val hook = getFishingHook() ?: return false
        return hook.isInWater && (Math.abs(hook.deltaMovement.y) > 0.08)
    }
}
