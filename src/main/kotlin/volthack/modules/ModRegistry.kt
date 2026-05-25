package volthack.modules

import volthack.modules.combat.KillAura
import volthack.modules.combat.Velocity
import volthack.modules.combat.Trigger
import volthack.modules.combat.AutoCrystal
import volthack.modules.combat.OffHand
import volthack.modules.combat.MainHand
import volthack.modules.combat.HitSound
import volthack.modules.movement.NoFall
import volthack.modules.movement.FastLadder
import volthack.modules.movement.FastSwim
import volthack.modules.movement.Fly
import volthack.modules.movement.AirJump
import volthack.modules.combat.MaceSwap
import volthack.modules.combat.WindAura
import volthack.modules.movement.Jesus
import volthack.modules.movement.Timer
import volthack.modules.movement.Sneak
import volthack.modules.movement.Scaffold
import volthack.modules.world.BoneMeal
import volthack.modules.render.SmallUser
import volthack.modules.render.ShiftInterp
import volthack.modules.combat.MaceKill
import volthack.modules.misc.PingSpoof
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
        MaceSwap,
        WindAura,
        MaceKill,
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
        Jesus,
        Timer,
        Sneak,
        Scaffold,
        FullBright,
        ESP,
        Notifications,
        Hud,
        FreeLook,
        NameTags,
        BlockOutline,
        PotatoMode,
        SmallUser,
        ShiftInterp,
        ElytraSwap,
        AutoTool,
        AntiHunger,
        AutoReconnect,
        AutoFarm,
        AutoMount,
        BoneMeal,
        DiscordStatusModule,
        AutoAuth,
        PacketCanceller,
        AutoFish,
        MessageAura,
        AutoSign,
        SourceFiller,
        PingSpoof
    )

    fun register() {
        ModuleManager.register(*modules.toTypedArray())
    }
}
