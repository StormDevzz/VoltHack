package volthack.util.render

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import volthack.gui.font.GUIFontRenderer

object FontUtils {
    private val font get() = Minecraft.getInstance().font

    fun draw(ctx: GuiGraphics, text: String, x: Float, y: Float, color: Int, shadow: Boolean = true) {
        GUIFontRenderer.draw(ctx, text, x, y, color, shadow)
    }

    fun draw(ctx: GuiGraphics, text: Component, x: Float, y: Float, color: Int, shadow: Boolean = true) {
        GUIFontRenderer.draw(ctx, text.string, x, y, color, shadow)
    }

    fun drawCentered(ctx: GuiGraphics, text: String, x: Float, y: Float, color: Int, shadow: Boolean = true) {
        GUIFontRenderer.drawCentered(ctx, text, x, y, color, shadow)
    }

    fun drawCentered(ctx: GuiGraphics, text: Component, x: Float, y: Float, color: Int, shadow: Boolean = true) {
        GUIFontRenderer.drawCentered(ctx, text.string, x, y, color, shadow)
    }

    fun drawWithShadow(ctx: GuiGraphics, text: String, x: Float, y: Float, color: Int, shadowColor: Int = 0x80000000.toInt()) {
        GUIFontRenderer.draw(ctx, text, x, y, color, true)
    }

    fun drawOutlined(ctx: GuiGraphics, text: String, x: Float, y: Float, color: Int, outlineColor: Int) {
        GUIFontRenderer.draw(ctx, text, x, y, color, false)
    }

    fun drawRainbow(ctx: GuiGraphics, text: String, x: Float, y: Float, offset: Float = 0f) {
        var cx = x
        for ((i, c) in text.withIndex()) {
            val color = RenderUtils.rainbow(offset + i * 0.1f, speed = 0.003f)
            GUIFontRenderer.draw(ctx, c.toString(), cx, y, color, true)
            cx += GUIFontRenderer.width(c.toString())
        }
    }

    fun drawGradientText(ctx: GuiGraphics, text: String, x: Float, y: Float, startColor: Int, endColor: Int) {
        var cx = x
        val len = text.length.coerceAtLeast(1)
        for ((i, c) in text.withIndex()) {
            val t = i.toFloat() / (len - 1)
            val color = RenderUtils.lerpColor(startColor, endColor, t)
            GUIFontRenderer.draw(ctx, c.toString(), cx, y, color, true)
            cx += GUIFontRenderer.width(c.toString())
        }
    }

    fun drawScrolling(ctx: GuiGraphics, text: String, x: Float, y: Float, w: Int, color: Int, speed: Float = 1f) {
        val tw = GUIFontRenderer.width(text)
        if (tw <= w) {
            GUIFontRenderer.draw(ctx, text, x, y, color, true)
            return
        }
        val elapsed = System.currentTimeMillis() * speed * 0.01f
        val offset = (elapsed % (tw + w)).toInt()
        ctx.enableScissor(x.toInt(), y.toInt(), x.toInt() + w, y.toInt() + GUIFontRenderer.height)
        GUIFontRenderer.draw(ctx, text, x - offset, y, color, true)
        GUIFontRenderer.draw(ctx, text, x - offset + tw + w, y, color, true)
        ctx.disableScissor()
    }

    fun width(text: String): Int = GUIFontRenderer.width(text)
    fun width(text: Component): Int = GUIFontRenderer.width(text.string)

    val height: Int get() = GUIFontRenderer.height

    fun trimToWidth(text: String, maxWidth: Int): String {
        if (GUIFontRenderer.width(text) <= maxWidth) return text
        for (i in text.indices) {
            if (GUIFontRenderer.width(text.substring(0, i + 1)) > maxWidth) {
                return text.substring(0, i) + "…"
            }
        }
        return text
    }

    fun colored(text: String, color: Int): MutableComponent {
        return Component.literal(text).setStyle(Style.EMPTY.withColor(color))
    }

    fun bold(text: String): MutableComponent {
        return Component.literal(text).setStyle(Style.EMPTY.withBold(true))
    }

    fun italic(text: String): MutableComponent {
        return Component.literal(text).setStyle(Style.EMPTY.withItalic(true))
    }
}