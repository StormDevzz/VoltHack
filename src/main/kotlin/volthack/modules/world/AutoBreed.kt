package volthack.modules.world

import net.minecraft.client.Minecraft
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.world.breed.*

object AutoBreed : Module("AutoBreed", "Automatically feeds and breeds nearby animals", Category.WORLD) {
    private val reachRange by float("Range", 4.5f, 2.0f, 6.0f, 0.1f, "Interaction range")
    private val feedDelay by int("Feed Delay Ticks", 4, 1, 20, "Ticks between feeding actions")

    // Per-animal toggles
    val breedPigs by boolean("Pigs", true, "Breed pigs")
    val breedCows by boolean("Cows", true, "Breed cows")
    val breedHorses by boolean("Horses", false, "Breed horses")
    val breedLlamas by boolean("Llamas", false, "Breed llamas")
    val breedSheep by boolean("Sheep", true, "Breed sheep")
    val breedRabbits by boolean("Rabbits", false, "Breed rabbits")
    val breedChickens by boolean("Chickens", true, "Breed chickens")

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

        BreedConfig.range = reachRange.toDouble()

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
