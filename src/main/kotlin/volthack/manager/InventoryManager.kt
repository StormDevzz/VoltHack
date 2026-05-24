package volthack.manager

import net.minecraft.client.Minecraft
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object InventoryManager {
    private val mc get() = Minecraft.getInstance()

    fun findItem(itemId: String): Int {
        val player = mc.player ?: return -1
        return (0..35).indexOfFirst {
            player.inventory.getItem(it).item.descriptionId.contains(itemId, ignoreCase = true)
        }
    }

    fun findItem(predicate: (ItemStack) -> Boolean): Int {
        val player = mc.player ?: return -1
        return (0..35).indexOfFirst { predicate(player.inventory.getItem(it)) }
    }

    fun countItem(itemId: String): Int {
        val player = mc.player ?: return 0
        return (0..35).count {
            player.inventory.getItem(it).item.descriptionId.contains(itemId, ignoreCase = true)
        }
    }

    fun move(slot: Int, targetSlot: Int) {
        val player = mc.player ?: return
        val handler = player.containerMenu
        mc.gameMode?.handleInventoryMouseClick(handler.containerId, slot, targetSlot, ClickType.SWAP, player)
    }

    fun drop(slot: Int, entireStack: Boolean = true) {
        val player = mc.player ?: return
        val handler = player.containerMenu
        val button = if (entireStack) 0 else 1
        mc.gameMode?.handleInventoryMouseClick(handler.containerId, slot, button, ClickType.THROW, player)
    }

    fun quickMove(slot: Int) {
        val player = mc.player ?: return
        val handler = player.containerMenu
        mc.gameMode?.handleInventoryMouseClick(handler.containerId, slot, 0, ClickType.QUICK_MOVE, player)
    }

    fun isHoldingSword(): Boolean {
        val player = mc.player ?: return false
        val stack = player.mainHandItem
        return stack.item == Items.DIAMOND_SWORD || stack.item == Items.NETHERITE_SWORD ||
               stack.item == Items.IRON_SWORD || stack.item == Items.STONE_SWORD ||
               stack.item == Items.WOODEN_SWORD
    }

    fun isHoldingPickaxe(): Boolean {
        val player = mc.player ?: return false
        val stack = player.mainHandItem
        return stack.item.descriptionId.contains("pickaxe", ignoreCase = true)
    }

    fun windowClick(slot: Int, action: ClickType) {
        val player = mc.player ?: return
        mc.gameMode?.handleInventoryMouseClick(player.containerMenu.containerId, slot, 0, action, player)
    }
}
