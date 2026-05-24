package volthack.modules.player

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.client.multiplayer.resolver.ServerAddress
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.manager.NetworkManager
import volthack.setting.Category
import volthack.setting.Module

object AutoReconnect : Module("AutoReconnect", "Automatically reconnect to servers", Category.PLAYER) {
    private val delay by int("Delay", 5, 1, 60)
    private val autoMode by boolean("Auto Reconnect", true)

    private var disconnectedAt = 0L
    private var reconnectTimer = 0
    private var shouldReconnect = false

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    fun onDisconnected(serverData: ServerData?, address: ServerAddress?) {
        if (!enabled) return
        if (serverData == null) return
        NetworkManager.onDisconnected(serverData)
        disconnectedAt = System.currentTimeMillis()

        if (autoMode) {
            reconnectTimer = delay * 20
            shouldReconnect = true
        }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        if (mc.level != null) {
            shouldReconnect = false
            reconnectTimer = 0
            return
        }

        if (shouldReconnect && reconnectTimer > 0) {
            reconnectTimer--
            if (reconnectTimer <= 0) {
                NetworkManager.reconnect()
                shouldReconnect = false
            }
        }
    }

    fun reconnect() {
        shouldReconnect = false
        NetworkManager.reconnect()
    }

    fun getTimerString(): String {
        if (!shouldReconnect || reconnectTimer <= 0) return ""
        val sec = reconnectTimer / 20
        return "Reconnecting in $sec..."
    }

    override fun onDisable() {
        shouldReconnect = false
        reconnectTimer = 0
    }
}
