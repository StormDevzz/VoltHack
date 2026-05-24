package volthack.util.chat

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object ChatUtils {
    private val mc get() = Minecraft.getInstance()
    private val player get() = mc.player

    fun sendMessage(text: String) {
        player?.connection?.sendChat(text)
    }

    fun sendCommand(command: String) {
        sendMessage("/$command")
    }

    fun addChatMessage(text: String) {
        player?.displayClientMessage(Component.literal(text), false)
    }

    fun addChatMessage(component: Component) {
        player?.displayClientMessage(component, false)
    }

    fun sendServerMessage(text: String) {
        sendMessage(text)
    }

    fun sendServerCommand(command: String) {
        sendCommand(command)
    }

    fun login(password: String) {
        sendCommand("login $password")
    }

    fun register(password: String) {
        sendCommand("register $password $password")
    }
}
