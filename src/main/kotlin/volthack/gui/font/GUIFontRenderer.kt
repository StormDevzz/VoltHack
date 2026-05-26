package volthack.gui.font

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import volthack.modules.render.FontModule

object GUIFontRenderer {
    private var customFont: CustomFont? = null
    private var loadedFontName = ""
    private var loadedFontSize = 0f

    private val font get() = Minecraft.getInstance().font

    fun reloadFont() {
        val name = FontModule.selectedFont
        val sz = FontModule.size
        if (name != loadedFontName || sz != loadedFontSize) {
            // Invalidate old font immediately so isReady() returns false while loading
            customFont = null
            loadedFontName = name
            loadedFontSize = sz
            customFont = CustomFont(name, sz)
        }
    }

    /** Returns true only when the custom font atlas texture has actually been uploaded to GPU. */
    private fun isReady(): Boolean = customFont?.isReady() == true

    fun draw(ctx: GuiGraphics, text: String, x: Float, y: Float, color: Int, shadow: Boolean = true) {
        if (FontModule.enabled && isReady()) {
            customFont!!.drawString(ctx, text, x, y, color)
        } else {
            ctx.drawString(font, text, x.toInt(), y.toInt(), color, shadow)
        }
    }

    fun drawCentered(ctx: GuiGraphics, text: String, x: Float, y: Float, color: Int, shadow: Boolean = true) {
        if (FontModule.enabled && isReady()) {
            val w = customFont!!.getWidth(text)
            customFont!!.drawString(ctx, text, x - w / 2f, y, color)
        } else {
            val w = font.width(text)
            ctx.drawString(font, text, (x - w / 2f).toInt(), y.toInt(), color, shadow)
        }
    }

    fun width(text: String): Int {
        if (FontModule.enabled && isReady()) {
            return customFont!!.getWidth(text)
        }
        return font.width(text)
    }

    val height: Int get() {
        if (FontModule.enabled && isReady()) {
            return customFont!!.lineHeight
        }
        return font.lineHeight
    }
}
