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
    var animProgress = 0f

    val width = VoltHackTheme.CARD_WIDTH
    val height: Int
        get() {
            val base = VoltHackTheme.CARD_HEIGHT
            return if (expanded) base + settingsHeight else base
        }

    private val settingsHeight: Int
        get() {
            if (module.settings.isEmpty()) return 0
            var h = 0
            for (s in module.settings) {
                if (!s.isVisible()) continue
                h += when (s) {
                    is Setting.Float, is Setting.Int -> 32
                    else -> 26
                }
            }
            return h + 8
        }

    fun render(ctx: GuiGraphics, mouseX: Int, mouseY: Int) {
        hovered = mouseX in x..(x + width) && mouseY in y..(y + VoltHackTheme.CARD_HEIGHT)

        val bgColor = if (module.enabled) VoltHackTheme.enabledBg else VoltHackTheme.disabledBg
        val borderColor = if (module.enabled) VoltHackTheme.accent else VoltHackTheme.border

        if (hovered) {
            ctx.fill(x - 1, y - 1, x + width + 1, y + VoltHackTheme.CARD_HEIGHT + 1, VoltHackTheme.accentGlow)
        }

        ctx.fill(x, y, x + width, y + VoltHackTheme.CARD_HEIGHT, bgColor)
        ctx.fill(x, y, x + width, y + 1, borderColor)

        val nameColor = if (module.enabled) VoltHackTheme.textPrimary else VoltHackTheme.textSecondary
        GUIFontRenderer.drawCentered(
            ctx, module.name,
            (x + width / 2f), (y + (VoltHackTheme.CARD_HEIGHT - GUIFontRenderer.height) / 2f),
            nameColor
        )

        if (expanded) renderSettings(ctx, mouseX, mouseY)
    }

    private fun renderSettings(ctx: GuiGraphics, mouseX: Int, mouseY: Int) {
        var sy = y + VoltHackTheme.CARD_HEIGHT + 4
        ctx.fill(x, sy, x + width, sy + settingsHeight - 4, VoltHackTheme.surfaceLight)

        for (setting in module.settings) {
            val sh = when (setting) {
                is Setting.Float, is Setting.Int -> 32
                else -> 26
            }
            if (setting.isVisible()) {
                SettingWidget.render(ctx, setting, x + 4, sy, width - 8)
            }
            sy += sh
        }
    }

    fun mouseClicked(mx: Int, my: Int): Boolean {
        if (!expanded) return false
        var sy = y + VoltHackTheme.CARD_HEIGHT + 4
        for (setting in module.settings) {
            val sh = when (setting) {
                is Setting.Float, is Setting.Int -> 32
                else -> 26
            }
            if (setting.isVisible()) {
                if (SettingWidget.mouseClicked(mx, my, setting, x + 4, sy, width - 8)) return true
            }
            sy += sh
        }
        return false
    }
}
