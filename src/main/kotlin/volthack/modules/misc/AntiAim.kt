package volthack.modules.misc

import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.event.PacketSendEvent
import volthack.setting.Category
import volthack.setting.Module
import kotlin.random.Random

object AntiAim : Module("AntiAim", "Spins and jitters your rotations frantically to make you harder to hit", Category.MISC) {
    private val yawMode by mode("Yaw Mode", listOf("Spin", "Jitter", "Random", "Static"), "Spin")
    private val spinSpeed by float("Spin Speed", 40.0f, 5.0f, 180.0f, 5.0f)
    private val pitchMode by mode("Pitch Mode", listOf("Down", "Up", "Jitter", "Random", "Zero"), "Down")
    private val silent by boolean("Silent", false, "Whether rotations are server-side only")

    private var currentSpin = 0f

    init {
        EventBus.listen<TickEvent> { onTick() }
        EventBus.listen<PacketSendEvent> { onPacketSend(it) }
    }

    private fun getTargetYaw(baseYaw: Float, tickCount: Int): Float {
        return when (yawMode) {
            "Spin" -> {
                currentSpin = (currentSpin + spinSpeed) % 360f
                currentSpin
            }
            "Jitter" -> {
                baseYaw + (if (tickCount % 2 == 0) 90f else -90f)
            }
            "Random" -> {
                Random.nextFloat() * 360f
            }
            "Static" -> {
                baseYaw + 180f
            }
            else -> baseYaw
        }
    }

    private fun getTargetPitch(basePitch: Float, tickCount: Int): Float {
        return when (pitchMode) {
            "Down" -> 90f
            "Up" -> -90f
            "Jitter" -> if (tickCount % 2 == 0) 90f else -90f
            "Random" -> Random.nextFloat() * 180f - 90f
            "Zero" -> 0f
            else -> basePitch
        }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        if (!silent) {
            val targetYaw = getTargetYaw(player.yRot, player.tickCount)
            val targetPitch = getTargetPitch(player.xRot, player.tickCount)
            player.yRot = targetYaw
            player.xRot = targetPitch
            player.yRotO = targetYaw
            player.xRotO = targetPitch
        }
    }

    private fun onPacketSend(event: PacketSendEvent) {
        if (!enabled || !silent) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val packet = event.packet

        if (packet is ServerboundMovePlayerPacket) {
            // Check if the packet subclass has rotation fields (Rot or PosRot)
            val hasRotation = packet is ServerboundMovePlayerPacket.Rot || packet is ServerboundMovePlayerPacket.PosRot
            if (hasRotation) {
                val targetYaw = getTargetYaw(player.yRot, player.tickCount)
                val targetPitch = getTargetPitch(player.xRot, player.tickCount)
                
                try {
                    val yawField = ServerboundMovePlayerPacket::class.java.getDeclaredField("yRot")
                    val pitchField = ServerboundMovePlayerPacket::class.java.getDeclaredField("xRot")
                    yawField.isAccessible = true
                    pitchField.isAccessible = true
                    
                    yawField.setFloat(packet, targetYaw)
                    pitchField.setFloat(packet, targetPitch)
                } catch (e: Exception) {
                    // Fail-safe: if reflection fails, fall back to direct player rotation modification
                    player.yRot = targetYaw
                    player.xRot = targetPitch
                }
            }
        }
    }
}
