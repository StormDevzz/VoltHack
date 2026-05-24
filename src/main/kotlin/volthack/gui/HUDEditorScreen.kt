package volthack.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme
import volthack.hud.HUDManager
import volthack.hud.HUDElement

class HUDEditorScreen : Screen(Component.literal("HUD Editor")) {
    private var dragTarget: HUDElement? = null
    private var dragOffX = 0
    private var dragOffY = 0
    private val snapSize = 5

    override fun render(ctx: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        ctx.fill(0, 0, width, height, 0x90000000.toInt())

        for (element in HUDManager.getAll()) {
            element.render(ctx)
            val w = element.cachedWidth
            val h = element.cachedHeight
            if (w <= 0 || h <= 0) continue

            val isHovered = mouseX in element.x..element.x + w && mouseY in element.y..element.y + h
            val borderColor = when {
                element == dragTarget -> VoltHackTheme.accent
                isHovered -> VoltHackTheme.accentDim
                element.enabled -> VoltHackTheme.textSecondary
                else -> VoltHackTheme.textDisabled
            }

            ctx.fill(element.x - 1, element.y - 1, element.x + w + 1, element.y + h + 1, borderColor)

            if (!element.enabled) {
                ctx.fill(element.x, element.y, element.x + w, element.y + h, 0x50000000.toInt())
                GUIFontRenderer.drawCentered(
                    ctx, "\u2715 ${element.name}",
                    (element.x + w / 2f), (element.y + h / 2f - 4f),
                    VoltHackTheme.textDisabled
                )
            }
        }

        renderSidebar(ctx, mouseX, mouseY)
        super.render(ctx, mouseX, mouseY, delta)
    }

    private fun renderSidebar(ctx: GuiGraphics, mouseX: Int, mouseY: Int) {
        val sx = width - 160
        ctx.fill(sx, 0, width, height, VoltHackTheme.background)

        GUIFontRenderer.drawCentered(ctx, "HUD Elements", (sx + 80f), 10f, VoltHackTheme.textPrimary, false)

        var cy = 30
        for (element in HUDManager.getAll()) {
            val hovered = mouseX in (sx + 4)..width && mouseY in cy..cy + 20
            if (hovered) ctx.fill(sx + 4, cy, width - 4, cy + 20, VoltHackTheme.surfaceHover)

            val icon = if (element.enabled) "\u25CF" else "\u25CB"
            val iconColor = if (element.enabled) VoltHackTheme.accent else VoltHackTheme.textDisabled
            GUIFontRenderer.draw(ctx, icon, (sx + 10).toFloat(), (cy + 6).toFloat(), iconColor)
            GUIFontRenderer.draw(ctx, element.name, (sx + 28).toFloat(), (cy + 6).toFloat(), VoltHackTheme.textPrimary)

            cy += 22
        }

        val resetY = height - 40
        val resetHover = mouseX in (sx + 10)..(width - 10) && mouseY in resetY..resetY + 28
        ctx.fill(sx + 10, resetY, width - 10, resetY + 28, if (resetHover) VoltHackTheme.surfaceHover else VoltHackTheme.surface)
        GUIFontRenderer.drawCentered(ctx, "Reset Positions", (sx + 80f), (resetY + 10).toFloat(), VoltHackTheme.textSecondary)
    }

    override fun mouseClicked(event: MouseButtonEvent, isInside: Boolean): Boolean {
        val mx = event.x().toInt()
        val my = event.y().toInt()
        val button = event.button()

        if (mx >= width - 160) {
            var cy = 30
            for (element in HUDManager.getAll()) {
                if (mx in (width - 156)..width && my in cy..cy + 20 && button == 0) {
                    element.enabled = !element.enabled
                    HUDManager.save()
                    return true
                }
                cy += 22
            }

            if (mx in (width - 150)..(width - 10) && my in height - 40..height - 12) {
                HUDManager.getAll().forEach { it.x = 4; it.y = 4 }
                HUDManager.save()
                return true
            }
            return super.mouseClicked(event, isInside)
        }

        for (element in HUDManager.getAll().reversed()) {
            val w = element.cachedWidth
            val h = element.cachedHeight
            if (w <= 0 || h <= 0) continue
            if (mx !in element.x..element.x + w || my !in element.y..element.y + h) continue

            if (button == 0) {
                dragTarget = element
                dragOffX = mx - element.x
                dragOffY = my - element.y
                return true
            } else if (button == 1) {
                element.enabled = !element.enabled
                HUDManager.save()
                return true
            }
        }

        return super.mouseClicked(event, isInside)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (dragTarget != null) {
            dragTarget = null
            HUDManager.save()
        }
        return super.mouseReleased(event)
    }

    override fun mouseDragged(event: MouseButtonEvent, deltaX: Double, deltaY: Double): Boolean {
        val element = dragTarget ?: return false

        val mx = event.x().toInt() - dragOffX
        val my = event.y().toInt() - dragOffY

        element.x = (mx / snapSize) * snapSize
        element.y = (my / snapSize) * snapSize
        return true
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE || event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            HUDManager.save()
            onClose()
            return true
        }
        return super.keyPressed(event)
    }

    override fun isPauseScreen() = false
}
