package volthack.util.misc

import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.Packet

object PacketUtils {
    private val mc get() = Minecraft.getInstance()
    private val player get() = mc.player

    fun sendPacket(packet: Packet<*>) {
        player?.connection?.send(packet)
    }
}
