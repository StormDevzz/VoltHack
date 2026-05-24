package volthack.gui.component

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme
import volthack.setting.Setting
import kotlin.math.roundToInt

object SettingWidget {
    private var dragging: Dragging? = null

    private data class Dragging(
        val setting: Setting<*>,
        val startMouseX: Int,
        val startValue: Float,
        val sliderX: Int,
        val sliderW: Int
    )

    fun render(ctx: GuiGraphics, setting: Setting<*>, x: Int, y: Int, width: Int) {
        if (!setting.isVisible()) return
        when (setting) {
            is Setting.Boolean -> renderBoolean(ctx, setting, x, y, width)
            is Setting.Float -> renderFloat(ctx, setting, x, y, width)
            is Setting.Int -> renderInt(ctx, setting, x, y, width)
            is Setting.Mode -> renderMode(ctx, setting, x, y, width)
            is Setting.Color -> renderColor(ctx, setting, x, y, width)
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, setting: Setting<*>, x: Int, y: Int, width: Int, button: Int = 0): Boolean {
        if (!setting.isVisible()) return false
        return when (setting) {
            is Setting.Boolean -> clickBoolean(mouseX, mouseY, setting, x, y, width)
            is Setting.Float -> clickFloat(mouseX, mouseY, setting, x, y, width)
            is Setting.Int -> clickInt(mouseX, mouseY, setting, x, y, width)
            is Setting.Mode -> clickMode(mouseX, mouseY, setting, x, y, width, button)
            else -> false
        }
    }

    fun mouseReleased(): Boolean {
        if (dragging != null) {
            dragging = null
            return true
        }
        return false
    }

    private fun renderBoolean(ctx: GuiGraphics, s: Setting.Boolean, x: Int, y: Int, width: Int) {
        val color = if (s.value) VoltHackTheme.accent else VoltHackTheme.textDisabled
        ctx.fill(x, y, x + width, y + 26, 0x10000000.toInt())
        GUIFontRenderer.draw(ctx, s.name, (x + 4).toFloat(), (y + 7).toFloat(), VoltHackTheme.textPrimary)

        val toggleX = x + width - 28
        val toggleY = y + 5
        val toggleW = 24
        val toggleH = 14
        ctx.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, color)
        val knobX = if (s.value) toggleX + toggleW - 10 else toggleX + 2
        ctx.fill(knobX, toggleY + 2, knobX + 8, toggleY + toggleH - 2, 0xFFFFFFFF.toInt())
    }

    private fun renderFloat(ctx: GuiGraphics, s: Setting.Float, x: Int, y: Int, width: Int) {
        ctx.fill(x, y, x + width, y + 32, 0x10000000.toInt())
        GUIFontRenderer.draw(ctx, s.name, (x + 4).toFloat(), (y + 3).toFloat(), VoltHackTheme.textSecondary)

        val label = "%.2f".format(s.value)
        GUIFontRenderer.draw(ctx, label, (x + width - GUIFontRenderer.width(label) - 4).toFloat(),
            (y + 3).toFloat(), VoltHackTheme.accent)

        val slY = y + 20
        val slH = 4
        ctx.fill(x + 2, slY, x + width - 2, slY + slH, VoltHackTheme.surfaceLight)

        val fill = ((s.value - s.min) / (s.max - s.min) * (width - 4)).coerceIn(0f, (width - 4).toFloat())
        val fillW = fill.roundToInt()
        ctx.fill(x + 2, slY, x + 2 + fillW, slY + slH, VoltHackTheme.accent)

        val thumbX = (x + 2 + fillW).coerceIn(x + 1, x + width - 2) - 3
        ctx.fill(thumbX, slY - 2, thumbX + 6, slY + slH + 2, 0xFFFFFFFF.toInt())
    }

