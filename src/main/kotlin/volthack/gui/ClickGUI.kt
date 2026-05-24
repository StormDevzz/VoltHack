package volthack.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.input.CharacterEvent
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
import volthack.manager.ConfigManager

class ClickGUI : Screen(Component.literal("ClickGUI")) {
    private var selectedCategory = Category.COMBAT
    private val cards = mutableListOf<ModuleCard>()
    private var scrollOffset = 0
    private var maxScroll = 0
    private var inputConfigName = ""
    private var activeTabX = 0f
    private var activeTabW = 0f

    override fun init() {
        super.init()
        rebuild()
    }

    private fun rebuild() {
        cards.clear()
        if (selectedCategory != Category.CONFIGS) {
            ModuleManager.getByCategory(selectedCategory).forEach { cards.add(ModuleCard(it)) }
        }
        layout()
    }

    private fun layout() {
        if (selectedCategory == Category.CONFIGS) {
            val panelW = width - VoltHackTheme.PANEL_PADDING * 2
            val startListY = VoltHackTheme.TAB_HEIGHT + VoltHackTheme.PANEL_PADDING + 45
            val rowH = 30
            val configs = ConfigManager.getConfigs()
            val contentH = 45 + configs.size * (rowH + 6)
            val panelH = height - (VoltHackTheme.TAB_HEIGHT + VoltHackTheme.PANEL_PADDING) - VoltHackTheme.PANEL_PADDING
            maxScroll = (contentH - panelH).coerceAtLeast(0)
            scrollOffset = scrollOffset.coerceIn(0, maxScroll)
            return
        }

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
        SettingWidget.hoveredSetting = null
        ctx.fill(0, 0, width, height, VoltHackTheme.overlay)
        renderTabs(ctx, mouseX, mouseY)

        if (selectedCategory == Category.CONFIGS) {
            renderConfigsCategory(ctx, mouseX, mouseY)
            return
        }

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

        if (hoveredName.isNotEmpty() && SettingWidget.hoveredSetting == null) {
            val desc = LanguageManager.get("module.$hoveredName.description")
            TooltipRenderer.render(ctx, desc, mouseX, mouseY)
        }

        val hoveredS = SettingWidget.hoveredSetting
        if (hoveredS != null && hoveredS.description.isNotEmpty()) {
            TooltipRenderer.render(ctx, hoveredS.description, mouseX, mouseY)
        }
    }

    private fun renderConfigsCategory(ctx: GuiGraphics, mouseX: Int, mouseY: Int) {
        val panelX = VoltHackTheme.PANEL_PADDING
        val panelY = VoltHackTheme.TAB_HEIGHT + VoltHackTheme.PANEL_PADDING
        val panelW = width - VoltHackTheme.PANEL_PADDING * 2
        val panelH = height - panelY - VoltHackTheme.PANEL_PADDING

        // Draw main container
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, VoltHackTheme.surface)

        // Draw textbox and Create button
        val inputY = panelY + 12
        val inputW = (panelW - 120).coerceAtLeast(100)
        val inputH = 22
        
        val isHoveredInput = mouseX in (panelX + 12)..(panelX + 12 + inputW) && mouseY in inputY..(inputY + inputH)
        ctx.fill(panelX + 12, inputY, panelX + 12 + inputW, inputY + inputH, if (isHoveredInput) VoltHackTheme.surfaceHover else VoltHackTheme.surfaceLight)
        
        val dispText = if (inputConfigName.isEmpty()) "Type config name..." else inputConfigName
        val dispColor = if (inputConfigName.isEmpty()) VoltHackTheme.textDisabled else VoltHackTheme.textPrimary
        GUIFontRenderer.draw(ctx, dispText, (panelX + 20).toFloat(), (inputY + (inputH - GUIFontRenderer.height) / 2f), dispColor)

        // CREATE button
        val btnX = panelX + 12 + inputW + 8
        val btnW = 88
        val btnH = 22
        val isHoveredCreate = mouseX in btnX..(btnX + btnW) && mouseY in inputY..(inputY + btnH)
        val createColor = if (isHoveredCreate) 0xFF00D4FF.toInt() else 0x4000D4FF.toInt()
        ctx.fill(btnX, inputY, btnX + btnW, inputY + btnH, createColor)
        GUIFontRenderer.drawCentered(ctx, "CREATE", (btnX + btnW / 2f), (inputY + (btnH - GUIFontRenderer.height) / 2f), VoltHackTheme.textPrimary)

