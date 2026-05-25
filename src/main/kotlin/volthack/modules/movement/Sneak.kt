package volthack.modules.movement

import net.minecraft.client.Minecraft
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module

object Sneak : Module("Sneak", "Automatically sneaks/crouches", Category.MOVEMENT) {
    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        mc.options.keyShift.isDown = true
    }

    override fun onDisable() {
        val mc = Minecraft.getInstance()
        mc.options.keyShift.isDown = false
    }
}