    private fun renderInt(ctx: GuiGraphics, s: Setting.Int, x: Int, y: Int, width: Int) {
        ctx.fill(x, y, x + width, y + 32, 0x10000000.toInt())
        GUIFontRenderer.draw(ctx, s.name, (x + 4).toFloat(), (y + 3).toFloat(), VoltHackTheme.textSecondary)

        val label = s.value.toString()
        GUIFontRenderer.draw(ctx, label, (x + width - GUIFontRenderer.width(label) - 4).toFloat(),
            (y + 3).toFloat(), VoltHackTheme.accent)

        val slY = y + 20
        val slH = 4
        ctx.fill(x + 2, slY, x + width - 2, slY + slH, VoltHackTheme.surfaceLight)

        val range = (s.max - s.min).coerceAtLeast(1)
        val fill = (s.value - s.min).toFloat() / range * (width - 4)
        val fillW = fill.roundToInt()
        ctx.fill(x + 2, slY, x + 2 + fillW, slY + slH, VoltHackTheme.accent)

        val thumbX = (x + 2 + fillW).coerceIn(x + 1, x + width - 2) - 3
        ctx.fill(thumbX, slY - 2, thumbX + 6, slY + slH + 2, 0xFFFFFFFF.toInt())
    }

    private fun renderMode(ctx: GuiGraphics, s: Setting.Mode, x: Int, y: Int, width: Int) {
        ctx.fill(x, y, x + width, y + 26, 0x10000000.toInt())
        GUIFontRenderer.draw(ctx, s.name, (x + 4).toFloat(), (y + 4).toFloat(), VoltHackTheme.textSecondary)
        val idx = s.modes.indexOf(s.value).coerceAtLeast(0)
        GUIFontRenderer.draw(ctx, "${idx + 1}/${s.modes.size}", (x + width - 32).toFloat(),
            (y + 4).toFloat(), VoltHackTheme.textDisabled)
        GUIFontRenderer.draw(ctx, s.value, (x + 4).toFloat(), (y + 14).toFloat(), VoltHackTheme.accent)
    }

    private fun renderColor(ctx: GuiGraphics, s: Setting.Color, x: Int, y: Int, width: Int) {
        ctx.fill(x, y, x + width, y + 26, 0x10000000.toInt())
        GUIFontRenderer.draw(ctx, s.name, (x + 4).toFloat(), (y + 7).toFloat(), VoltHackTheme.textPrimary)
        ctx.fill(x + width - 22, y + 4, x + width - 6, y + 20, s.value)
        ctx.fill(x + width - 22, y + 4, x + width - 6, y + 20, 0x40000000.toInt())
    }

    private fun clickBoolean(mx: Int, my: Int, s: Setting.Boolean, x: Int, y: Int, width: Int): Boolean {
        val toggleX = x + width - 28
        val toggleY = y + 5
        if (mx in toggleX..toggleX + 24 && my in toggleY..toggleY + 14) {
            s.value = !s.value
            return true
        }
        return false
    }

    private fun clickFloat(mx: Int, my: Int, s: Setting.Float, x: Int, y: Int, width: Int): Boolean {
        val slY = y + 20
        if (my in slY - 3..slY + 7) {
            val ratio = ((mx - x - 2).toFloat() / (width - 4)).coerceIn(0f, 1f)
            val range = s.max - s.min
            val steps = (range / s.step).roundToInt().coerceAtLeast(1)
            val stepped = (ratio * steps).roundToInt().coerceIn(0, steps)
            s.value = s.min + stepped * s.step
            dragging = Dragging(s, mx, s.value, x + 2, width - 4)
            return true
        }
        return false
    }

    private fun clickInt(mx: Int, my: Int, s: Setting.Int, x: Int, y: Int, width: Int): Boolean {
        val slY = y + 20
        if (my in slY - 3..slY + 7) {
            val ratio = ((mx - x - 2).toFloat() / (width - 4)).coerceIn(0f, 1f)
            val range = s.max - s.min
            s.value = (s.min + (ratio * range).roundToInt()).coerceIn(s.min, s.max)
            dragging = Dragging(s, mx, (s.value - s.min).toFloat(), x + 2, width - 4)
            return true
        }
        return false
    }

    private fun clickMode(mx: Int, my: Int, s: Setting.Mode, x: Int, y: Int, width: Int, button: Int): Boolean {
        if (my in y..y + 26) {
            val idx = s.modes.indexOf(s.value).coerceAtLeast(0)
            if (button == 1) {
                s.value = s.modes[(idx - 1 + s.modes.size) % s.modes.size]
            } else {
                s.value = s.modes[(idx + 1) % s.modes.size]
            }
            return true
        }
        return false
    }
}
