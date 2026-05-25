package volthack.modules.world

import net.minecraft.client.Minecraft
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.world.breed.*

object AutoBreed : Module("AutoBreed", "Automatically feeds and breeds nearby animals using correct foods from your hotbar", Category.WORLD) {
    private val reachRange by float("Range", 4.5f, 2.0f, 6.0f, 0.1f)
    private val feedDelay by int("Feed Delay Ticks", 4, 1, 20)

    private var tickCooldown = 0

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return

        if (tickCooldown > 0) {
            tickCooldown--
            return
        }

        // Synchronize settings to BreedConfig
        BreedConfig.range = reachRange.toDouble()

        // Get targets matching our breed selectors
        val targets = BreedTargetSelector.findBreedTargets()

        for (animal in targets) {
            val foodSlot = FoodFinder.findFoodSlot(animal)
            if (foodSlot != -1) {
                BreedingAction.feed(animal, foodSlot)
                tickCooldown = feedDelay
                break
            }
        }
    }

    override fun onDisable() {
        tickCooldown = 0
    }
}
