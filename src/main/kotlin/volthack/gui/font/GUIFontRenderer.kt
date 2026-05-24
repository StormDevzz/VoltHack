package volthack.gui.font

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

object GUIFontRenderer {
    private val font get() = Minecraft.getInstance().font

    fun draw(ctx: GuiGraphics, text: String, x: Float, y: Float, color: Int, shadow: Boolean = true) {
        ctx.drawString(font, text, x.toInt(), y.toInt(), color, shadow)
    }

    fun drawCentered(ctx: GuiGraphics, text: String, x: Float, y: Float, color: Int, shadow: Boolean = true) {
        val w = font.width(text)
        ctx.drawString(font, text, (x - w / 2f).toInt(), y.toInt(), color, shadow)
    }

    fun width(text: String): Int = font.width(text)

    val height: Int get() = font.lineHeight
}
