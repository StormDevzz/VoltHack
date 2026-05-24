package volthack.gui.component

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme
import volthack.setting.Setting
import kotlin.math.roundToInt

object SettingWidget {
    private var dragging: Dragging? = null
    var hoveredSetting: Setting<*>? = null
    
    var activeInputSetting: Setting<*>? = null
    var inputText: String = ""
    private var inputAnim = 0f

    private data class Dragging(
        val setting: Setting<*>,
        val startMouseX: Int,
        val startValue: Float,
        val sliderX: Int,
        val sliderW: Int
    )

    @JvmOverloads
    fun render(ctx: GuiGraphics, setting: Setting<*>, x: Int, y: Int, width: Int, mouseX: Int = -999, mouseY: Int = -999) {
        if (!setting.isVisible()) return

        val height = when (setting) {
            is Setting.Float, is Setting.Int -> 32
            else -> 26
        }

        // Check hover for tooltip
        if (mouseX in x..x + width && mouseY in y..y + height) {
            hoveredSetting = setting
            // Lose focus on text box if mouse clicks elsewhere
            checkFocusLoss(mouseX, mouseY, x, y, width, height)
        }

        when (setting) {
            is Setting.Boolean -> renderBoolean(ctx, setting, x, y, width)
            is Setting.Float -> renderFloat(ctx, setting, x, y, width)
            is Setting.Int -> renderInt(ctx, setting, x, y, width)
            is Setting.Mode -> renderMode(ctx, setting, x, y, width)
            is Setting.Color -> renderColor(ctx, setting, x, y, width)
            is Setting.StringSetting -> renderString(ctx, setting, x, y, width)
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, setting: Setting<*>, x: Int, y: Int, width: Int, button: Int = 0): Boolean {
        if (!setting.isVisible()) return false
        
        // Handle right-click on float/int to activate text input
        if (button == 1 && (setting is Setting.Float || setting is Setting.Int)) {
            if (mouseX in x..x + width && mouseY in y..y + 32) {
                activeInputSetting = setting
                inputText = if (setting is Setting.Float) "%.2f".format(setting.value) else setting.value.toString()
                inputAnim = 0f
                return true
            }
        }

        return when (setting) {
            is Setting.Boolean -> clickBoolean(mouseX, mouseY, setting, x, y, width)
            is Setting.Float -> clickFloat(mouseX, mouseY, setting, x, y, width)
            is Setting.Int -> clickInt(mouseX, mouseY, setting, x, y, width)
            is Setting.Mode -> clickMode(mouseX, mouseY, setting, x, y, width, button)
            is Setting.StringSetting -> false
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

    fun mouseDragged(mx: Int, my: Int, button: Int): Boolean {
        val drag = dragging ?: return false
        val s = drag.setting
        val ratio = ((mx - drag.sliderX).toFloat() / drag.sliderW).coerceIn(0f, 1f)
        
        when (s) {
            is Setting.Float -> {
                val range = s.max - s.min
                val steps = (range / s.step).roundToInt().coerceAtLeast(1)
                val stepped = (ratio * steps).roundToInt().coerceIn(0, steps)
                s.value = s.min + stepped * s.step
            }
            is Setting.Int -> {
                val range = s.max - s.min
                s.value = (s.min + (ratio * range).roundToInt()).coerceIn(s.min, s.max)
            }
            else -> {}
        }
        return true
    }

    fun keyPressed(event: KeyEvent): Boolean {
        val s = activeInputSetting ?: return false
        val key = event.key()
        
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            applyInput()
            activeInputSetting = null
            return true
        } else if (key == GLFW.GLFW_KEY_ESCAPE) {
            activeInputSetting = null
            return true
        } else if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (inputText.isNotEmpty()) {
                inputText = inputText.substring(0, inputText.length - 1)
            }
            return true
        }
        return false
    }

    fun charTyped(event: CharacterEvent): Boolean {
        val char = event.codepoint().toChar()
        if (char.isDigit() || char == '.' || char == '-') {
            if (inputText.length < 10) {
                inputText += char
            }
            return true
        }
        return false
    }

    private fun applyInput() {
        val s = activeInputSetting ?: return
        try {
            when (s) {
                is Setting.Float -> {
                    val parsed = inputText.toFloatOrNull()
                    if (parsed != null) {
                        s.value = parsed.coerceIn(s.min, s.max)
                    }
                }
                is Setting.Int -> {
                    val parsed = inputText.toIntOrNull()
                    if (parsed != null) {
                        s.value = parsed.coerceIn(s.min, s.max)
                    }
                }
                else -> {}
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun resetState() {
        activeInputSetting = null
        inputText = ""
        dragging = null
        hoveredSetting = null
    }

    private fun checkFocusLoss(mx: Int, my: Int, x: Int, y: Int, w: Int, h: Int) {
        val active = activeInputSetting ?: return
        // If mouse is outside this setting widget, apply changes
        if (mx !in x..x + w || my !in y..y + h) {
            applyInput()
            activeInputSetting = null
        }
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

        if (s == activeInputSetting) {
            inputAnim += (1f - inputAnim) * 0.18f
            val boxColor = VoltHackTheme.surfaceLight
            val borderC = VoltHackTheme.accent
            
            // Draw animated text box instead of slider
            ctx.fill(x + 2, slY - 4, x + width - 2, slY + slH + 4, boxColor)
            val bt = 1
            ctx.fill(x + 2, slY - 4, x + width - 2, slY - 4 + bt, borderC)
            ctx.fill(x + 2, slY + slH + 4 - bt, x + width - 2, slY + slH + 4, borderC)
            ctx.fill(x + 2, slY - 4, x + 2 + bt, slY + slH + 4, borderC)
            ctx.fill(x + width - 2 - bt, slY - 4, x + width - 2, slY + slH + 4, borderC)

            val cursor = if ((System.currentTimeMillis() / 500) % 2 == 0L) "|" else ""
            GUIFontRenderer.draw(ctx, inputText + cursor, (x + 6).toFloat(), (slY - 2).toFloat(), VoltHackTheme.textPrimary)
        } else {
            ctx.fill(x + 2, slY, x + width - 2, slY + slH, VoltHackTheme.surfaceLight)

            val fill = ((s.value - s.min) / (s.max - s.min) * (width - 4)).coerceIn(0f, (width - 4).toFloat())
            val fillW = fill.roundToInt()
            ctx.fill(x + 2, slY, x + 2 + fillW, slY + slH, VoltHackTheme.accent)

            val thumbX = (x + 2 + fillW).coerceIn(x + 1, x + width - 2) - 3
            ctx.fill(thumbX, slY - 2, thumbX + 6, slY + slH + 2, 0xFFFFFFFF.toInt())
        }
    }

    private fun renderInt(ctx: GuiGraphics, s: Setting.Int, x: Int, y: Int, width: Int) {
        ctx.fill(x, y, x + width, y + 32, 0x10000000.toInt())
        GUIFontRenderer.draw(ctx, s.name, (x + 4).toFloat(), (y + 3).toFloat(), VoltHackTheme.textSecondary)

        val label = s.value.toString()
        GUIFontRenderer.draw(ctx, label, (x + width - GUIFontRenderer.width(label) - 4).toFloat(),
            (y + 3).toFloat(), VoltHackTheme.accent)

        val slY = y + 20
        val slH = 4

        if (s == activeInputSetting) {
            inputAnim += (1f - inputAnim) * 0.18f
            val boxColor = VoltHackTheme.surfaceLight
            val borderC = VoltHackTheme.accent
            
            // Draw animated text box instead of slider
            ctx.fill(x + 2, slY - 4, x + width - 2, slY + slH + 4, boxColor)
            val bt = 1
            ctx.fill(x + 2, slY - 4, x + width - 2, slY - 4 + bt, borderC)
            ctx.fill(x + 2, slY + slH + 4 - bt, x + width - 2, slY + slH + 4, borderC)
            ctx.fill(x + 2, slY - 4, x + 2 + bt, slY + slH + 4, borderC)
            ctx.fill(x + width - 2 - bt, slY - 4, x + width - 2, slY + slH + 4, borderC)

            val cursor = if ((System.currentTimeMillis() / 500) % 2 == 0L) "|" else ""
            GUIFontRenderer.draw(ctx, inputText + cursor, (x + 6).toFloat(), (slY - 2).toFloat(), VoltHackTheme.textPrimary)
        } else {
            ctx.fill(x + 2, slY, x + width - 2, slY + slH, VoltHackTheme.surfaceLight)

            val range = (s.max - s.min).coerceAtLeast(1)
            val fill = (s.value - s.min).toFloat() / range * (width - 4)
            val fillW = fill.roundToInt()
            ctx.fill(x + 2, slY, x + 2 + fillW, slY + slH, VoltHackTheme.accent)

            val thumbX = (x + 2 + fillW).coerceIn(x + 1, x + width - 2) - 3
            ctx.fill(thumbX, slY - 2, thumbX + 6, slY + slH + 2, 0xFFFFFFFF.toInt())
        }
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

    private fun renderString(ctx: GuiGraphics, s: Setting.StringSetting, x: Int, y: Int, width: Int) {
        ctx.fill(x, y, x + width, y + 26, 0x10000000.toInt())
        GUIFontRenderer.draw(ctx, s.name, (x + 4).toFloat(), (y + 4).toFloat(), VoltHackTheme.textSecondary)
        val display = if (s.value.length > 20) s.value.take(17) + "..." else s.value
        GUIFontRenderer.draw(ctx, display, (x + 4).toFloat(), (y + 14).toFloat(), VoltHackTheme.accent)
    }

    private fun clickBoolean(mx: Int, my: Int, s: Setting.Boolean, x: Int, y: Int, width: Int): Boolean {
        if (my in y..y + 26 && mx in x..x + width) {
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
