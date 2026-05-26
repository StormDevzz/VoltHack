package volthack.modules.player

import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.game.ServerboundUseItemPacket
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module

object FastUse : Module("FastUse", "Allows you to eat and use items at extreme speeds", Category.PLAYER) {
    private val speed by int("Speed", 20, 1, 40, "FastUse speed multiplier")

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        if (player.isUsingItem) {
            val hand = player.usedItemHand
            if (player.ticksUsingItem > 0) {
                repeat(speed) {
                    mc.connection?.send(ServerboundUseItemPacket(hand, 0, player.yRot, player.xRot))
                }
            }
        }
    }
}
