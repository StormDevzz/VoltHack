package volthack.modules.misc

import net.minecraft.client.Minecraft
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.AutoEnable
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.chat.ChatUtils

object AutoAuth : Module("AutoAuth", "Auto login/register on servers", Category.MISC, AutoEnable.NORMAL) {
    private val password by text("Password", "123123")
    private val delay by int("Delay", 40, 10, 100)

    private var tickCounter = 0
    private var attempted = false

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        if (Minecraft.getInstance().player == null) return
        if (Minecraft.getInstance().level == null) return
        if (attempted) return

        tickCounter++
        if (tickCounter < delay) return

        attempted = true
        ChatUtils.sendCommand("login $password")
    }

    override fun onDisable() {
        tickCounter = 0
        attempted = false
    }
}
