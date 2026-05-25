package volthack.modules.player

import volthack.setting.Category
import volthack.setting.Module

object FastBreak : Module("FastBreak", "Removes block breaking delay and increases mining speed legitimately", Category.PLAYER) {
    val speed by float("Speed Multiplier", 1.4f, 1.0f, 3.0f, 0.1f)
}
