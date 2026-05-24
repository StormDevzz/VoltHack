package volthack.hud.elements

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack
import volthack.gui.theme.VoltHackTheme
import volthack.hud.HUDElement

class HotbarElement : HUDElement("Hotbar") {
    private val customColor by color("Color", 0xFF6C63FF.toInt())

    init {
        x = 300
        y = 500
        cachedWidth = 208
        cachedHeight = 22
        enabled = false
    }

    override fun draw(ctx: GuiGraphics) {
        val mc = Minecraft.getInstance()
        val player = mc.player

        cachedWidth = 208
        cachedHeight = 24

        // Draw card background
        ctx.fill(x, y, x + cachedWidth, y + cachedHeight, VoltHackTheme.surface)
        ctx.fill(x, y, x + cachedWidth, y + 1, customColor)

        // Draw 9 slots
        for (i in 0..8) {
            val slotX = x + i * 20 + 2
            val slotY = y + 2
            
            // Draw background for slot
            ctx.fill(slotX, slotY, slotX + 18, slotY + 20, VoltHackTheme.surfaceLight)

            // Draw current active slot highlight
            if (player != null && player.inventory.selectedSlot == i) {
                val borderThickness = 1
                ctx.fill(slotX, slotY, slotX + 18, slotY + borderThickness, customColor)
                ctx.fill(slotX, slotY + 20 - borderThickness, slotX + 18, slotY + 20, customColor)
                ctx.fill(slotX, slotY, slotX + borderThickness, slotY + 20, customColor)
                ctx.fill(slotX + 18 - borderThickness, slotY, slotX + 18, slotY + 20, customColor)
            }

            if (player != null) {
                val stack = player.inventory.getItem(i)
                if (!stack.isEmpty) {
                    ctx.renderItem(stack, slotX + 1, slotY + 2)
                    ctx.renderItemDecorations(mc.font, stack, slotX + 1, slotY + 2)
                }
            }
        }

        // Draw Offhand Slot
        val offhandX = x + 182 + 6
        val offhandY = y + 2
        
        // Background for offhand slot
        ctx.fill(offhandX, offhandY, offhandX + 18, offhandY + 20, VoltHackTheme.surfaceLight)

        // Draw a subtle border for the offhand slot to distinguish it
        val offhandBorderColor = (customColor and 0x00FFFFFF) or 0x66000000 // 40% opacity customColor
        val borderThickness = 1
        ctx.fill(offhandX, offhandY, offhandX + 18, offhandY + borderThickness, offhandBorderColor.toInt())
        ctx.fill(offhandX, offhandY + 20 - borderThickness, offhandX + 18, offhandY + 20, offhandBorderColor.toInt())
        ctx.fill(offhandX, offhandY, offhandX + borderThickness, offhandY + 20, offhandBorderColor.toInt())
        ctx.fill(offhandX + 18 - borderThickness, offhandY, offhandX + 18, offhandY + 20, offhandBorderColor.toInt())

        if (player != null) {
            val offhandStack = player.offhandItem
            if (!offhandStack.isEmpty) {
                ctx.renderItem(offhandStack, offhandX + 1, offhandY + 2)
                ctx.renderItemDecorations(mc.font, offhandStack, offhandX + 1, offhandY + 2)
            }
        }
    }
}
