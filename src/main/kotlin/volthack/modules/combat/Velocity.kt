package volthack.modules.combat

import volthack.setting.Category
import volthack.setting.Module

object Velocity : Module("Velocity", "Reduces knockback", Category.COMBAT) {
    private val horizontal by float("Horizontal", 0f, 0f, 100f, 5f)
    private val vertical by float("Vertical", 0f, 0f, 100f, 5f)
}
