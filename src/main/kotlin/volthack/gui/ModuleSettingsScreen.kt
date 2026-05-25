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

class ModuleSettingsScreen(val module: Module, val parent: Screen) : Screen(Component.literal(module.name + " Settings")) {
    val winW = 320
    val winH = 360
    
    override fun render(ctx: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        ctx.fill(0, 0, width, height, VoltHackTheme.overlay)
        
        val x = (width - winW) / 2
        val y = (height - winH) / 2
        
        // Draw premium window design with glow and border
        ctx.fill(x - 2, y - 2, x + winW + 2, y + winH + 2, VoltHackTheme.accentGlow)
        ctx.fill(x, y, x + winW, y + winH, VoltHackTheme.surface)
        ctx.fill(x, y, x + winW, y + 2, VoltHackTheme.accent)
        
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
        
        // Render each setting
        for (setting in module.settings) {
            if (!setting.isVisible()) continue
            val sh = SettingWidget.getHeight(setting)
            SettingWidget.render(ctx, setting, x + 10, sy, winW - 20, mouseX, mouseY)
            sy += sh + 4
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
    
    override fun mouseClicked(event: MouseButtonEvent, isInside: Boolean): Boolean {
        val mx = event.x().toInt()
        val my = event.y().toInt()
        val button = event.button()
        
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
        
        var sy = y + 46
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
        
        sy += 28
        
        for (setting in module.settings) {
            if (!setting.isVisible()) continue
            val sh = SettingWidget.getHeight(setting)
            if (SettingWidget.mouseClicked(mx, my, setting, x + 10, sy, winW - 20, button)) {
                return true
            }
            sy += sh + 4
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
    
    override fun onClose() {
        SettingWidget.resetState()
        minecraft?.setScreen(parent)
    }
    
    override fun isPauseScreen() = false
}
