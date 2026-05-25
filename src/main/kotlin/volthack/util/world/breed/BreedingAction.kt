package volthack.util.world.breed

import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.animal.Animal

object BreedingAction {
    fun feed(animal: Animal, slot: Int) {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val gameMode = mc.gameMode ?: return

        val oldSlot = player.inventory.selectedSlot
        player.inventory.selectedSlot = slot
        
        gameMode.interact(player, animal, InteractionHand.MAIN_HAND)
        player.swing(InteractionHand.MAIN_HAND)
        
        player.inventory.selectedSlot = oldSlot
    }
}
