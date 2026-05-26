package volthack.util.world.breed

import net.minecraft.world.entity.animal.Animal
import volthack.modules.world.AutoBreed

object BreedFilter {
    fun isBreedable(animal: Animal): Boolean {
        if (!animal.isAlive || animal.age != 0 || animal.isInLove) return false
        return isAllowedType(animal)
    }

    private fun isAllowedType(animal: Animal): Boolean {
        val id = animal.type.descriptionId.lowercase()
        return when {
            id.contains("pig")     -> AutoBreed.breedPigs
            id.contains("cow") || id.contains("mooshroom") -> AutoBreed.breedCows
            id.contains("horse")   -> AutoBreed.breedHorses
            id.contains("llama")   -> AutoBreed.breedLlamas
            id.contains("sheep")   -> AutoBreed.breedSheep
            id.contains("rabbit")  -> AutoBreed.breedRabbits
            id.contains("chicken") -> AutoBreed.breedChickens
            else                   -> false
        }
    }
}
