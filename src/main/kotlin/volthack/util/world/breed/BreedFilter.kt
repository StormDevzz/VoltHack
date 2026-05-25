package volthack.util.world.breed

import net.minecraft.world.entity.animal.Animal

object BreedFilter {
    fun isBreedable(animal: Animal): Boolean {
        return animal.isAlive && 
               animal.age == 0 && 
               !animal.isInLove
    }
}
