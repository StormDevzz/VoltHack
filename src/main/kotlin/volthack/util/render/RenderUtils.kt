package volthack.util.render

import net.minecraft.client.gui.GuiGraphics
import volthack.gui.theme.VoltHackTheme

object RenderUtils {

    fun rect(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int, color: Int) {
        ctx.fill(x, y, x + w, y + h, color)
    }

    fun rect(ctx: GuiGraphics, x: Float, y: Float, w: Float, h: Float, color: Int) {
        ctx.fill(x.toInt(), y.toInt(), (x + w).toInt(), (y + h).toInt(), color)
    }

    fun border(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int, color: Int, thickness: Int = 1) {
        ctx.fill(x, y, x + w, y + thickness, color)
        ctx.fill(x, y + h - thickness, x + w, y + h, color)
        ctx.fill(x, y + thickness, x + thickness, y + h - thickness, color)
        ctx.fill(x + w - thickness, y + thickness, x + w, y + h - thickness, color)
    }

    fun border(ctx: GuiGraphics, x: Float, y: Float, w: Float, h: Float, color: Int, thickness: Int = 1) {
        border(ctx, x.toInt(), y.toInt(), w.toInt(), h.toInt(), color, thickness)
    }

    fun gradientVertical(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int, topColor: Int, bottomColor: Int) {
        val steps = minOf(64, h.coerceAtLeast(1))
        val stepH = h / steps
        val rem = h % steps
        var cy = y
        for (i in 0 until steps) {
            val ratio = i.toFloat() / steps
            val color = lerpColor(topColor, bottomColor, ratio)
            val sh = stepH + (if (i < rem) 1 else 0)
            ctx.fill(x, cy, x + w, cy + sh, color)
            cy += sh
        }
    }

    fun gradientHorizontal(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int, leftColor: Int, rightColor: Int) {
        val steps = minOf(64, w.coerceAtLeast(1))
        val stepW = w / steps
        val rem = w % steps
        var cx = x
        for (i in 0 until steps) {
            val ratio = i.toFloat() / steps
            val color = lerpColor(leftColor, rightColor, ratio)
            val sw = stepW + (if (i < rem) 1 else 0)
            ctx.fill(cx, y, cx + sw, y + h, color)
            cx += sw
        }
    }

    fun roundedRect(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int, radius: Int, color: Int) {
        ctx.fill(x + radius, y, x + w - radius, y + h, color)
        ctx.fill(x, y + radius, x + w, y + h - radius, color)
        for (i in 0 until radius) {
            val d = radius - i
            val offset = kotlin.math.sqrt((radius * radius - d * d).toFloat()).toInt()
            ctx.fill(x + radius - offset, y + i, x + radius + offset, y + i + 1, color)
            ctx.fill(x + w - radius - offset, y + i, x + w - radius + offset, y + i + 1, color)
            ctx.fill(x + radius - offset, y + h - i - 1, x + radius + offset, y + h - i, color)
            ctx.fill(x + w - radius - offset, y + h - i - 1, x + w - radius + offset, y + h - i, color)
        }
    }

    fun roundedBorder(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int, radius: Int, color: Int, thickness: Int = 1) {
        for (t in 0 until thickness) {
            val rx = x + t; val ry = y + t; val rw = w - t * 2; val rh = h - t * 2
            val r = (radius - t).coerceAtLeast(0)
            roundedRect(ctx, rx, ry, rw, rh, r, color)
        }
    }

    fun circle(ctx: GuiGraphics, cx: Int, cy: Int, radius: Int, color: Int) {
        for (dy in -radius..radius) {
            val dx = kotlin.math.sqrt((radius * radius - dy * dy).toFloat()).toInt()
            ctx.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color)
        }
    }

    fun line(ctx: GuiGraphics, x1: Int, y1: Int, x2: Int, y2: Int, thickness: Int, color: Int) {
        val dx = x2 - x1; val dy = y2 - y1
        val len = kotlin.math.sqrt((dx * dx + dy * dy).toFloat()).toInt().coerceAtLeast(1)
        for (i in 0..len) {
            val t = i.toFloat() / len
            val px = (x1 + dx * t).toInt()
            val py = (y1 + dy * t).toInt()
            ctx.fill(px - thickness / 2, py - thickness / 2, px + thickness / 2 + 1, py + thickness / 2 + 1, color)
        }
    }

    fun glow(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int, color: Int, radius: Int = 8) {
        for (i in 1..radius) {
            val a = ((1.0f - i.toFloat() / radius) * 0.3f * ((color shr 24) and 0xFF) / 255f).toInt().coerceIn(0, 255)
            if (a == 0) continue
            val c = (a shl 24) or (color and 0x00FFFFFF)
            border(ctx, x - i, y - i, w + i * 2, h + i * 2, c)
        }
    }

    fun scissor(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int, block: () -> Unit) {
        ctx.enableScissor(x, y, x + w, y + h)
        block()
        ctx.disableScissor()
    }

    fun lerpColor(c1: Int, c2: Int, t: Float): Int {
        val a = lerp((c1 shr 24) and 0xFF, (c2 shr 24) and 0xFF, t)
        val r = lerp((c1 shr 16) and 0xFF, (c2 shr 16) and 0xFF, t)
        val g = lerp((c1 shr 8) and 0xFF, (c2 shr 8) and 0xFF, t)
        val b = (c1 and 0xFF) + (((c2 and 0xFF) - (c1 and 0xFF)) * t).toInt()
        return (a.toInt() shl 24) or (r.toInt() shl 16) or (g.toInt() shl 8) or b
    }

    private fun lerp(a: Int, b: Int, t: Float): Float = a + (b - a) * t

    fun withAlpha(color: Int, alpha: Int): Int {
        val a = alpha.coerceIn(0, 255)
        return (a shl 24) or (color and 0x00FFFFFF)
    }

    fun darker(color: Int, factor: Float = 0.7f): Int {
        val r = (((color shr 16) and 0xFF) * factor).toInt()
        val g = (((color shr 8) and 0xFF) * factor).toInt()
        val b = ((color and 0xFF) * factor).toInt()
        return (color and 0xFF000000.toInt()) or (r shl 16) or (g shl 8) or b
    }

    fun lighter(color: Int, factor: Float = 1.3f): Int {
        val r = (((color shr 16) and 0xFF) * factor).toInt().coerceAtMost(255)
        val g = (((color shr 8) and 0xFF) * factor).toInt().coerceAtMost(255)
        val b = ((color and 0xFF) * factor).toInt().coerceAtMost(255)
        return (color and 0xFF000000.toInt()) or (r shl 16) or (g shl 8) or b
    }

    fun rainbow(offset: Float = 0f, speed: Float = 0.005f): Int {
        val hue = (System.currentTimeMillis() * speed + offset) % 1.0f
        return java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f)
    }

    fun panel(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int) {
        rect(ctx, x, y, w, h, VoltHackTheme.surface)
        border(ctx, x, y, w, h, VoltHackTheme.border)
    }

    fun accentLine(ctx: GuiGraphics, x: Int, y: Int, w: Int) {
        ctx.fill(x, y, x + w, y + 2, VoltHackTheme.accent)
    }
}