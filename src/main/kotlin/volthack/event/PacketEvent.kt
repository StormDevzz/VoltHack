package volthack.event

import net.minecraft.network.protocol.Packet

class PacketSendEvent(val packet: Packet<*>) : Event()
class PacketReceiveEvent(val packet: Packet<*>) : Event()
