package volthack.modules.render

import volthack.setting.Category
import volthack.setting.Module

object ESP : Module("ESP", "Highlights entities", Category.RENDER) {
    private val color by mode("Color", listOf("Red", "Green", "Blue", "White"), "Red")
    private val mode by mode("Mode", listOf("Box", "Outline", "Both"), "Both")
}
