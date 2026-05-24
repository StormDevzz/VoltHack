package volthack.modules.player

import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import volthack.event.EventBus
import volthack.event.PacketSendEvent
import volthack.event.TickEvent
import volthack.manager.PacketManager
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.player.HungerUtil

object AntiHunger : Module("AntiHunger", "Prevents hunger loss using packets", Category.PLAYER) {
    private val mode by mode("Mode", listOf("SpoofGround", "NoExhaustion", "Both"), "Both")

    init {
        EventBus.listen<TickEvent> { onTick() }
        PacketManager.registerSendListener { onPacketSend(it) }
    }

    private fun onTick() {
        if (!enabled) return
        if (mode == "NoExhaustion" || mode == "Both") {
            HungerUtil.resetExhaustion()
        }
    }

    private var spoofing = false

    private fun onPacketSend(event: PacketSendEvent) {
        if (!enabled) return
        if (spoofing) return
        if (mode != "SpoofGround" && mode != "Both") return
        if (event.packet !is ServerboundMovePlayerPacket) return

        spoofing = true
        Minecraft.getInstance().player?.connection?.send(ServerboundMovePlayerPacket.StatusOnly(true, false))
        spoofing = false
    }

    override fun onDisable() {
        spoofing = false
    }
}
