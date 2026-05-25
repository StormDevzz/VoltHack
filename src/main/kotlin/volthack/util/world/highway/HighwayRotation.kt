package volthack.util.world.highway

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import volthack.manager.RotationManager

object HighwayRotation {
    fun rotateTo(pos: BlockPos) {
        val player = Minecraft.getInstance().player ?: return
        val target = Vec3(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
        
        val diffX = target.x - player.x
        val diffY = target.y - player.eyePosition.y
        val diffZ = target.z - player.z
        
        val diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ)
        
        val yaw = Math.toDegrees(Math.atan2(diffZ, diffX)).toFloat() - 90.0f
        val pitch = -Math.toDegrees(Math.atan2(diffY, diffXZ)).toFloat()

        RotationManager.setRotations(yaw, pitch, true)
    }
}
