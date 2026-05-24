package volthack.modules

import volthack.modules.combat.KillAura
import volthack.modules.combat.Velocity
import volthack.modules.combat.Trigger
import volthack.modules.combat.AutoCrystal
import volthack.modules.combat.OffHand
import volthack.modules.combat.MainHand
import volthack.modules.movement.NoFall
import volthack.modules.player.DiscordStatusModule
import volthack.modules.player.ElytraSwap
import volthack.modules.player.AutoTool
import volthack.modules.render.ESP
import volthack.modules.render.FullBright
import volthack.modules.render.Hud
import volthack.modules.render.FreeLook
import volthack.modules.render.PotatoMode
import volthack.modules.render.TargetESP
import volthack.modules.render.Notifications
import volthack.modules.world.AutoFarm
import volthack.setting.Module
import volthack.setting.ModuleManager

object ModRegistry {
    private val modules = listOf(
        KillAura,
        Velocity,
        Trigger,
        AutoCrystal,
        OffHand,
        MainHand,
        NoFall,
        FullBright,
        ESP,
        TargetESP,
        Notifications,
        Hud,
        FreeLook,
        PotatoMode,
        ElytraSwap,
        AutoTool,
        AutoFarm,
        DiscordStatusModule
    )

    fun register() {
        ModuleManager.register(*modules.toTypedArray())
    }
}
