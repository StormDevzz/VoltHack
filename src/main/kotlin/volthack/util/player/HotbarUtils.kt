package volthack.util.player

import net.minecraft.client.Minecraft
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack

object HotbarUtils {
    private val mc get() = Minecraft.getInstance()
    private val player get() = mc.player
    private val inventory get() = player?.inventory

    val selectedSlot: Int get() = inventory?.selectedSlot ?: -1
    val selectedStack: ItemStack? get() = inventory?.getSelectedItem()
    val mainHand: ItemStack? get() = player?.mainHandItem
    val offHand: ItemStack? get() = player?.offhandItem

    fun select(slot: Int) {
        if (slot in 0..8) inventory?.selectedSlot = slot
    }

    fun select(predicate: (ItemStack) -> Boolean): Boolean {
        for (i in 0..8) {
            val stack = inventory?.getItem(i) ?: continue
            if (predicate(stack)) {
                inventory?.selectedSlot = i
                return true
            }
        }
        return false
    }

    fun selectItem(itemId: String): Boolean {
        return select { it.item.descriptionId.contains(itemId, ignoreCase = true) }
    }

    fun find(predicate: (ItemStack) -> Boolean): Int {
        for (i in 0..8) {
            val stack = inventory?.getItem(i) ?: continue
            if (predicate(stack)) return i
        }
        return -1
    }

    fun findItem(itemId: String): Int {
        return find { it.item.descriptionId.contains(itemId, ignoreCase = true) }
    }

    fun getStack(slot: Int): ItemStack? {
        if (slot !in 0..8) return null
        return inventory?.getItem(slot)
    }

    fun has(predicate: (ItemStack) -> Boolean): Boolean {
        return find(predicate) != -1
    }

    fun hasItem(itemId: String): Boolean {
        return findItem(itemId) != -1
    }

    fun count(predicate: (ItemStack) -> Boolean): Int {
        return (0..8).count { i -> predicate(inventory?.getItem(i) ?: return@count false) }
    }

    fun countItem(itemId: String): Int {
        return count { it.item.descriptionId.contains(itemId, ignoreCase = true) }
    }

    fun swapWithInventory(hotbarSlot: Int, invSlot: Int) {
        if (hotbarSlot !in 0..8) return
        InventoryUtils.click(invSlot, hotbarSlot, ClickType.SWAP)
    }

    fun findAxe(): Int {
        return find { it.item is net.minecraft.world.item.AxeItem }
    }
}