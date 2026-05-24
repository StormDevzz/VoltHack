package volthack.modules.movement

import volthack.setting.Category
import volthack.setting.Module

object NoFall : Module("NoFall", "Prevents fall damage", Category.MOVEMENT) {
    private val mode by mode("Mode", listOf("Packet", "Ground"), "Packet")
}
