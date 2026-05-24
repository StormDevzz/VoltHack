package volthack.modules

import volthack.modules.combat.Aura
import volthack.modules.combat.Velocity
import volthack.modules.movement.NoFall
import volthack.modules.player.DiscordStatusModule
import volthack.modules.player.ElytraSwap
import volthack.modules.render.ESP
import volthack.modules.render.FullBright
import volthack.modules.render.Hud
import volthack.modules.world.AutoFarm
import volthack.setting.Module
import volthack.setting.ModuleManager

object ModRegistry {
    private val modules = listOf(
        Aura,
        Velocity,
        NoFall,
        FullBright,
        ESP,
        Hud,
        ElytraSwap,
        AutoFarm,
        DiscordStatusModule
    )

    fun register() {
        ModuleManager.register(*modules.toTypedArray())
    }
}
