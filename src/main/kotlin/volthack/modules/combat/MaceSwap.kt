package volthack.modules.combat

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Items
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.player.HotbarUtils

object MaceSwap : Module("MaceSwap", "Swaps to a Mace dynamically when falling to perform smash attacks", Category.COMBAT) {
    private val range by float("Range", 4.5f, 2.0f, 6.0f, 0.1f)
    private val minFall by float("Min Fall Distance", 1.0f, 0.5f, 3.0f, 0.1f)
    private val swapBack by boolean("Swap Back", true, "Swap back to the previous weapon upon landing")

    private var swapped = false
    private var savedSlot = -1

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val level = mc.level ?: return

        // If player is falling
        if (player.fallDistance > minFall) {
            // Find target in range
            val target = (level.entitiesForRendering() as Iterable<Entity>)
                .filterIsInstance<LivingEntity>()
                .filter { it != player && !it.isDeadOrDying }
                .any { player.distanceTo(it) <= range }

            if (target && !swapped) {
                val maceSlot = HotbarUtils.find { it.item == Items.MACE }
                if (maceSlot != -1 && HotbarUtils.selectedSlot != maceSlot) {
                    savedSlot = HotbarUtils.selectedSlot
                    HotbarUtils.select(maceSlot)
                    swapped = true
                }
            }
        }

        // When landed or touching liquid
        if (player.onGround() || player.isInWater || player.isInLava) {
            if (swapped) {
                if (swapBack && savedSlot != -1) {
                    HotbarUtils.select(savedSlot)
                }
                swapped = false
                savedSlot = -1
            }
        }
    }

    override fun onDisable() {
        if (swapped && savedSlot != -1 && swapBack) {
            HotbarUtils.select(savedSlot)
        }
        swapped = false
        savedSlot = -1
    }
}
