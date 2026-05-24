package volthack.hud.elements

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme
import volthack.hud.HUDElement

class FPSElement : HUDElement("FPS") {
    private val customColor by color("Color", 0xFF6C63FF.toInt())

    init {
        x = 4
        y = 35
        cachedWidth = 60
        cachedHeight = 16
        enabled = false
    }

    override fun draw(ctx: GuiGraphics) {
        val mc = Minecraft.getInstance()
        val fpsText = "FPS: ${mc.fps}"
        
        cachedWidth = GUIFontRenderer.width(fpsText) + 12
        cachedHeight = GUIFontRenderer.height + 8

        // Draw card background
        ctx.fill(x, y, x + cachedWidth, y + cachedHeight, VoltHackTheme.surface)
        ctx.fill(x, y, x + cachedWidth, y + 1, customColor)

        val tx = x + 6
        val ty = y + (cachedHeight - GUIFontRenderer.height) / 2f

        GUIFontRenderer.draw(ctx, fpsText, tx.toFloat(), ty, customColor)
    }
}
