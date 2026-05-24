package volthack.hud.elements

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import volthack.gui.theme.VoltHackTheme
import volthack.hud.HUDElement

class InvPreviewElement : HUDElement("InvPreview") {
    private val customColor by color("Color", 0xFF6C63FF.toInt())
    private val showTitle by boolean("Show Title", true)

    init {
        x = 200
        y = 200
        cachedWidth = 188
        cachedHeight = 68
        enabled = false
    }

    override fun draw(ctx: GuiGraphics) {
        val mc = Minecraft.getInstance()
        val player = mc.player

        val titleH = if (showTitle) 14 else 0
        cachedWidth = 188
        cachedHeight = 68 + titleH

        // Draw card background
        ctx.fill(x, y, x + cachedWidth, y + cachedHeight, VoltHackTheme.surface)
        ctx.fill(x, y, x + cachedWidth, y + 1, customColor)

        if (showTitle) {
            volthack.gui.font.GUIFontRenderer.draw(
                ctx,
                "Inventory",
                (x + 6).toFloat(),
                (y + 3).toFloat(),
                VoltHackTheme.textSecondary
            )
        }

        val startY = y + 4 + titleH
        for (row in 0..2) {
            for (col in 0..8) {
                val slotX = x + col * 20 + 4
                val slotY = startY + row * 20
                ctx.fill(slotX, slotY, slotX + 18, slotY + 18, VoltHackTheme.surfaceLight)

                if (player != null) {
                    val slotIndex = 9 + row * 9 + col
                    val stack = player.inventory.getItem(slotIndex)
                    if (!stack.isEmpty) {
                        ctx.renderItem(stack, slotX + 1, slotY + 1)
                        ctx.renderItemDecorations(mc.font, stack, slotX + 1, slotY + 1)
                    }
                }
            }
        }
    }
}
