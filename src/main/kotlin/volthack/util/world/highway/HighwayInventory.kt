package volthack.util.world.highway

import net.minecraft.client.Minecraft
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Items

object HighwayInventory {
    fun findBuildingBlockSlot(priority: String): Int {
        val player = Minecraft.getInstance().player ?: return -1
        
        // 1. Check direct preference first
        when (priority) {
            "Obsidian" -> {
                val slot = findItemSlot(Items.OBSIDIAN)
                if (slot != -1) return slot
            }
            "Netherrack" -> {
                val slot = findItemSlot(Items.NETHERRACK)
                if (slot != -1) return slot
            }
            "Cobblestone" -> {
                val slot = findItemSlot(Items.COBBLESTONE)
                if (slot != -1) return slot
            }
        }

        // 2. Check general priority order if target preferred is missing
        val obsidian = findItemSlot(Items.OBSIDIAN)
        if (obsidian != -1) return obsidian

        val netherrack = findItemSlot(Items.NETHERRACK)
        if (netherrack != -1) return netherrack

        val cobble = findItemSlot(Items.COBBLESTONE)
        if (cobble != -1) return cobble

        // 3. Fallback: Any block
        for (i in 0..8) {
            val stack = player.inventory.getItem(i)
            if (stack.item is BlockItem) return i
        }

        return -1
    }

    private fun findItemSlot(item: net.minecraft.world.item.Item): Int {
        val player = Minecraft.getInstance().player ?: return -1
        for (i in 0..8) {
            if (player.inventory.getItem(i).item == item) return i
        }
        return -1
    }
}
