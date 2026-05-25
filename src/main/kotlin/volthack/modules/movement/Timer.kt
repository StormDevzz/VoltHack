package volthack.modules.movement

import volthack.setting.Category
import volthack.setting.Module

object Timer : Module("Timer", "Modifies client tick rate to move faster", Category.MOVEMENT) {
    val speed by float("Speed", 1.2f, 0.1f, 5.0f, 0.05f)

    fun getTimerSpeed(): Float {
        return if (enabled) speed else 1.0f
    }
}
