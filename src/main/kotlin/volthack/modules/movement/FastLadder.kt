package volthack.modules.movement

import net.minecraft.client.Minecraft
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module

object FastLadder : Module("FastLadder", "Climb ladders faster", Category.MOVEMENT) {
    private val speed by float("Speed", 0.2f, 0.1f, 1.0f, 0.05f)

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val player = Minecraft.getInstance().player ?: return
        if (!player.onClimbable()) return
        if (!player.horizontalCollision && player.xxa == 0f && player.zza == 0f) return

        player.setDeltaMovement(player.deltaMovement.x, speed.toDouble(), player.deltaMovement.z)
    }
}