        // Draw list of configs
        val startListY = inputY + 32
        val rowH = 30
        val configs = ConfigManager.getConfigs()

        configs.forEachIndexed { index, config ->
            val rowY = startListY + index * (rowH + 6) - scrollOffset
            if (rowY + rowH < startListY || rowY > panelY + panelH - 12) return@forEachIndexed

            // Row background
            ctx.fill(panelX + 12, rowY, panelX + panelW - 12, rowY + rowH, VoltHackTheme.surfaceLight)

            // Config name
            GUIFontRenderer.draw(ctx, "${config.name}.vlth", (panelX + 22).toFloat(), (rowY + (rowH - GUIFontRenderer.height) / 2f), VoltHackTheme.textPrimary)

            // Control Buttons
            val loadX = panelX + panelW - 192
            val saveX = panelX + panelW - 132
            val delX = panelX + panelW - 72
            val ctrlW = 52
            val ctrlH = 20
            val ctrlY = rowY + (rowH - ctrlH) / 2

            // LOAD
            val isHoveredLoad = mouseX in loadX..(loadX + ctrlW) && mouseY in ctrlY..(ctrlY + ctrlH)
            ctx.fill(loadX, ctrlY, loadX + ctrlW, ctrlY + ctrlH, if (isHoveredLoad) 0xFF2ED573.toInt() else 0x402ED573.toInt())
            GUIFontRenderer.drawCentered(ctx, "LOAD", (loadX + ctrlW / 2f), (ctrlY + (ctrlH - GUIFontRenderer.height) / 2f), VoltHackTheme.textPrimary)

            // SAVE
            val isHoveredSave = mouseX in saveX..(saveX + ctrlW) && mouseY in ctrlY..(ctrlY + ctrlH)
            ctx.fill(saveX, ctrlY, saveX + ctrlW, ctrlY + ctrlH, if (isHoveredSave) 0xFF1E90FF.toInt() else 0x401E90FF.toInt())
            GUIFontRenderer.drawCentered(ctx, "SAVE", (saveX + ctrlW / 2f), (ctrlY + (ctrlH - GUIFontRenderer.height) / 2f), VoltHackTheme.textPrimary)

            // DELETE
            val isHoveredDel = mouseX in delX..(delX + ctrlW) && mouseY in ctrlY..(ctrlY + ctrlH)
            ctx.fill(delX, ctrlY, delX + ctrlW, ctrlY + ctrlH, if (isHoveredDel) 0xFFFF4757.toInt() else 0x40FFFF4757.toInt())
            GUIFontRenderer.drawCentered(ctx, "DEL", (delX + ctrlW / 2f), (ctrlY + (ctrlH - GUIFontRenderer.height) / 2f), VoltHackTheme.textPrimary)
        }
    }

    private fun renderTabs(ctx: GuiGraphics, mouseX: Int, mouseY: Int) {
        ctx.fill(0, 0, width, VoltHackTheme.TAB_HEIGHT, VoltHackTheme.background)

        // Smooth active tab transition calculations
        var targetX = 0f
        var targetW = 0f
        var tx = VoltHackTheme.PANEL_PADDING
        for (cat in Category.entries) {
            val tw = GUIFontRenderer.width(cat.displayName) + VoltHackTheme.TAB_PADDING * 2 + 8
            if (cat == selectedCategory) {
                targetX = tx.toFloat()
                targetW = tw.toFloat()
                break
            }
            tx += tw + 4
        }

        if (activeTabX == 0f) {
            activeTabX = targetX
            activeTabW = targetW
        } else {
            activeTabX += (targetX - activeTabX) * 0.18f
            activeTabW += (targetW - activeTabW) * 0.18f
        }

        tx = VoltHackTheme.PANEL_PADDING
        for (cat in Category.entries) {
            val text = cat.displayName
            val tw = GUIFontRenderer.width(text) + VoltHackTheme.TAB_PADDING * 2 + 8
            val selected = cat == selectedCategory
            val color = if (selected) (VoltHackTheme.categoryColors[cat] ?: VoltHackTheme.accent) else VoltHackTheme.textSecondary

            ctx.fill(tx, 0, tx + tw, VoltHackTheme.TAB_HEIGHT, if (selected) VoltHackTheme.surfaceLight else VoltHackTheme.background)
            GUIFontRenderer.drawCentered(ctx, text, (tx + tw / 2f), (VoltHackTheme.TAB_HEIGHT - GUIFontRenderer.height) / 2f, color)

            tx += tw + 4
        }

        val activeColor = VoltHackTheme.categoryColors[selectedCategory] ?: VoltHackTheme.accent
        ctx.fill(activeTabX.toInt(), VoltHackTheme.TAB_HEIGHT - 3, (activeTabX + activeTabW).toInt(), VoltHackTheme.TAB_HEIGHT, activeColor)
    }

    override fun charTyped(characterEvent: CharacterEvent): Boolean {
        if (SettingWidget.activeInputSetting != null) {
            if (SettingWidget.charTyped(characterEvent)) return true
        }
        if (selectedCategory == Category.CONFIGS) {
            val char = characterEvent.codepoint().toChar()
            if (char.isLetterOrDigit() || char == '_' || char == '-') {
                if (inputConfigName.length < 16) {
                    inputConfigName += char
                }
                return true
            }
        }
        return super.charTyped(characterEvent)
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

        if (selectedCategory == Category.CONFIGS) {
            val panelX = VoltHackTheme.PANEL_PADDING
            val panelY = VoltHackTheme.TAB_HEIGHT + VoltHackTheme.PANEL_PADDING
            val panelW = width - VoltHackTheme.PANEL_PADDING * 2

            val inputY = panelY + 12
            val inputW = (panelW - 120).coerceAtLeast(100)
            
            // check CREATE button click
            val btnX = panelX + 12 + inputW + 8
            val btnW = 88
            val btnH = 22
            if (mx in btnX..(btnX + btnW) && my in inputY..(inputY + btnH)) {
                if (inputConfigName.isNotBlank()) {
                    ConfigManager.createConfig(inputConfigName.trim())
                    inputConfigName = ""
                    rebuild()
                }
                return true
            }

            // check list button clicks
            val startListY = inputY + 32
            val rowH = 30
            val configs = ConfigManager.getConfigs()
            configs.forEachIndexed { index, config ->
                val rowY = startListY + index * (rowH + 6) - scrollOffset
                val loadX = panelX + panelW - 192
                val saveX = panelX + panelW - 132
                val delX = panelX + panelW - 72
                val ctrlW = 52
                val ctrlH = 20
                val ctrlY = rowY + (rowH - ctrlH) / 2

                if (my in ctrlY..(ctrlY + ctrlH)) {
                    if (mx in loadX..(loadX + ctrlW)) {
                        ConfigManager.loadConfig(config.name)
                        return true
                    }
                    if (mx in saveX..(saveX + ctrlW)) {
                        ConfigManager.saveConfig(config.name)
                        return true
                    }
                    if (mx in delX..(delX + ctrlW)) {
                        ConfigManager.deleteConfig(config.name)
                        rebuild()
                        return true
                    }
                }
            }
            return super.mouseClicked(event, isInside)
        }

        val adjY = my + scrollOffset
        for (card in cards) {
            if (mx in card.x..card.x + VoltHackTheme.CARD_WIDTH &&
                adjY in card.y..card.y + card.height
            ) {
                if (card.expanded) {
                    if (card.mouseClicked(mx, adjY, button)) return true
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

    override fun mouseDragged(mouseButtonEvent: MouseButtonEvent, d: Double, e: Double): Boolean {
        if (SettingWidget.mouseDragged(mouseButtonEvent.x().toInt(), mouseButtonEvent.y().toInt(), mouseButtonEvent.button())) return true
        return super.mouseDragged(mouseButtonEvent, d, e)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (SettingWidget.activeInputSetting != null) {
            if (SettingWidget.keyPressed(event)) return true
        }
        if (event.key() == GLFW.GLFW_KEY_ESCAPE || event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            onClose()
            return true
        }
        if (selectedCategory == Category.CONFIGS) {
            if (event.key() == GLFW.GLFW_KEY_BACKSPACE && inputConfigName.isNotEmpty()) {
                inputConfigName = inputConfigName.substring(0, inputConfigName.length - 1)
                return true
            }
            if (event.key() == GLFW.GLFW_KEY_ENTER && inputConfigName.isNotBlank()) {
                ConfigManager.createConfig(inputConfigName.trim())
                inputConfigName = ""
                rebuild()
                return true
            }
        }
        return super.keyPressed(event)
    }

    override fun isPauseScreen() = false
}
