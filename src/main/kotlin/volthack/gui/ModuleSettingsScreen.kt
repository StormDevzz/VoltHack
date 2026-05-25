package volthack.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.input.CharacterEvent
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import volthack.gui.component.SettingWidget
import volthack.gui.component.TooltipRenderer
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme
import volthack.setting.Module
import volthack.setting.Setting
import kotlin.math.roundToInt

class ModuleSettingsScreen(val module: Module, val parent: Screen) : Screen(Component.literal(module.name + " Settings")) {
    val winW = 320
    private var scroll = 0.0
    private var animProgress = 0f
    private var closing = false

    override fun render(ctx: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        // Smoothly interpolate animation progress
        val target = if (closing) 0f else 1f
        animProgress += (target - animProgress) * 0.22f

        if (closing && animProgress < 0.05f) {
            minecraft?.setScreen(parent)
            return
        }

        // Animated translucent background overlay
        val overlayAlpha = ((VoltHackTheme.overlay shr 24 and 0xFF) * animProgress).toInt()
        val overlayColor = (overlayAlpha shl 24) or (VoltHackTheme.overlay and 0x00FFFFFF)
        ctx.fill(0, 0, width, height, overlayColor)

        val visibleSettings = module.settings.filter { it.isVisible() }
        val settingsTotalHeight = visibleSettings.sumOf { SettingWidget.getHeight(it) + 4 }

        // Dynamic height calculation
        val contentHeight = 46 + 28 + 10 + settingsTotalHeight + 40
        val winH = contentHeight.coerceIn(220, 420)

        // Calculate pop scale and coordinates centered
        val scale = 0.9f + animProgress * 0.1f
        val currentW = (winW * scale).toInt()
        val currentH = (winH * scale).toInt()
        val x = (width - currentW) / 2
        val y = (height - currentH) / 2

        // Draw premium window design with glow and border
        ctx.fill(x - 2, y - 2, x + currentW + 2, y + currentH + 2, VoltHackTheme.accentGlow)
        ctx.fill(x, y, x + currentW, y + currentH, VoltHackTheme.surface)
        ctx.fill(x, y, x + currentW, y + 2, VoltHackTheme.accent)

        // Only render text and controls when transition is almost completed to prevent skewing
        if (animProgress > 0.8f) {
            val settingsAreaY = y + 78
            val settingsAreaMaxY = y + winH - 40
            val viewHeight = settingsAreaMaxY - settingsAreaY
            val maxScroll = (settingsTotalHeight - viewHeight).coerceAtLeast(0)
            scroll = scroll.coerceIn(0.0, maxScroll.toDouble())

            // Title
            GUIFontRenderer.drawCentered(ctx, module.name, (x + winW / 2f), (y + 16).toFloat(), VoltHackTheme.textPrimary)

            ctx.fill(x + 10, y + 40, x + winW - 10, y + 41, VoltHackTheme.border)

            // Render Bind setting
            var sy = y + 46
            ctx.fill(x + 10, sy, x + winW - 10, sy + 24, 0x10000000.toInt())
            GUIFontRenderer.draw(ctx, "Bind", (x + 14).toFloat(), (sy + 7).toFloat(), VoltHackTheme.textSecondary)

            val active = volthack.manager.InputManager.bindTargetModule == module
            val bindText = if (active) "..." else volthack.manager.InputManager.getBindName(module.bindKey)
            val btnW = 54
            val btnX = x + winW - 14 - btnW
            val btnY = sy + 3
            val btnH = 18
            val isHoveredBind = mouseX in btnX..(btnX + btnW) && mouseY in btnY..(btnY + btnH)
            val btnBg = when {
                active -> VoltHackTheme.accent
                isHoveredBind -> VoltHackTheme.surfaceHover
                else -> VoltHackTheme.surface
            }
            ctx.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg)
            GUIFontRenderer.drawCentered(ctx, bindText, (btnX + btnW / 2f), (btnY + (btnH - GUIFontRenderer.height) / 2f), VoltHackTheme.textPrimary)

            sy += 28

            // Scissored scroll viewport
            ctx.enableScissor(x + 8, settingsAreaY, x + winW - 8, settingsAreaMaxY)

            val isMouseInsideViewport = mouseX in (x + 8)..(x + winW - 8) && mouseY in settingsAreaY..settingsAreaMaxY
            val passedMouseY = if (isMouseInsideViewport) mouseY else -999

            SettingWidget.hoveredSetting = null

            var currentSy = sy - scroll.toInt()
            for (setting in visibleSettings) {
                val sh = SettingWidget.getHeight(setting)
                SettingWidget.render(ctx, setting, x + 10, currentSy, winW - 20, mouseX, passedMouseY)
                currentSy += sh + 4
            }

            ctx.disableScissor()

            // Render scrollbar if needed
            if (maxScroll > 0) {
                val scrollbarW = 4
                val scrollbarX = x + winW - 6
                val scrollTrackH = viewHeight
                val scrollThumbH = (viewHeight.toFloat() * viewHeight / (settingsTotalHeight + viewHeight)).coerceAtLeast(10f).roundToInt()
                val scrollThumbY = settingsAreaY + ((scroll / maxScroll) * (scrollTrackH - scrollThumbH)).roundToInt()

                ctx.fill(scrollbarX, settingsAreaY, scrollbarX + scrollbarW, settingsAreaY + scrollTrackH, 0x1AFFFFFF.toInt())
                ctx.fill(scrollbarX, scrollThumbY, scrollbarX + scrollbarW, scrollThumbY + scrollThumbH, VoltHackTheme.accent)
            }

            // Render Back button
            val closeW = 90
            val closeH = 22
            val closeX = x + (winW - closeW) / 2
            val closeY = y + winH - 30
            val hoveredClose = mouseX in closeX..(closeX + closeW) && mouseY in closeY..(closeY + closeH)
            ctx.fill(closeX, closeY, closeX + closeW, closeY + closeH, if (hoveredClose) VoltHackTheme.accent else VoltHackTheme.surfaceLight)
            GUIFontRenderer.drawCentered(ctx, "BACK", (closeX + closeW / 2f), (closeY + (closeH - GUIFontRenderer.height) / 2f), VoltHackTheme.textPrimary)

            // Tooltips
            val hoveredS = SettingWidget.hoveredSetting
            if (hoveredS != null && hoveredS.description.isNotEmpty()) {
                TooltipRenderer.render(ctx, hoveredS.description, mouseX, mouseY)
            }
        }
    }

    override fun mouseClicked(event: MouseButtonEvent, isInside: Boolean): Boolean {
        if (closing || animProgress < 0.8f) return false
        val mx = event.x().toInt()
        val my = event.y().toInt()
        val button = event.button()

        val visibleSettings = module.settings.filter { it.isVisible() }
        val settingsTotalHeight = visibleSettings.sumOf { SettingWidget.getHeight(it) + 4 }
        val contentHeight = 46 + 28 + 10 + settingsTotalHeight + 40
        val winH = contentHeight.coerceIn(220, 420)

        val x = (width - winW) / 2
        val y = (height - winH) / 2

        val closeW = 90
        val closeH = 22
        val closeX = x + (winW - closeW) / 2
        val closeY = y + winH - 30
        if (mx in closeX..(closeX + closeW) && my in closeY..(closeY + closeH)) {
            onClose()
            return true
        }

        val sy = y + 46
        val btnW = 54
        val btnX = x + winW - 14 - btnW
        val btnY = sy + 3
        val btnH = 18
        if (mx in btnX..(btnX + btnW) && my in btnY..(btnY + btnH)) {
            if (volthack.manager.InputManager.bindTargetModule == module) {
                volthack.manager.InputManager.bindTargetModule = null
            } else {
                volthack.manager.InputManager.bindTargetModule = module
            }
            return true
        }

        val settingsAreaY = y + 78
        val settingsAreaMaxY = y + winH - 40
        val isMouseInsideViewport = mx in (x + 8)..(x + winW - 8) && my in settingsAreaY..settingsAreaMaxY

        if (isMouseInsideViewport) {
            var currentSy = sy + 28 - scroll.toInt()
            for (setting in visibleSettings) {
                val sh = SettingWidget.getHeight(setting)
                if (SettingWidget.mouseClicked(mx, my, setting, x + 10, currentSy, winW - 20, button)) {
                    return true
                }
                currentSy += sh + 4
            }
        }

        return super.mouseClicked(event, isInside)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        SettingWidget.mouseReleased()
        return super.mouseReleased(event)
    }

    override fun mouseDragged(mouseButtonEvent: MouseButtonEvent, d: Double, e: Double): Boolean {
        if (SettingWidget.mouseDragged(mouseButtonEvent.x().toInt(), mouseButtonEvent.y().toInt(), mouseButtonEvent.button())) return true
        return super.mouseDragged(mouseButtonEvent, d, e)
    }

    override fun charTyped(characterEvent: CharacterEvent): Boolean {
        if (SettingWidget.activeInputSetting != null) {
            if (SettingWidget.charTyped(characterEvent)) return true
        }
        return super.charTyped(characterEvent)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (SettingWidget.activeInputSetting != null) {
            if (SettingWidget.keyPressed(event)) return true
        }
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            onClose()
            return true
        }
        return super.keyPressed(event)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (closing || animProgress < 0.8f) return false
        val visibleSettings = module.settings.filter { it.isVisible() }
        val settingsTotalHeight = visibleSettings.sumOf { SettingWidget.getHeight(it) + 4 }
        val contentHeight = 46 + 28 + 10 + settingsTotalHeight + 40
        val winH = contentHeight.coerceIn(220, 420)

        val y = (height - winH) / 2
        val settingsAreaY = y + 78
        val settingsAreaMaxY = y + winH - 40
        val viewHeight = settingsAreaMaxY - settingsAreaY
        val maxScroll = (settingsTotalHeight - viewHeight).coerceAtLeast(0)

        scroll = (scroll - verticalAmount * 15).coerceIn(0.0, maxScroll.toDouble())
        return true
    }

    override fun onClose() {
        closing = true
        SettingWidget.resetState()
    }

    override fun isPauseScreen() = false
}
