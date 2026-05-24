package volthack.modules.render

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.render.NotificationManager
import volthack.util.render.NotificationType

object Notifications : Module("Notifications", "Handles in-game notifications overlay", Category.RENDER) {
    val notifyModules by boolean("Module States", true, "Notify when modules are enabled or disabled")
    val notifyPlayers by boolean("Player Detect", true, "Notify when players enter visual range")
    val playSound by boolean("Play Sound", true, "Play sound on new notifications")

    private val trackedPlayers = mutableSetOf<String>()

    init {
        enabled = true // Enable by default for a stellar initial experience!
        EventBus.listen<TickEvent> { onTick() }
    }

    override fun onDisable() {
        trackedPlayers.clear()
    }

    private fun onTick() {
        if (!enabled) return

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val level = mc.level ?: return

        if (!notifyPlayers) {
            trackedPlayers.clear()
            return
        }

        val currentPlayers = level.players().filter { it != player && it.isAlive }
        val currentPlayerNames = currentPlayers.map { it.name.string }.toSet()

        // Detect entering players
        for (p in currentPlayers) {
            val name = p.name.string
            if (!trackedPlayers.contains(name)) {
                trackedPlayers.add(name)
                NotificationManager.add(
                    "Player Nearby",
                    "$name has entered your visual range!",
                    NotificationType.WARNING
                )
            }
        }

        // Detect leaving players
        val iterator = trackedPlayers.iterator()
        while (iterator.hasNext()) {
            val name = iterator.next()
            if (!currentPlayerNames.contains(name)) {
                iterator.remove()
                NotificationManager.add(
                    "Player Left",
                    "$name has left your visual range!",
                    NotificationType.INFO
                )
            }
        }
    }
}
