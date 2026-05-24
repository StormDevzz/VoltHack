package volthack.modules.movement

import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module

object AirJump : Module("AirJump", "Jump in mid-air", Category.MOVEMENT) {
    private val mode by mode("Mode", listOf("Packet", "Motion"), "Packet")

    private var wasOnGround = true
    private var jumpTicks = 0

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    override fun onEnable() {
        wasOnGround = Minecraft.getInstance().player?.onGround() ?: true
        jumpTicks = 0
    }

    private fun onTick() {
        if (!enabled) return
        val player = Minecraft.getInstance().player ?: return
        val mc = Minecraft.getInstance()

        if (player.onGround()) {
            wasOnGround = true
            jumpTicks = 0
            return
        }

        if (!mc.options.keyJump.isDown) {
            jumpTicks = 0
            return
        }

        if (jumpTicks > 0) {
            jumpTicks--
            return
        }

        if (mode == "Packet") {
            val conn = player.connection ?: return
            conn.send(ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING))
        } else {
            player.setDeltaMovement(player.deltaMovement.x, 0.42, player.deltaMovement.z)
        }

        jumpTicks = 4
    }
}
