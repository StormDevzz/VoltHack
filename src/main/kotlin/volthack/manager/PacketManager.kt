package volthack.manager

import volthack.event.PacketReceiveEvent
import volthack.event.PacketSendEvent
import java.util.concurrent.CopyOnWriteArrayList

object PacketManager {
    private val sendListeners = CopyOnWriteArrayList<(PacketSendEvent) -> Unit>()
    private val receiveListeners = CopyOnWriteArrayList<(PacketReceiveEvent) -> Unit>()

    fun registerSendListener(handler: (PacketSendEvent) -> Unit) {
        sendListeners.add(handler)
    }

    fun unregisterSendListener(handler: (PacketSendEvent) -> Unit) {
        sendListeners.remove(handler)
    }

    fun registerReceiveListener(handler: (PacketReceiveEvent) -> Unit) {
        receiveListeners.add(handler)
    }

    fun unregisterReceiveListener(handler: (PacketReceiveEvent) -> Unit) {
        receiveListeners.remove(handler)
    }

    fun onPacketSend(event: PacketSendEvent): Boolean {
        for (handler in sendListeners) {
            handler(event)
            if (event.cancelled) return false
        }
        return !event.cancelled
    }

    fun onPacketReceive(event: PacketReceiveEvent): Boolean {
        for (handler in receiveListeners) {
            handler(event)
            if (event.cancelled) return false
        }
        return !event.cancelled
    }
}
