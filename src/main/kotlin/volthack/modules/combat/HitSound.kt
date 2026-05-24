package volthack.modules.combat

import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.game.ServerboundInteractPacket
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import volthack.event.PacketSendEvent
import volthack.event.TickEvent
import volthack.event.EventBus
import volthack.manager.PacketManager
import volthack.setting.Category
import volthack.setting.Module

object HitSound : Module("HitSound", "Plays a sound when you hit an entity", Category.COMBAT) {
    private val soundMode by mode("Sound", listOf("Sound1", "Sound2", "Sound3", "Sound4", "Sound5"), "Sound1")
    private val volume by float("Volume", 1.0f, 0.1f, 2.0f, 0.1f)

    private var lastCooldown = 1.0f
    private var attackSent = false

    init {
        EventBus.listen<TickEvent> { onTick() }
        PacketManager.registerSendListener { onPacketSend(it) }
    }

    private fun onPacketSend(event: PacketSendEvent) {
        if (!enabled) return
        if (event.packet is ServerboundInteractPacket) {
            attackSent = true
        }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        if (attackSent) {
            attackSent = false
            val sound = getSound()
            player.playSound(sound, volume, 1.0f)
        }
    }

    private fun getSound(): SoundEvent {
        return when (soundMode) {
            "Sound1" -> SoundEvents.EXPERIENCE_ORB_PICKUP
            "Sound2" -> SoundEvents.PLAYER_LEVELUP
            "Sound3" -> SoundEvents.NOTE_BLOCK_CHIME.value()
            "Sound4" -> SoundEvents.ARROW_HIT_PLAYER
            "Sound5" -> SoundEvents.FIREWORK_ROCKET_TWINKLE
            else -> SoundEvents.EXPERIENCE_ORB_PICKUP
        }
    }

    override fun onDisable() {
        attackSent = false
    }
}
