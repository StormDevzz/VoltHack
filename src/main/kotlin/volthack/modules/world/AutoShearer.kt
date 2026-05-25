package volthack.modules.world

import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Items
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module

object AutoShearer : Module("AutoShearer", "Shears sheep smoothly without packet spam", Category.WORLD) {
    private val reachRange by float("Range", 4.5f, 2.0f, 6.0f, 0.1f)
    private val shearDelay by int("Shear Delay Ticks", 4, 1, 20)

    private var cooldown = 0
    private var originalSlot = -1

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return

        if (cooldown > 0) {
            cooldown--
            return
        }

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val world = mc.level ?: return

        val shearsSlot = (0..8).firstOrNull { player.inventory.getItem(it).item == Items.SHEARS } ?: return

        // Scan for all unsheared sheep in range
        val unshearedSheep = world.entitiesForRendering()
            .filter { it.isAlive && it.type.descriptionId.contains("sheep", ignoreCase = true) }
            .filter { player.distanceToSqr(it) <= (reachRange * reachRange).toDouble() }
            .filter { isUnsheared(it) }

        if (unshearedSheep.isNotEmpty()) {
            // If we aren't holding shears yet, remember original slot and swap
            if (player.inventory.selectedSlot != shearsSlot) {
                if (originalSlot == -1) {
                    originalSlot = player.inventory.selectedSlot
                }
                player.inventory.selectedSlot = shearsSlot
            }

            // Shear the first sheep
            val targetSheep = unshearedSheep.first()
            mc.gameMode?.interact(player, targetSheep, InteractionHand.MAIN_HAND)
            player.swing(InteractionHand.MAIN_HAND)
            cooldown = shearDelay
        } else {
            // No more sheep need shearing, restore original slot if swapped
            if (originalSlot != -1) {
                if (player.inventory.selectedSlot == shearsSlot) {
                    player.inventory.selectedSlot = originalSlot
                }
                originalSlot = -1
            }
        }
    }

    private fun isUnsheared(entity: Entity): Boolean {
        return try {
            val isShearedMethod = entity.javaClass.getMethod("isSheared")
            !(isShearedMethod.invoke(entity) as Boolean)
        } catch (_: Exception) {
            false
        }
    }

    override fun onDisable() {
        cooldown = 0
        val mc = Minecraft.getInstance()
        val player = mc.player
        if (player != null && originalSlot != -1) {
            player.inventory.selectedSlot = originalSlot
        }
        originalSlot = -1
    }
}
