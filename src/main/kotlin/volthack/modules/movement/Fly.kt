package volthack.modules.movement

import net.minecraft.client.Minecraft
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module

object Fly : Module("Fly", "Fly in creative-like mode", Category.MOVEMENT) {
    private val speed by float("Speed", 1.0f, 0.1f, 5.0f, 0.1f)
    private val mode by mode("Mode", listOf("Vanilla", "Motion"), "Vanilla")
    private val glide by boolean("Glide", false)

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        if (mode == "Vanilla") {
            player.abilities.mayfly = true
            player.abilities.flying = true
            player.abilities.flyingSpeed = speed / 10f
        } else {
            player.abilities.mayfly = false
            player.abilities.flying = false

            if (player.isInWater || player.isInLava) return

            val forward = player.zza
            val strafe = player.xxa

            if (forward == 0f && strafe == 0f) {
                if (glide) {
                    player.setDeltaMovement(player.deltaMovement.x, -0.05, player.deltaMovement.z)
                }
                return
            }

            var yaw = player.yRot
            var f = forward
            var s = strafe
            if (f != 0f) {
                if (s > 0f) yaw -= 45f
                else if (s < 0f) yaw += 45f
                s = 0f
            }
            if (f < 0f) yaw += 180f

            val rad = Math.toRadians(yaw.toDouble())
            val spd = speed.toDouble()
            val vx = -Math.sin(rad) * spd
            val vz = Math.cos(rad) * spd

            val jump = mc.options.keyJump.isDown
            val sneak = mc.options.keyShift.isDown
            val vy = if (jump) spd else if (sneak) -spd else 0.0

            player.setDeltaMovement(vx, vy, vz)
        }
    }

    override fun onDisable() {
        val player = Minecraft.getInstance().player ?: return
        player.abilities.mayfly = false
        player.abilities.flying = false
        player.abilities.flyingSpeed = 0.05f
    }
}
