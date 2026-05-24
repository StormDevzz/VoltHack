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

object OffHand : Module("OffHand", "Manages your offhand slot automatically (AutoTotem)", Category.COMBAT) {
    private val health by float("Health", 10.0f, 1.0f, 20.0f, 0.5f)
    private val defaultItem by mode("Default", listOf("Totem", "Shield", "Gapple", "Crystal"), "Shield")
    private val rightClickGapple by boolean("RC Gapple", true, "Swaps to Gapple when holding right-click with a sword")
    private val fallDistance by float("Fall Distance", 5.0f, 1.0f, 20.0f, 0.5f)
    private val crystalCheck by boolean("Crystal Check", true, "Force Totem if crystals are nearby")
    private val crystalRange by float("Crystal Range", 6.0f, 1.0f, 12.0f, 0.5f)
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
        if (time - lastSwapTime < delay.toLong()) return

        // 1. Determine if we are in danger
        val hp = player.health + player.absorptionAmount
        val isLowHp = hp <= health
        
        val isFalling = player.fallDistance >= fallDistance && !player.isFallFlying
        
        val isCrystalNear = crystalCheck && (world.entitiesForRendering() as Iterable<Entity>)
            .filterIsInstance<EndCrystal>()
            .any { player.distanceTo(it) <= crystalRange }

        val inDanger = isLowHp || isFalling || isCrystalNear

        // 2. Decide target item
        val targetItemName = if (inDanger) {
            "Totem"
        } else {
            val isRightClickHeld = mc.options.keyUse.isDown
            val holdingSword = player.mainHandItem.item.descriptionId.contains("sword", ignoreCase = true)
            if (rightClickGapple && isRightClickHeld && holdingSword) {
                "Gapple"
            } else {
                defaultItem
            }
        }

        val targetItem = when (targetItemName) {
            "Totem" -> Items.TOTEM_OF_UNDYING
            "Shield" -> Items.SHIELD
            "Gapple" -> Items.GOLDEN_APPLE // We will search for enchanted first, then regular
            "Crystal" -> Items.END_CRYSTAL
            else -> Items.TOTEM_OF_UNDYING
        }

        // 3. Check what is currently in offhand
        val currentOffhand = player.offhandItem
        if (currentOffhand.item == targetItem || (targetItemName == "Gapple" && (currentOffhand.item == Items.GOLDEN_APPLE || currentOffhand.item == Items.ENCHANTED_GOLDEN_APPLE))) {
            return
        }

        // 4. Find the item in our inventory
        val slot = if (targetItemName == "Gapple") {
            findGapple()
        } else {
            InventoryUtils.findFirst { it.item == targetItem }
        }

        if (slot == -1) return

        // 5. Swap the item into the offhand slot
        val containerSlot = if (slot in 0..8) slot + 36 else slot
        
        InventoryUtils.click(containerSlot, 0, ClickType.PICKUP)
        InventoryUtils.click(45, 0, ClickType.PICKUP)
        InventoryUtils.click(containerSlot, 0, ClickType.PICKUP)

        lastSwapTime = time
    }

    private fun findGapple(): Int {
        val enchanted = InventoryUtils.findFirst { it.item == Items.ENCHANTED_GOLDEN_APPLE }
        if (enchanted != -1) return enchanted
        return InventoryUtils.findFirst { it.item == Items.GOLDEN_APPLE }
    }
}
