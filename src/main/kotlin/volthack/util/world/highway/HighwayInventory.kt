package volthack.util.world.highway

import net.minecraft.client.Minecraft
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Items

object HighwayInventory {
    fun findBuildingBlockSlot(): Int {
        val player = Minecraft.getInstance().player ?: return -1
        
        for (i in 0..8) {
            val stack = player.inventory.getItem(i)
            if (stack.item == Items.OBSIDIAN) return i
        }

        for (i in 0..8) {
            val stack = player.inventory.getItem(i)
            if (stack.item == Items.NETHERRACK || stack.item == Items.COBBLESTONE) return i
        }

        for (i in 0..8) {
            val stack = player.inventory.getItem(i)
            if (stack.item is BlockItem) return i
        }

        return -1
    }
}
