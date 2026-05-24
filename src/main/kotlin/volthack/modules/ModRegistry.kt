package volthack.modules

import volthack.modules.combat.KillAura
import volthack.modules.combat.Velocity
import volthack.modules.combat.Trigger
import volthack.modules.combat.AutoCrystal
import volthack.modules.combat.OffHand
import volthack.modules.combat.MainHand
import volthack.modules.combat.HitSound
import volthack.modules.render.CoolHat
import volthack.modules.movement.NoFall
import volthack.modules.movement.FastLadder
import volthack.modules.movement.FastSwim
import volthack.modules.movement.Fly
import volthack.modules.movement.AirJump
import volthack.modules.player.DiscordStatusModule
import volthack.modules.player.ElytraSwap
import volthack.modules.player.AutoTool
import volthack.modules.player.AntiHunger
import volthack.modules.player.AutoReconnect
import volthack.modules.render.ESP
import volthack.modules.render.FullBright
import volthack.modules.render.Hud
import volthack.modules.render.FreeLook
import volthack.modules.render.PotatoMode
import volthack.modules.render.Notifications
import volthack.modules.render.BlockOutline
import volthack.modules.render.NameTags
import volthack.modules.world.AutoFarm
import volthack.modules.world.AutoMount
import volthack.modules.misc.AutoAuth
import volthack.modules.misc.PacketCanceller
import volthack.modules.misc.AutoFish
import volthack.modules.misc.MessageAura
import volthack.modules.misc.AutoSign
import volthack.modules.misc.SourceFiller
import volthack.setting.ModuleManager

object ModRegistry {
    private val modules = listOf(
        KillAura,
        Velocity,
        Trigger,
        AutoCrystal,
        OffHand,
        MainHand,
        HitSound,
        NoFall,
        FastLadder,
        FastSwim,
        Fly,
        AirJump,
        FullBright,
        ESP,
        Notifications,
        Hud,
        FreeLook,
        NameTags,
        BlockOutline,
        PotatoMode,
        CoolHat,
        ElytraSwap,
        AutoTool,
        AntiHunger,
        AutoReconnect,
        AutoFarm,
        AutoMount,
        DiscordStatusModule,
        AutoAuth,
        PacketCanceller,
        AutoFish,
        MessageAura,
        AutoSign,
        SourceFiller
    )

    fun register() {
        ModuleManager.register(*modules.toTypedArray())
    }
}
