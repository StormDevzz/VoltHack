package volthack.manager

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.client.multiplayer.resolver.ServerAddress
import net.minecraft.client.gui.screens.ConnectScreen
import net.minecraft.client.gui.screens.Screen

object NetworkManager {
    private var lastServerData: ServerData? = null
    private var lastServerAddress: ServerAddress? = null
    var isDisconnected = false
        private set

    fun onConnected(serverData: ServerData?) {
        lastServerData = serverData
        if (serverData != null) {
            lastServerAddress = ServerAddress.parseString(serverData.ip)
        }
        isDisconnected = false
    }

    fun onDisconnected(serverData: ServerData?) {
        if (serverData != null) {
            lastServerData = serverData
            lastServerAddress = ServerAddress.parseString(serverData.ip)
        }
        isDisconnected = true
    }

    fun reconnect() {
        val sd = lastServerData ?: return
        val sa = lastServerAddress ?: ServerAddress.parseString(sd.ip)
        val mc = Minecraft.getInstance()

        val screen: Screen = mc.screen ?: net.minecraft.client.gui.screens.TitleScreen()

        ConnectScreen.startConnecting(screen, mc, sa, sd, false, null)
    }

    fun hasServerData(): Boolean = lastServerData != null && lastServerAddress != null

    fun getLastServerIp(): String = lastServerData?.ip ?: ""

    fun reset() {
        lastServerData = null
        lastServerAddress = null
        isDisconnected = false
    }
}
