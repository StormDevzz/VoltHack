package volthack.modules.combat

import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module

object Aura : Module("Aura", "Attacks nearest entity", Category.COMBAT) {
    private val range by float("Range", 4.2f, 0f, 6f, 0.1f)
    private val mode by mode("Mode", listOf("Single", "Multi"), "Single")
    private val delay by int("Delay", 0, 0, 20)

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private var ticks = 0

    private fun onTick() {
        if (!enabled) return
        ticks++
        if (ticks < delay) return
        ticks = 0

        val player = Minecraft.getInstance().player ?: return
        val world = Minecraft.getInstance().level ?: return

        @Suppress("UNCHECKED_CAST")
        val targets = (world.entitiesForRendering() as Iterable<Entity>)
            .filterIsInstance<LivingEntity>()
            .filter { it != player && !it.isDeadOrDying }
            .filter { player.distanceTo(it) <= range }
            .sortedBy { player.distanceTo(it) }

        if (mode == "Multi") {
            targets.forEach { attack(player, it) }
        } else {
            targets.firstOrNull()?.let { attack(player, it) }
        }
    }

    private fun attack(player: Player, target: Entity) {
        val mc = Minecraft.getInstance()
        mc.gameMode?.attack(player, target)
        player.swing(InteractionHand.MAIN_HAND)
    }
}
