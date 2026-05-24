package volthack.modules.player

import net.minecraft.client.Minecraft
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.AutoEnable
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.discord.DiscordRPC

object DiscordStatusModule : Module(
    name = "DiscordStatus",
    description = "Shows your game state in Discord Rich Presence",
    category = Category.PLAYER,
    autoEnable = AutoEnable.ONCE
) {
    private var lastState = ""
    private var tickCounter = 0

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    override fun onEnable() {
        if (!DiscordRPC.isConnected) {
            DiscordRPC.start()
        }
        tickCounter = 0
        updatePresence(force = true)
    }

    override fun onDisable() {
        DiscordRPC.stop()
    }

    private fun onTick() {
        if (!enabled) return
        tickCounter++
        
        val stateChanged = updatePresence(force = false)
        if (stateChanged || tickCounter >= 100) {
            tickCounter = 0
            if (!stateChanged) {
                updatePresence(force = true)
            }
        }
    }

    private fun updatePresence(force: Boolean = false): Boolean {
        val mc = Minecraft.getInstance()

        val state = when {
            mc.level == null -> "In Main Menu"
            mc.isLocalServer || mc.hasSingleplayerServer() -> "Playing Singleplayer"
            else -> {
                val server = mc.currentServer
                if (server != null) "Playing on ${server.name.take(40)}"
                else "In Game"
            }
        }

        if (state != lastState || force) {
            lastState = state
            val details = when {
                mc.level == null -> "VoltHack v${volthack.VoltHack.version}"
                mc.isLocalServer || mc.hasSingleplayerServer() -> "Playing Singleplayer"
                else -> {
                    val server = mc.currentServer
                    if (server != null) "Online: ${server.name.take(30)}"
                    else "In Game"
                }
            }
            DiscordRPC.update(details = details, state = state, largeImage = "volt")
            return true
        }
        return false
    }
}
