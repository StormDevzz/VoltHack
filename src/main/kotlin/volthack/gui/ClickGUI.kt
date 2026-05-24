package volthack.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import volthack.gui.component.ModuleCard
import volthack.gui.component.SettingWidget
import volthack.gui.component.TooltipRenderer
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme
import volthack.lang.LanguageManager
import volthack.setting.Category
import volthack.setting.ModuleManager

class ClickGUI : Screen(Component.literal("ClickGUI")) {
    private var selectedCategory = Category.COMBAT
    private val cards = mutableListOf<ModuleCard>()
    private var scrollOffset = 0
    private var maxScroll = 0

    override fun init() {
        super.init()
        rebuild()
    }

    private fun rebuild() {
        cards.clear()
        ModuleManager.getByCategory(selectedCategory).forEach { cards.add(ModuleCard(it)) }
        layout()
    }

    private fun layout() {
        val cols = ((width - VoltHackTheme.PANEL_PADDING * 2 + VoltHackTheme.CARD_GAP)
                / (VoltHackTheme.CARD_WIDTH + VoltHackTheme.CARD_GAP)).coerceAtLeast(1)

        cards.forEachIndexed { i, card ->
            card.x = VoltHackTheme.PANEL_PADDING + (i % cols) * (VoltHackTheme.CARD_WIDTH + VoltHackTheme.CARD_GAP)
            card.y = VoltHackTheme.TAB_HEIGHT + VoltHackTheme.PANEL_PADDING + (i / cols) * (VoltHackTheme.CARD_HEIGHT + VoltHackTheme.CARD_GAP)
        }

        val rows = (cards.size + cols - 1) / cols
        val contentH = rows * (VoltHackTheme.CARD_HEIGHT + VoltHackTheme.CARD_GAP)
        maxScroll = (contentH - (height - VoltHackTheme.TAB_HEIGHT - VoltHackTheme.PANEL_PADDING)).coerceAtLeast(0)
        scrollOffset = scrollOffset.coerceIn(0, maxScroll)
    }

    override fun render(ctx: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        ctx.fill(0, 0, width, height, VoltHackTheme.overlay)
        renderTabs(ctx, mouseX, mouseY)

        var hoveredName = ""
        for (card in cards) {
            val visualY = card.y - scrollOffset
            if (visualY + VoltHackTheme.CARD_HEIGHT < VoltHackTheme.TAB_HEIGHT || visualY > height) continue

            val originalY = card.y
            card.y = visualY
            card.render(ctx, mouseX, mouseY)
            card.y = originalY

            if (card.hovered && card.module.description.isNotEmpty()) {
                hoveredName = card.module.name
            }
        }

        if (hoveredName.isNotEmpty()) {
            val desc = LanguageManager.get("module.$hoveredName.description")
            TooltipRenderer.render(ctx, desc, mouseX, mouseY)
        }
    }

    private fun renderTabs(ctx: GuiGraphics, mouseX: Int, mouseY: Int) {
        ctx.fill(0, 0, width, VoltHackTheme.TAB_HEIGHT, VoltHackTheme.background)

        var tx = VoltHackTheme.PANEL_PADDING
        for (cat in Category.entries) {
            val text = cat.displayName
            val tw = GUIFontRenderer.width(text) + VoltHackTheme.TAB_PADDING * 2 + 8
            val selected = cat == selectedCategory
            val color = if (selected) (VoltHackTheme.categoryColors[cat] ?: VoltHackTheme.accent) else VoltHackTheme.textSecondary

            ctx.fill(tx, 0, tx + tw, VoltHackTheme.TAB_HEIGHT, if (selected) VoltHackTheme.surfaceLight else VoltHackTheme.background)
            ctx.fill(tx, VoltHackTheme.TAB_HEIGHT - 2, tx + tw, VoltHackTheme.TAB_HEIGHT, color)
            GUIFontRenderer.drawCentered(ctx, text, (tx + tw / 2f), (VoltHackTheme.TAB_HEIGHT - GUIFontRenderer.height) / 2f, color)

            tx += tw + 4
        }
    }

    override fun mouseClicked(event: MouseButtonEvent, isInside: Boolean): Boolean {
        val mx = event.x().toInt()
        val my = event.y().toInt()
        val button = event.button()

        if (my < VoltHackTheme.TAB_HEIGHT) {
            var tx = VoltHackTheme.PANEL_PADDING
            for (cat in Category.entries) {
                val tw = GUIFontRenderer.width(cat.displayName) + VoltHackTheme.TAB_PADDING * 2 + 8
                if (mx in tx..tx + tw) {
                    selectedCategory = cat
                    scrollOffset = 0
                    rebuild()
                    return true
                }
                tx += tw + 4
            }
            return super.mouseClicked(event, isInside)
        }

        val adjY = my + scrollOffset
        for (card in cards) {
            if (mx in card.x..card.x + VoltHackTheme.CARD_WIDTH &&
                adjY in card.y..card.y + card.height
            ) {
                if (card.expanded && button == 0) {
                    if (card.mouseClicked(mx, adjY)) return true
                }

                if (button == 0) {
                    card.module.toggle()
                    return true
                } else if (button == 1 && card.module.settings.isNotEmpty()) {
                    card.expanded = !card.expanded
                    return true
                }
            }
        }

        return super.mouseClicked(event, isInside)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        SettingWidget.mouseReleased()
        return super.mouseReleased(event)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        scrollOffset = (scrollOffset - (verticalAmount * VoltHackTheme.SCROLL_SPEED)).toInt().coerceIn(0, maxScroll)
        return true
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE || event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            onClose()
            return true
        }
        return super.keyPressed(event)
    }

    override fun isPauseScreen() = false
}
