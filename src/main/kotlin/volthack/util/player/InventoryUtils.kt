package volthack.util.player

import net.minecraft.client.Minecraft
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack

object InventoryUtils {
    private val mc get() = Minecraft.getInstance()
    private val player get() = mc.player

    val isOpen: Boolean get() = player?.containerMenu != player?.inventoryMenu

    fun count(predicate: (ItemStack) -> Boolean): Int {
        val inv = player?.inventory ?: return 0
        return (0..35).count { predicate(inv.getItem(it)) }
    }

    fun countItem(itemId: String): Int {
        return count { it.item.descriptionId.contains(itemId, ignoreCase = true) }
    }

    fun findFirst(predicate: (ItemStack) -> Boolean): Int {
        val inv = player?.inventory ?: return -1
        return (0..35).indexOfFirst { predicate(inv.getItem(it)) }
    }

    fun findFirst(itemId: String): Int {
        return findFirst { it.item.descriptionId.contains(itemId, ignoreCase = true) }
    }

    fun findAll(predicate: (ItemStack) -> Boolean): List<Int> {
        val inv = player?.inventory ?: return emptyList()
        return (0..35).filter { predicate(inv.getItem(it)) }
    }

    fun click(slot: Int, button: Int = 0, action: ClickType = ClickType.PICKUP) {
        val p = player ?: return
        mc.gameMode?.handleInventoryMouseClick(p.containerMenu.containerId, slot, button, action, p)
    }

    fun shiftClick(slot: Int) = click(slot, 0, ClickType.QUICK_MOVE)

    fun drop(slot: Int, wholeStack: Boolean = true) {
        click(slot, if (wholeStack) 0 else 1, ClickType.THROW)
    }

    fun swap(slotA: Int, slotB: Int) {
        click(slotA, slotB, ClickType.SWAP)
    }

    fun moveItems(fromSlots: List<Int>, toSlot: Int) {
        for (slot in fromSlots.reversed()) {
            if (player?.containerMenu?.getSlot(slot)?.hasItem() == true) {
                click(slot, toSlot, ClickType.SWAP)
            }
        }
    }
}