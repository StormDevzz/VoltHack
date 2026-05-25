package volthack.util.world.breed

import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object FoodFinder {
    fun getBreedingFood(animal: Animal): Item? {
        val id = animal.type.descriptionId.lowercase()
        return when {
            id.contains("cow") || id.contains("sheep") -> Items.WHEAT
            id.contains("pig") -> Items.CARROT
            id.contains("chicken") -> Items.WHEAT_SEEDS
            id.contains("wolf") -> Items.BEEF
            id.contains("rabbit") -> Items.CARROT
            id.contains("horse") || id.contains("donkey") -> Items.GOLDEN_APPLE
            id.contains("cat") || id.contains("ocelot") -> Items.COD
            id.contains("llama") -> Items.HAY_BLOCK
            else -> null
        }
    }

    fun findFoodSlot(animal: Animal): Int {
        val player = Minecraft.getInstance().player ?: return -1
        val food = getBreedingFood(animal) ?: return -1
        
        for (i in 0..8) {
            val stack = player.inventory.getItem(i)
            if (!stack.isEmpty && isMatchingFood(stack, food, animal)) {
                return i
            }
        }
        return -1
    }

    private fun isMatchingFood(stack: ItemStack, foodItem: Item, animal: Animal): Boolean {
        if (stack.item == foodItem) return true
        
        val item = stack.item
        val id = animal.type.descriptionId.lowercase()
        if (id.contains("pig")) {
            return item == Items.CARROT || item == Items.POTATO || item == Items.BEETROOT
        }
        if (id.contains("chicken")) {
            return item == Items.WHEAT_SEEDS || item == Items.MELON_SEEDS || item == Items.PUMPKIN_SEEDS || item == Items.BEETROOT_SEEDS
        }
        if (id.contains("wolf")) {
            return stack.get(DataComponents.FOOD) != null
        }
        if (id.contains("cat") || id.contains("ocelot")) {
            return item == Items.COD || item == Items.SALMON
        }
        return false
    }
}
