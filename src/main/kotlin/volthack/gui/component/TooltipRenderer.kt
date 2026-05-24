package volthack.gui.component

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme

object TooltipRenderer {
    private val mc get() = Minecraft.getInstance()

    fun render(ctx: GuiGraphics, text: String, mouseX: Int, mouseY: Int) {
        if (text.isEmpty()) return

        val tw = GUIFontRenderer.width(text)
        val pad = 6
        val w = tw + pad * 2
        val h = 18
        var x = mouseX + 10
        var y = mouseY + 8

        if (x + w > mc.window.guiScaledWidth) x = mouseX - w - 6
        if (y + h > mc.window.guiScaledHeight) y = mouseY - h - 6

        ctx.fill(x, y, x + w, y + h, 0xCC16162A.toInt())
        ctx.fill(x, y + h - 1, x + w, y + h, VoltHackTheme.accent)
        GUIFontRenderer.draw(ctx, text, (x + pad).toFloat(), (y + (h - GUIFontRenderer.height) / 2f).toFloat(), VoltHackTheme.textPrimary)
    }
}
