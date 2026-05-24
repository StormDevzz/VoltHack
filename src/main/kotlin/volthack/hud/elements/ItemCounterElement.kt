package volthack.hud.elements

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.ItemStack
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme
import volthack.hud.HUDElement
import volthack.util.player.InventoryUtils
import volthack.util.player.HotbarUtils

class ItemCounterElement : HUDElement("ItemCounter") {
    private val customColor by color("Color", 0xFF6C63FF.toInt())

    init {
        x = 400
        y = 100
        cachedWidth = 160
        cachedHeight = 32
        enabled = false
    }

    override fun draw(ctx: GuiGraphics) {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        // Define items to count
        data class CountableItem(val item: Item, val defaultStack: ItemStack)
        val itemsList = listOf(
            CountableItem(Items.TOTEM_OF_UNDYING, ItemStack(Items.TOTEM_OF_UNDYING)),
            CountableItem(Items.OBSIDIAN, ItemStack(Items.OBSIDIAN)),
            CountableItem(Items.GOLDEN_APPLE, ItemStack(Items.GOLDEN_APPLE)),
            CountableItem(Items.ENCHANTED_GOLDEN_APPLE, ItemStack(Items.ENCHANTED_GOLDEN_APPLE)),
            CountableItem(Items.END_CRYSTAL, ItemStack(Items.END_CRYSTAL)),
            CountableItem(Items.RESPAWN_ANCHOR, ItemStack(Items.RESPAWN_ANCHOR)),
            CountableItem(Items.MACE, ItemStack(Items.MACE)),
            CountableItem(Items.TRIDENT, ItemStack(Items.TRIDENT))
        )

        // Count totals
        val counts = itemsList.map { ci ->
            var total = 0
            val inv = player.inventory
            for (i in 0 until inv.containerSize) {
                val stack = inv.getItem(i)
                if (stack.item == ci.item) {
                    total += stack.count
                }
            }
            if (player.offhandItem.item == ci.item) {
                total += player.offhandItem.count
            }
            ci to total
        }

        // Filter out items with 0 count to make a neat dynamic HUD element,
        // but if all are 0, display first 4 items as a preview in editor
        var activeItems = counts.filter { it.second > 0 }
        if (activeItems.isEmpty()) {
            activeItems = counts.take(4)
        }

        val slotWidth = 24
        val gap = 6
        cachedWidth = activeItems.size * slotWidth + (activeItems.size - 1) * gap + 12
        cachedHeight = 32

        // Draw card background
        ctx.fill(x, y, x + cachedWidth, y + cachedHeight, VoltHackTheme.surface)
        ctx.fill(x, y, x + cachedWidth, y + 1, customColor)

        var cx = x + 6
        val cy = y + 6

        for ((ci, count) in activeItems) {
            // Draw a subtle border backdrop for each item slot
            ctx.fill(cx, cy, cx + 20, cy + 20, VoltHackTheme.surfaceLight)

            // Render Item Icon
            ctx.renderItem(ci.defaultStack, cx + 2, cy + 2)
            
            // Render count text overlay
            val countStr = count.toString()
            val tw = GUIFontRenderer.width(countStr)
            val tx = cx + 20 - tw - 1f
            val ty = cy + 20 - GUIFontRenderer.height + 1f
            
            // Draw text shadow background for high contrast on item texture
            ctx.fill(tx.toInt() - 1, ty.toInt(), cx + 20, cy + 20, 0x80000000.toInt())
            GUIFontRenderer.draw(ctx, countStr, tx, ty, VoltHackTheme.textPrimary)

            cx += slotWidth + gap
        }
    }
}
