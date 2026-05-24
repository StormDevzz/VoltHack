package volthack.modules.render

import net.minecraft.client.CameraType
import net.minecraft.client.Minecraft
import volthack.setting.Category
import volthack.setting.Module

object FreeLook : Module("FreeLook", "Free camera in third-person without changing player movement", Category.RENDER) {
    private val speed by float("Speed", 0.6f, 0.1f, 2.0f, 0.05f)

    var freeYaw = 0f
    var freePitch = 0f
    private var prevCameraType: CameraType? = null

    override fun onEnable() {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        freeYaw = player.yRot
        freePitch = player.xRot
        prevCameraType = mc.options.cameraType
        if (mc.options.cameraType == CameraType.FIRST_PERSON) {
            mc.options.cameraType = CameraType.THIRD_PERSON_BACK
        }
    }

    override fun onDisable() {
        val mc = Minecraft.getInstance()
        prevCameraType?.let { mc.options.cameraType = it }
        prevCameraType = null
    }

    fun onMouseTurn(yRot: Double, xRot: Double) {
        freeYaw += (yRot * speed).toFloat()
        freePitch += (xRot * speed).toFloat()
        freePitch = freePitch.coerceIn(-90f, 90f)
    }
}
