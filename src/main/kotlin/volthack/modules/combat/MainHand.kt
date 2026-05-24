package volthack.modules.combat

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.boss.enderdragon.EndCrystal
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.Items
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.player.InventoryUtils

object MainHand : Module("MainHand", "Manages your main hand slot automatically (AutoTotem & AutoSword)", Category.COMBAT) {
    private val health by float("Health", 8.0f, 1.0f, 20.0f, 0.5f)
    private val fallDistance by float("Fall Distance", 8.0f, 1.0f, 20.0f, 0.5f)
    private val crystalCheck by boolean("Crystal Check", true, "Force Totem in mainhand when crystals are nearby")
    private val crystalRange by float("Crystal Range", 6.0f, 1.0f, 12.0f, 0.5f)
    private val autoSword by boolean("Auto Sword", true, "Automatically swap to weapon when attacking")
    private val delay by int("Delay (ms)", 50, 0, 500, "Delay in milliseconds between swaps")

    private var lastSwapTime = 0L

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val world = mc.level ?: return

        // Do not swap items if player is in an inventory screen
        if (mc.screen != null) return

        val time = System.currentTimeMillis()

        // 1. Determine if we are in severe danger (need a Totem in Main Hand)
        val hp = player.health + player.absorptionAmount
        val isLowHp = hp <= health
        val isFalling = player.fallDistance >= fallDistance && !player.isFallFlying
        val isCrystalNear = crystalCheck && (world.entitiesForRendering() as Iterable<Entity>)
            .filterIsInstance<EndCrystal>()
            .any { player.distanceTo(it) <= crystalRange }

        val severeDanger = isLowHp || isFalling || isCrystalNear

        if (severeDanger) {
            if (time - lastSwapTime < delay.toLong()) return
            
            // Check if we are already holding a Totem in main hand
            if (player.mainHandItem.item == Items.TOTEM_OF_UNDYING) return

            // Search for totem in inventory
            val slot = InventoryUtils.findFirst { it.item == Items.TOTEM_OF_UNDYING }
            if (slot != -1) {
                if (slot in 0..8) {
                    // Silently select the hotbar slot
                    player.inventory.selectedSlot = slot
                } else {
                    // Swap with currently selected slot
                    val currentSelectedContainerSlot = player.inventory.selectedSlot + 36
                    InventoryUtils.click(slot, 0, ClickType.PICKUP)
                    InventoryUtils.click(currentSelectedContainerSlot, 0, ClickType.PICKUP)
                    InventoryUtils.click(slot, 0, ClickType.PICKUP)
                    lastSwapTime = time
                }
            }
            return
        }

        // 2. Auto Sword logic during safety
        if (autoSword) {
            val hasTarget = KillAura.currentTarget != null || mc.crosshairPickEntity != null
            if (hasTarget) {
                // Find best weapon in the hotbar
                val bestWeaponSlot = (0..8)
                    .map { i -> i to player.inventory.getItem(i) }
                    .filter { (_, stack) ->
                        val desc = stack.item.descriptionId.lowercase()
                        desc.contains("sword") || desc.contains("axe")
                    }
                    .maxByOrNull { (_, stack) ->
                        val desc = stack.item.descriptionId.lowercase()
                        val isSword = desc.contains("sword")
                        var dmg = if (isSword) 6.0 else 5.0
                        if (desc.contains("netherite")) dmg += 4.0
                        else if (desc.contains("diamond")) dmg += 3.0
                        else if (desc.contains("iron")) dmg += 2.0
                        else if (desc.contains("stone")) dmg += 1.0
                        dmg
                    }?.first

                if (bestWeaponSlot != null && bestWeaponSlot != player.inventory.selectedSlot) {
                    player.inventory.selectedSlot = bestWeaponSlot
                }
            }
        }
    }
}
