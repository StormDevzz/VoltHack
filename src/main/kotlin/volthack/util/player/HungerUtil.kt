package volthack.util.player

import net.minecraft.client.Minecraft

object HungerUtil {
    private val mc get() = Minecraft.getInstance()
    private val player get() = mc.player

    val foodLevel: Int get() = player?.foodData?.foodLevel ?: 20
    val saturation: Float get() = player?.foodData?.saturationLevel ?: 5f
    val isHungry: Boolean get() = foodLevel < 18

    fun resetExhaustion() {
        val foodData = player?.foodData ?: return
        try {
            val field = foodData.javaClass.getDeclaredField("exhaustionLevel")
            field.isAccessible = true
            field.setFloat(foodData, 0f)
        } catch (_: Exception) {}
    }

    fun sendSpoofedGroundPacket() {
        player?.connection?.send(net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly(true, false))
    }
}
