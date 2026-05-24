package volthack.hud.elements

import net.minecraft.client.gui.GuiGraphics
import volthack.VoltHack
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme
import volthack.hud.HUDElement

class WatermarkElement : HUDElement("Watermark") {
    private val text = "VoltHack v${VoltHack.version}"
    private var tw = 0

    init {
        x = 4
        y = 4
        enabled = false
    }

    override fun render(ctx: GuiGraphics) {
        if (tw == 0) {
            tw = GUIFontRenderer.width(text)
            cachedWidth = tw + 12
            cachedHeight = GUIFontRenderer.height + 8
        }
        ctx.fill(x, y, x + cachedWidth, y + cachedHeight, VoltHackTheme.surface)
        ctx.fill(x, y, x + cachedWidth, y + 1, VoltHackTheme.accent)
        GUIFontRenderer.drawCentered(
            ctx, text,
            (x + cachedWidth / 2f), (y + (cachedHeight - GUIFontRenderer.height) / 2f),
            VoltHackTheme.accent
        )
    }
}
