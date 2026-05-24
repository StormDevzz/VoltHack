package volthack.modules.world

import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.animal.camel.Camel
import net.minecraft.world.entity.animal.equine.AbstractHorse
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse
import net.minecraft.world.entity.animal.equine.Horse
import net.minecraft.world.entity.animal.equine.SkeletonHorse
import net.minecraft.world.entity.animal.equine.ZombieHorse
import net.minecraft.world.entity.animal.pig.Pig
import net.minecraft.world.entity.monster.Strider
import net.minecraft.world.entity.player.Player
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module

object AutoMount : Module("AutoMount", "Automatically mounts rideable entities", Category.WORLD) {
    private val range by float("Range", 4.0f, 1f, 6f, 0.5f)
    private val onlySaddled by boolean("Only Saddled", true)
    private val mountBabies by boolean("Mount Babies", false)

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        if (player.vehicle != null) return
        val world = mc.level ?: return

        val mount = world.entitiesForRendering()
            .filter { it != player && it.isAlive && player.distanceTo(it) <= range }
            .filter { canMount(it) }
            .minByOrNull { player.distanceTo(it) } ?: return

        mc.gameMode?.interact(player, mount, InteractionHand.MAIN_HAND)
    }

    private fun canMount(entity: Entity): Boolean {
        return when {
            entity is AbstractHorse -> {
                if (!mountBabies && entity.isBaby) return false
                if (onlySaddled && !entity.isSaddled) return false
                true
            }
            entity is Pig -> {
                if (onlySaddled && !entity.isSaddled) return false
                true
            }
            entity is Strider -> {
                if (onlySaddled && !entity.isSaddled) return false
                true
            }
            else -> false
        }
    }
}
