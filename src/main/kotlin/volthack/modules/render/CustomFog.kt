package volthack.modules.render

import volthack.setting.Category
import volthack.setting.Module

object CustomFog : Module("CustomFog", "Customizes fog and environment rendering", Category.RENDER) {
    val fogColor by color("Fog Color", 0xFF6C63FF.toInt(), "The color of the world fog")
}
