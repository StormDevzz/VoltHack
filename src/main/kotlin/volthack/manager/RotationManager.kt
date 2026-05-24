package volthack.manager

import net.minecraft.client.Minecraft

object RotationManager {
    var yaw = 0f
    var pitch = 0f
    var isRotating = false
    var silent = false

    fun setRotations(targetYaw: Float, targetPitch: Float, isSilent: Boolean) {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        yaw = targetYaw
        pitch = targetPitch
        isRotating = true
        silent = isSilent

        if (!silent) {
            player.yRot = targetYaw
            player.yRotO = targetYaw
            player.xRot = targetPitch
            player.xRotO = targetPitch
        }
    }

    fun reset() {
        isRotating = false
        silent = false
    }

    fun wrapDegrees(value: Float): Float {
        var deg = value % 360f
        if (deg >= 180f) deg -= 360f
        if (deg < -180f) deg += 360f
        return deg
    }

    /**
     * Executes the given [action] block with the rotations temporarily applied to the player.
     * This is useful for placing or attacking silently.
     */
    inline fun runRotation(action: () -> Unit) {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        if (isRotating) {
            val prevYaw = player.yRot
            val prevPitch = player.xRot
            player.yRot = yaw
            player.xRot = pitch
            
            action()
            
            player.yRot = prevYaw
            player.xRot = prevPitch
        } else {
            action()
        }
    }
}
