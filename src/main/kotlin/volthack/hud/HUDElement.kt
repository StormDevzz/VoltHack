package volthack.hud

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import volthack.gui.HUDEditorScreen
import volthack.gui.ClickGUI
import volthack.setting.Setting

abstract class HUDElement(val name: String) {
    var x = 0
    var y = 0
    var enabled = true
    var cachedWidth = 0
    var cachedHeight = 0

    // Animation states
    var renderX = 0f
    var renderY = 0f
    var currentOpacity = 0f
    var scale = 0.95f

    val settings = mutableListOf<Setting<*>>()

    abstract fun draw(ctx: GuiGraphics)

    fun updateAnimation() {
        val mc = Minecraft.getInstance()
        val editorOpen = mc.screen is HUDEditorScreen
        
        val targetOpacity = if (enabled) 1f else if (editorOpen) 0.4f else 0f
        
        val speed = 0.18f
        
        if (renderX == 0f && x != 0) renderX = x.toFloat()
        if (renderY == 0f && y != 0) renderY = y.toFloat()
        
        renderX += (x - renderX) * speed
        renderY += (y - renderY) * speed
        currentOpacity += (targetOpacity - currentOpacity) * speed
        
        val targetScale = if (editorOpen || currentOpacity > 0.1f) 1.0f else 0.95f
        scale += (targetScale - scale) * speed
    }

    fun render(ctx: GuiGraphics) {
        updateAnimation()
        if (currentOpacity <= 0.01f) return

        val stack = (ctx as volthack.mixin.render.GuiGraphicsAccessor).pose
        stack.pushMatrix()

        stack.translate(renderX - x, renderY - y)

        val centerX = x + cachedWidth / 2f
        val centerY = y + cachedHeight / 2f
        stack.translate(centerX, centerY)
        stack.scale(scale, scale)
        stack.translate(-centerX, -centerY)

        draw(ctx)

        stack.popMatrix()
    }

    protected fun boolean(
        name: String,
        default: Boolean = false,
        description: String = ""
    ) = Setting.Boolean(name, description, default).also { settings.add(it) }

    protected fun float(
        name: String,
        default: Float = 0f,
        min: Float = 0f,
        max: Float = 1f,
        step: Float = 0.1f,
        description: String = ""
    ) = Setting.Float(name, description, default, min, max, step).also { settings.add(it) }

    protected fun int(
        name: String,
        default: Int = 0,
        min: Int = 0,
        max: Int = 10,
        description: String = ""
    ) = Setting.Int(name, description, default, min, max).also { settings.add(it) }

    protected fun mode(
        name: String,
        modes: List<String>,
        default: String = modes.first(),
        description: String = ""
    ) = Setting.Mode(name, description, default, modes).also { settings.add(it) }

    protected fun color(
        name: String,
        default: Int = 0xFFFFFFFF.toInt(),
        description: String = ""
    ) = Setting.Color(name, description, default).also { settings.add(it) }
}
