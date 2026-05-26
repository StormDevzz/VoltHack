package volthack.modules.render

import volthack.setting.Category
import volthack.setting.Module

object Glint : Module("Glint", "Changes enchantment glint color on items and armor", Category.RENDER) {
    val glintColor by color("Color", 0xFF6C63FF.toInt(), "The glint overlay color")
    val strength by float("Strength", 1.0f, 0.0f, 1.0f, 0.05f, "Glint opacity multiplier")
    val rainbow by boolean("Rainbow", false, "Cycle glint through rainbow colors")

    /** Called from the mixin to get the current glint RGBA components. */
    fun getGlintRGBA(): FloatArray {
        val col = if (rainbow) {
            val hue = ((System.currentTimeMillis() * 0.0005f) % 1.0f)
            java.awt.Color.HSBtoRGB(hue, 0.9f, 1.0f)
        } else {
            glintColor
        }
        val r = ((col shr 16) and 0xFF) / 255.0f
        val g = ((col shr 8) and 0xFF) / 255.0f
        val b = (col and 0xFF) / 255.0f
        val a = strength
        return floatArrayOf(r, g, b, a)
    }
}
