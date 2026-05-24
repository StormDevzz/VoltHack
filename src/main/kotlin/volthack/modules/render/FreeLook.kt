package volthack.modules.render

import net.minecraft.client.Minecraft
import volthack.setting.Category
import volthack.setting.Module

object FreeLook : Module("FreeLook", "Allows looking around without changing movement direction", Category.RENDER) {
    var freeYaw = 0f
    var freePitch = 0f

    override fun onEnable() {
        val player = Minecraft.getInstance().player ?: return
        freeYaw = player.yRot
        freePitch = player.xRot
    }

    fun onMouseTurn(yRot: Double, xRot: Double) {
        freeYaw += yRot.toFloat()
        freePitch += xRot.toFloat()
        freePitch = freePitch.coerceIn(-90f, 90f)
    }
}
