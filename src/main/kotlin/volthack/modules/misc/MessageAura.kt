package volthack.modules.misc

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.chat.ChatUtils

object MessageAura : Module("MessageAura", "Sends messages to nearby players", Category.MISC) {
    private val message by text("Message", "hello!")
    private val range by float("Range", 10.0f, 1f, 50f, 1f)
    private val mode by mode("Mode", listOf("Public", "Private"), "Public")
    private val delay by int("Delay (ms)", 3000, 500, 10000, "Delay between messages")
    private val onlyOnce by boolean("Only Once", true, "Only send to each player once")

    private var lastSendTime = 0L
    private val sentPlayers = mutableSetOf<String>()

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val world = mc.level ?: return

        if (System.currentTimeMillis() - lastSendTime < delay) return

        val targets = world.entitiesForRendering()
            .filterIsInstance<Player>()
            .filter { it != player && it.isAlive && player.distanceTo(it) <= range && !it.isDeadOrDying }

        for (target in targets) {
            val name = target.name.string
            if (onlyOnce && sentPlayers.contains(name)) continue
            if (mode == "Private") {
                ChatUtils.sendCommand("tell $name $message")
            } else {
                ChatUtils.sendMessage(message)
            }
            sentPlayers.add(name)
            lastSendTime = System.currentTimeMillis()
        }
    }

    override fun onDisable() {
        sentPlayers.clear()
        lastSendTime = 0L
    }
}
