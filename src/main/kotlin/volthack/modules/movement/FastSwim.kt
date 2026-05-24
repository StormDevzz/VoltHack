package volthack.modules.movement

import net.minecraft.client.Minecraft
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module

object FastSwim : Module("FastSwim", "Swim faster in water/lava", Category.MOVEMENT) {
    private val speed by float("Speed", 0.15f, 0.05f, 0.5f, 0.05f)
    private val mode by mode("Mode", listOf("Vanilla", "Strafe"), "Vanilla")

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val player = Minecraft.getInstance().player ?: return
        if (!player.isInWater && !player.isInLava) return

        if (mode == "Strafe") {
            val forward = player.zza
            val strafe = player.xxa
            if (forward == 0f && strafe == 0f) return

            var yaw = player.yRot
            if (strafe > 0f) yaw -= 45f
            else if (strafe < 0f) yaw += 45f

            val rad = Math.toRadians(yaw.toDouble())
            player.setDeltaMovement(
                -Math.sin(rad) * speed,
                player.deltaMovement.y,
                Math.cos(rad) * speed
            )
        } else {
            val forward = player.zza
            val strafe = player.xxa
            if (forward == 0f && strafe == 0f) return

            val yaw = player.yRot
            val rad = Math.toRadians(yaw.toDouble() + 90.0)
            val vx = (Math.cos(rad) * strafe + Math.cos(rad + Math.PI) * forward) * speed
            val vz = (Math.sin(rad) * strafe + Math.sin(rad + Math.PI) * forward) * speed

            player.setDeltaMovement(vx, player.deltaMovement.y, vz)
        }
    }
}
