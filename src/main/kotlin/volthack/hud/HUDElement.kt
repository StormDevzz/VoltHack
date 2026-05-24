package volthack.hud

import net.minecraft.client.gui.GuiGraphics
import volthack.setting.Setting

abstract class HUDElement(val name: String) {
    var x = 0
    var y = 0
    var enabled = true
    var cachedWidth = 0
    var cachedHeight = 0

    val settings = mutableListOf<Setting<*>>()

    abstract fun render(ctx: GuiGraphics)

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
