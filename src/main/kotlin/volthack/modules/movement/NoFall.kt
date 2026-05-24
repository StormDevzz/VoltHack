package volthack.modules.movement

import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module

object NoFall : Module("NoFall", "Prevents fall damage", Category.MOVEMENT) {
    private val mode by mode("Mode", listOf("Packet", "Ground"), "Packet")

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val player = Minecraft.getInstance().player ?: return
        if (player.isFallFlying) return
        if (player.fallDistance <= ((player.health + 2.0).coerceAtMost(3.0))) return

        player.connection?.send(ServerboundMovePlayerPacket.StatusOnly(true, false))
        player.fallDistance = 0.0
    }
}
