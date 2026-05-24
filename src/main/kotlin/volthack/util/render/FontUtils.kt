package volthack.util.render

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style

object FontUtils {
    private val font get() = Minecraft.getInstance().font

    fun draw(ctx: GuiGraphics, text: String, x: Float, y: Float, color: Int, shadow: Boolean = true) {
        ctx.drawString(font, text, x.toInt(), y.toInt(), color, shadow)
    }

    fun draw(ctx: GuiGraphics, text: Component, x: Float, y: Float, color: Int, shadow: Boolean = true) {
        ctx.drawString(font, text, x.toInt(), y.toInt(), color, shadow)
    }

    fun drawCentered(ctx: GuiGraphics, text: String, x: Float, y: Float, color: Int, shadow: Boolean = true) {
        val w = font.width(text)
        ctx.drawString(font, text, (x - w / 2f).toInt(), y.toInt(), color, shadow)
    }

    fun drawCentered(ctx: GuiGraphics, text: Component, x: Float, y: Float, color: Int, shadow: Boolean = true) {
        val w = font.width(text)
        ctx.drawString(font, text, (x - w / 2f).toInt(), y.toInt(), color, shadow)
    }

    fun drawWithShadow(ctx: GuiGraphics, text: String, x: Float, y: Float, color: Int, shadowColor: Int = 0x80000000.toInt()) {
        ctx.drawString(font, text, x.toInt() + 1, y.toInt() + 1, shadowColor, false)
        ctx.drawString(font, text, x.toInt(), y.toInt(), color, false)
    }

    fun drawOutlined(ctx: GuiGraphics, text: String, x: Float, y: Float, color: Int, outlineColor: Int) {
        for (dx in -1..1) for (dy in -1..1) {
            if (dx == 0 && dy == 0) continue
            ctx.drawString(font, text, x.toInt() + dx, y.toInt() + dy, outlineColor, false)
        }
        ctx.drawString(font, text, x.toInt(), y.toInt(), color, false)
    }

    fun drawRainbow(ctx: GuiGraphics, text: String, x: Float, y: Float, offset: Float = 0f) {
        var cx = x
        for ((i, c) in text.withIndex()) {
            val color = RenderUtils.rainbow(offset + i * 0.1f, speed = 0.003f)
            ctx.drawString(font, c.toString(), cx.toInt(), y.toInt(), color, true)
            cx += font.width(c.toString())
        }
    }

    fun drawGradientText(ctx: GuiGraphics, text: String, x: Float, y: Float, startColor: Int, endColor: Int) {
        var cx = x
        val len = text.length.coerceAtLeast(1)
        for ((i, c) in text.withIndex()) {
            val t = i.toFloat() / (len - 1)
            val color = RenderUtils.lerpColor(startColor, endColor, t)
            ctx.drawString(font, c.toString(), cx.toInt(), y.toInt(), color, true)
            cx += font.width(c.toString())
        }
    }

    fun drawScrolling(ctx: GuiGraphics, text: String, x: Float, y: Float, w: Int, color: Int, speed: Float = 1f) {
        val tw = font.width(text)
        if (tw <= w) {
            ctx.drawString(font, text, x.toInt(), y.toInt(), color, true)
            return
        }
        val elapsed = System.currentTimeMillis() * speed * 0.01f
        val offset = (elapsed % (tw + w)).toInt()
        ctx.enableScissor(x.toInt(), y.toInt(), x.toInt() + w, y.toInt() + font.lineHeight)
        ctx.drawString(font, text, (x - offset).toInt(), y.toInt(), color, true)
        ctx.drawString(font, text, (x - offset + tw + w).toInt(), y.toInt(), color, true)
        ctx.disableScissor()
    }

    fun width(text: String): Int = font.width(text)
    fun width(text: Component): Int = font.width(text)

    val height: Int get() = font.lineHeight

    fun trimToWidth(text: String, maxWidth: Int): String {
        if (font.width(text) <= maxWidth) return text
        for (i in text.indices) {
            if (font.width(text.substring(0, i + 1)) > maxWidth) {
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