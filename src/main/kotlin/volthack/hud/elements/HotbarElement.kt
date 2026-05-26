package volthack.hud.elements

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack
import volthack.gui.theme.VoltHackTheme
import volthack.hud.HUDElement

class HotbarElement : HUDElement("Hotbar") {
    private val customColor by color("Color", 0xFF6C63FF.toInt())
    private var smoothSlotX = -1f

    init {
        x = 300
        y = 500
        cachedWidth = 208
        cachedHeight = 24
        enabled = false
    }

    override fun draw(ctx: GuiGraphics) {
        val mc = Minecraft.getInstance()
        val player = mc.player

        cachedWidth = 208
        cachedHeight = 24

        // Glassmorphic border glow base
        val glowColor = (0x33000000.toLong() or (customColor.toLong() and 0x00FFFFFF)).toInt()
        ctx.fill(x - 2, y - 2, x + cachedWidth + 2, y + cachedHeight + 2, glowColor)
        ctx.fill(x - 1, y - 1, x + cachedWidth + 1, y + cachedHeight + 1, customColor)
        ctx.fill(x, y, x + cachedWidth, y + cachedHeight, 0xEE0D0D1A.toInt())

        // Draw 9 slots backdrop
        for (i in 0..8) {
            val slotX = x + i * 20 + 2
            val slotY = y + 2
            ctx.fill(slotX, slotY, slotX + 18, slotY + 20, 0x1AFFFFFF.toInt())
        }

        // Draw active slot with gorgeous smooth sliding glow highlight
        if (player != null) {
            val targetX = (player.inventory.selectedSlot * 20 + 2).toFloat()
            if (smoothSlotX < 0f) {
                smoothSlotX = targetX
            } else {
                smoothSlotX += (targetX - smoothSlotX) * 0.22f
            }

            val highlightX = x + smoothSlotX.toInt()
            val slotY = y + 2
            val borderThickness = 1
            
            // Draw active neon border around sliding slot position
            ctx.fill(highlightX, slotY, highlightX + 18, slotY + borderThickness, customColor)
            ctx.fill(highlightX, slotY + 20 - borderThickness, highlightX + 18, slotY + 20, customColor)
            ctx.fill(highlightX, slotY, highlightX + borderThickness, slotY + 20, customColor)
            ctx.fill(highlightX + 18 - borderThickness, slotY, highlightX + 18, slotY + 20, customColor)
            
            // Draw active subtle background fill
            val activeFillColor = (0x24000000.toLong() or (customColor.toLong() and 0x00FFFFFF)).toInt()
            ctx.fill(highlightX + borderThickness, slotY + borderThickness, highlightX + 18 - borderThickness, slotY + 20 - borderThickness, activeFillColor)
        }

        // Render actual inventory items in slots
        for (i in 0..8) {
            val slotX = x + i * 20 + 2
            val slotY = y + 2
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
        
        ctx.fill(offhandX, offhandY, offhandX + 18, offhandY + 20, 0x1AFFFFFF.toInt())

        // Subtly border the offhand slot to match the custom color
        val offhandBorderColor = (0x66000000.toLong() or (customColor.toLong() and 0x00FFFFFF)).toInt()
        val borderThickness = 1
        ctx.fill(offhandX, offhandY, offhandX + 18, offhandY + borderThickness, offhandBorderColor)
        ctx.fill(offhandX, offhandY + 20 - borderThickness, offhandX + 18, offhandY + 20, offhandBorderColor)
        ctx.fill(offhandX, offhandY, offhandX + borderThickness, offhandY + 20, offhandBorderColor)
        ctx.fill(offhandX + 18 - borderThickness, offhandY, offhandX + 18, offhandY + 20, offhandBorderColor)

        if (player != null) {
            val offhandStack = player.offhandItem
            if (!offhandStack.isEmpty) {
                ctx.renderItem(offhandStack, offhandX + 1, offhandY + 2)
                ctx.renderItemDecorations(mc.font, offhandStack, offhandX + 1, offhandY + 2)
            }
        }
    }
}
