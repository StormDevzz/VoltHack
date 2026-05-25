package volthack.modules.misc

import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket
import net.minecraft.network.protocol.common.ServerboundPongPacket
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.manager.PacketManager
import volthack.setting.Category
import volthack.setting.Module
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.CopyOnWriteArrayList

object PingSpoof : Module("PingSpoof", "Spoofs your connection latency/ping", Category.MISC) {
    private val delaySetting by int("Delay (ms)", 1000, 50, 60000, "Ping delay in milliseconds")

    private val packetQueue = CopyOnWriteArrayList<DelayedPacket>()
    private val relayedPackets = Collections.newSetFromMap(WeakHashMap<Packet<*>, Boolean>())

    private val sendListener = { event: volthack.event.PacketSendEvent ->
        val packet = event.packet
        if (enabled && (packet is ServerboundKeepAlivePacket || packet is ServerboundPongPacket)) {
            if (!relayedPackets.contains(packet)) {
                event.cancelled = true
                packetQueue.add(DelayedPacket(packet, System.currentTimeMillis() + delaySetting))
            }
        }
    }

    init {
        PacketManager.registerSendListener(sendListener)
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (packetQueue.isEmpty()) return
        val now = System.currentTimeMillis()
        val mc = Minecraft.getInstance()
        val conn = mc.connection?.connection ?: return

        val toRemove = mutableListOf<DelayedPacket>()
        for (dp in packetQueue) {
            if (now >= dp.time) {
                relayedPackets.add(dp.packet)
                conn.send(dp.packet)
                toRemove.add(dp)
            }
        }
        packetQueue.removeAll(toRemove)
    }

    override fun onDisable() {
        val mc = Minecraft.getInstance()
        val conn = mc.connection?.connection
        if (conn != null) {
            for (dp in packetQueue) {
                relayedPackets.add(dp.packet)
                conn.send(dp.packet)
            }
        }
        packetQueue.clear()
        relayedPackets.clear()
    }

    private class DelayedPacket(val packet: Packet<*>, val time: Long)
}
