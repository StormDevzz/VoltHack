package volthack.gui.component

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme

object TooltipRenderer {
    private val mc get() = Minecraft.getInstance()
    private var lastText = ""
    private var alpha = 0.0f
    private var lastMouseX = 0
    private var lastMouseY = 0

    fun render(ctx: GuiGraphics, text: String, mouseX: Int, mouseY: Int) {
        val targetAlpha = if (text.isNotEmpty()) 1.0f else 0.0f
        
        if (text.isNotEmpty()) {
            if (text != lastText) {
                lastText = text
                if (alpha < 0.1f) alpha = 0.0f
            }
            lastMouseX = mouseX
            lastMouseY = mouseY
        }

        alpha += (targetAlpha - alpha) * 0.18f

        val aInt = (alpha * 255).toInt().coerceIn(0, 255)
        if (aInt <= 5 || lastText.isEmpty()) return

        val tw = GUIFontRenderer.width(lastText)
        val pad = 6
        val w = tw + pad * 2
        val h = 18
        var x = lastMouseX + 10
        var y = lastMouseY + 8

        if (x + w > mc.window.guiScaledWidth) x = lastMouseX - w - 6
        if (y + h > mc.window.guiScaledHeight) y = lastMouseY - h - 6

        // Compute colors using interpolated alpha channel
        val bgAlpha = (((0xCC * alpha).toInt() and 0xFF) shl 24)
        val bgColor = bgAlpha or (VoltHackTheme.surface and 0x00FFFFFF)
        
        val accentAlpha = (((0xFF * alpha).toInt() and 0xFF) shl 24)
        val accentColor = accentAlpha or (VoltHackTheme.accent and 0x00FFFFFF)
        
        val textAlpha = (((0xFF * alpha).toInt() and 0xFF) shl 24)
        val textColor = textAlpha or (VoltHackTheme.textPrimary and 0x00FFFFFF)

        ctx.fill(x, y, x + w, y + h, bgColor)
        ctx.fill(x, y + h - 1, x + w, y + h, accentColor)
        GUIFontRenderer.draw(ctx, lastText, (x + pad).toFloat(), (y + (h - GUIFontRenderer.height) / 2f).toFloat(), textColor)
    }
}
