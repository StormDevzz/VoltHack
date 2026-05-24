package volthack.gui.component

import net.minecraft.client.gui.GuiGraphics
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme
import volthack.setting.Module
import volthack.setting.Setting

class ModuleCard(val module: Module) {
    var x = 0
    var y = 0
    var hovered = false
    var expanded = false

    private var toggleAnim = if (module.enabled) 1f else 0f
    private var settingsAnim = if (expanded) 1f else 0f

    val width = VoltHackTheme.CARD_WIDTH
    val height: Int
        get() {
            val base = VoltHackTheme.CARD_HEIGHT
            return base + (settingsHeight * settingsAnim).toInt()
        }

    private val settingsHeight: Int
        get() {
            var h = 26
            for (s in module.settings) {
                if (!s.isVisible()) continue
                h += when (s) {
                    is Setting.Float, is Setting.Int -> 32
                    else -> 26
                }
            }
            return h + 8
        }

    private fun blendColors(c1: Int, c2: Int, ratio: Float): Int {
        val r1 = (c1 shr 16) and 0xFF
        val g1 = (c1 shr 8) and 0xFF
        val b1 = c1 and 0xFF
        val a1 = (c1 shr 24) and 0xFF

        val r2 = (c2 shr 16) and 0xFF
        val g2 = (c2 shr 8) and 0xFF
        val b2 = c2 and 0xFF
        val a2 = (c2 shr 24) and 0xFF

        val r = (r1 + ratio * (r2 - r1)).toInt()
        val g = (g1 + ratio * (g2 - g1)).toInt()
        val b = (b1 + ratio * (b2 - b1)).toInt()
        val a = (a1 + ratio * (a2 - a1)).toInt()

        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    fun render(ctx: GuiGraphics, mouseX: Int, mouseY: Int) {
        hovered = mouseX in x..(x + width) && mouseY in y..(y + VoltHackTheme.CARD_HEIGHT)

        val targetToggle = if (module.enabled) 1f else 0f
        toggleAnim += (targetToggle - toggleAnim) * 0.18f

        val targetSettings = if (expanded) 1f else 0f
        settingsAnim += (targetSettings - settingsAnim) * 0.18f

        val bgColor = blendColors(VoltHackTheme.disabledBg, VoltHackTheme.enabledBg, toggleAnim)
        val borderColor = blendColors(VoltHackTheme.border, VoltHackTheme.accent, toggleAnim)

        if (hovered) {
            ctx.fill(x - 1, y - 1, x + width + 1, y + VoltHackTheme.CARD_HEIGHT + 1, VoltHackTheme.accentGlow)
        }

        ctx.fill(x, y, x + width, y + VoltHackTheme.CARD_HEIGHT, bgColor)
        ctx.fill(x, y, x + width, y + 1, borderColor)

        val nameColor = blendColors(VoltHackTheme.textSecondary, VoltHackTheme.textPrimary, toggleAnim)
        GUIFontRenderer.drawCentered(
            ctx, module.name,
            (x + width / 2f), (y + (VoltHackTheme.CARD_HEIGHT - GUIFontRenderer.height) / 2f),
            nameColor
        )

        if (settingsAnim > 0.01f) {
            renderSettings(ctx, mouseX, mouseY)
        }
    }

    private fun renderSettings(ctx: GuiGraphics, mouseX: Int, mouseY: Int) {
        var sy = y + VoltHackTheme.CARD_HEIGHT + 4
        val drawH = (settingsHeight * settingsAnim).toInt()
        if (drawH <= 4) return

        ctx.fill(x, sy, x + width, sy + drawH - 4, VoltHackTheme.surfaceLight)

        val bindH = 26
        if (sy + bindH - (y + VoltHackTheme.CARD_HEIGHT + 4) <= drawH) {
            ctx.fill(x + 4, sy, x + width - 4, sy + bindH, 0x10000000.toInt())
            GUIFontRenderer.draw(ctx, "Bind", (x + 8).toFloat(), (sy + 7).toFloat(), VoltHackTheme.textSecondary)

            val active = volthack.manager.InputManager.bindTargetModule == module
            val bindText = if (active) "..." else volthack.manager.InputManager.getBindName(module.bindKey)
            val btnW = 54
            val btnX = x + width - 8 - btnW
            val btnY = sy + 4
            val btnH = 18
            val isHovered = mouseX in btnX..(btnX + btnW) && mouseY in btnY..(btnY + btnH)

            val btnBg = when {
                active -> VoltHackTheme.accent
                isHovered -> VoltHackTheme.surfaceHover
                else -> VoltHackTheme.surface
            }
            ctx.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg)
            GUIFontRenderer.drawCentered(ctx, bindText, (btnX + btnW / 2f), (btnY + (btnH - GUIFontRenderer.height) / 2f), VoltHackTheme.textPrimary)
        }
        sy += bindH

        for (setting in module.settings) {
            val sh = when (setting) {
                is Setting.Float, is Setting.Int -> 32
                else -> 26
            }
            if (setting.isVisible()) {
                if (sy + sh - (y + VoltHackTheme.CARD_HEIGHT + 4) <= drawH) {
                    SettingWidget.render(ctx, setting, x + 4, sy, width - 8, mouseX, mouseY)
                }
            }
            sy += sh
        }
    }

    fun mouseClicked(mx: Int, my: Int, button: Int): Boolean {
        if (!expanded || settingsAnim < 0.5f) return false
        var sy = y + VoltHackTheme.CARD_HEIGHT + 4

        val bindH = 26
        val bindBtnW = 54
        val bindBtnX = x + width - 8 - bindBtnW
        val bindBtnY = sy + 4
        val bindBtnH = 18
        if (mx in bindBtnX..(bindBtnX + bindBtnW) && my in bindBtnY..(bindBtnY + bindBtnH)) {
            if (volthack.manager.InputManager.bindTargetModule == module) {
                volthack.manager.InputManager.bindTargetModule = null
            } else {
                volthack.manager.InputManager.bindTargetModule = module
            }
            return true
        }
        sy += bindH

        for (setting in module.settings) {
            val sh = when (setting) {
                is Setting.Float, is Setting.Int -> 32
                else -> 26
            }
            if (setting.isVisible()) {
                if (SettingWidget.mouseClicked(mx, my, setting, x + 4, sy, width - 8, button)) return true
            }
            sy += sh
        }
        return false
    }
}
