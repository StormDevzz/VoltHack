package volthack.modules.player

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.Items
import volthack.setting.Category
import volthack.setting.Module

object ElytraSwap : Module("ElytraSwap", "Swaps chestplate with elytra", Category.PLAYER) {
    private val mode by mode("Mode", listOf("Swap", "EquipElytra", "EquipChestplate"), "Swap")

    override fun onEnable() {
        val player = Minecraft.getInstance().player ?: return
        val inv = player.inventory

        val chestItem = player.getItemBySlot(EquipmentSlot.CHEST)

        when (mode) {
            "Swap" -> {
                val elytraSlot = (0..35).indexOfFirst { inv.getItem(it).item == Items.ELYTRA }
                val chestSlot = (0..35).indexOfFirst { inv.getItem(it).item == Items.NETHERITE_CHESTPLATE || inv.getItem(it).item == Items.DIAMOND_CHESTPLATE }

                if (chestItem.item == Items.ELYTRA && chestSlot != -1) {
                    swap(elytraSlot, chestSlot)
                } else if (chestItem.item != Items.ELYTRA && elytraSlot != -1) {
                    swap(elytraSlot, chestSlot)
                }
            }
            "EquipElytra" -> {
                val slot = (0..35).indexOfFirst { inv.getItem(it).item == Items.ELYTRA }
                if (slot >= 0) equip(slot)
            }
            "EquipChestplate" -> {
                val slot = (0..35).indexOfFirst {
                    inv.getItem(it).item == Items.NETHERITE_CHESTPLATE || inv.getItem(it).item == Items.DIAMOND_CHESTPLATE
                }
                if (slot >= 0) equip(slot)
            }
        }

        disable()
    }

    private fun swap(from: Int, to: Int) {
        val mc = Minecraft.getInstance()
        mc.gameMode?.handleInventoryMouseClick(mc.player!!.containerMenu.containerId, from, to, ClickType.SWAP, mc.player!!)
    }

    private fun equip(slot: Int) {
        val mc = Minecraft.getInstance()
        val invSlot = if (slot < 9) slot + 36 else slot
        mc.gameMode?.handleInventoryMouseClick(mc.player!!.containerMenu.containerId, invSlot, 0, ClickType.QUICK_MOVE, mc.player!!)
    }
}
