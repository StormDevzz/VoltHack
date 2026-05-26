package volthack.modules

import volthack.modules.combat.*
import volthack.modules.movement.*
import volthack.modules.world.*
import volthack.modules.render.*
import volthack.modules.misc.*
import volthack.modules.player.*
import volthack.setting.ModuleManager

object ModRegistry {
    private val modules = listOf(
        // Combat
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
        AutoBuff,
        BowSpam,
        AutoWeb,

        // Movement
        NoFall,
        FastLadder,
        FastSwim,
        Fly,
        AirJump,
        Jesus,
        Timer,
        Sneak,
        Scaffold,
        LevitCtrl,
        Step,
        FastFall,
        NoWeb,

        // Render
        FullBright,
        ESP,
        Notifications,
        Hud,
        FreeLook,
        NameTags,
        BlockOutline,
        PotatoMode,
        ViewClip,
        ViewModel,
        Ambience,
        Chams,
        NoBob,
        Spawner,
        Trajectories,
        CustomFog,
        NoRender,
        Tracers,
        PopChams,
        BreadCrumbs,
        FontModule,
        Glint,

        // World
        AutoFarm,
        AutoMount,
        BoneMeal,
        FakePlayer,
        StashFinder,
        HighwayMake,
        AutoBreed,
        AutoNameTag,
        AutoShearer,

        // Player
        ElytraSwap,
        AutoTool,
        AntiHunger,
        AutoReconnect,
        DiscordStatusModule,
        AutoFix,
        Replenish,
        AutoRespawn,
        FastBreak,
        AirPlace,
        AutoEat,
        MultiTask,
        Reach,
        FastUse,

        // Misc
        AutoAuth,
        PacketCanceller,
        AutoFish,
        MessageAura,
        AutoSign,
        SourceFiller,
        PingSpoof,
        HandShake,
        PacketLogger,
        Language,
        AntiAim
    )

    fun register() {
        ModuleManager.register(*modules.toTypedArray())
    }
}
