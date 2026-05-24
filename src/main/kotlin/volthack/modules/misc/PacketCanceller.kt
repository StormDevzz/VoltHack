package volthack.modules.misc

import net.minecraft.network.protocol.game.ServerboundChatPacket
import net.minecraft.network.protocol.game.ServerboundInteractPacket
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.network.protocol.game.ServerboundSwingPacket
import net.minecraft.network.protocol.game.ServerboundUseItemPacket
import volthack.event.PacketSendEvent
import volthack.manager.PacketManager
import volthack.setting.Category
import volthack.setting.Module

object PacketCanceller : Module("PacketCanceller", "Cancel specific packets", Category.MISC) {
    private val cancelMovement by boolean("Cancel Movement", false)
    private val cancelRotation by boolean("Cancel Rotation", false)
    private val cancelSwing by boolean("Cancel Swing", false)
    private val cancelUseItem by boolean("Cancel Use Item", false)
    private val cancelInteract by boolean("Cancel Interact", false)
    private val cancelChat by boolean("Cancel Chat", false)

    init {
        PacketManager.registerSendListener { onPacketSend(it) }
    }

    private fun onPacketSend(event: PacketSendEvent) {
        if (!enabled) return
        val packet = event.packet

        when {
            cancelMovement && packet is ServerboundMovePlayerPacket -> event.cancelled = true
            cancelRotation && packet is ServerboundMovePlayerPacket.Rot -> event.cancelled = true
            cancelSwing && packet is ServerboundSwingPacket -> event.cancelled = true
            cancelUseItem && packet is ServerboundUseItemPacket -> event.cancelled = true
            cancelInteract && packet is ServerboundInteractPacket -> event.cancelled = true
            cancelChat && packet is ServerboundChatPacket -> event.cancelled = true
        }
    }
}
