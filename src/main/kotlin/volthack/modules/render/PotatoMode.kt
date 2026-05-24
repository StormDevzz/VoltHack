package volthack.modules.render

import net.minecraft.client.Minecraft
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.AutoEnable
import volthack.setting.Category
import volthack.setting.Module
import volthack.setting.ModuleManager

object PotatoMode : Module(
    name = "PotatoMode",
    description = "Performance optimizer",
    category = Category.MISC,
    autoEnable = AutoEnable.ALWAYS
) {
    private val unfocusedFps by boolean("Unfocused FPS Cap", true, "Caps FPS to 15 when game is unfocused")
    private val aggressiveGc by boolean("Aggressive GC", true, "Periodically clears RAM via JVM Garbage Collector")
    private val gcInterval by int("GC Interval (s)", 30, 10, 180, "Interval in seconds to run GC")
    private val gcOnWorldLoad by boolean("GC on World Load", true, "Clears memory when loading/leaving worlds")
    private val disableDiscord by boolean("Disable Discord RPC", true, "Disables Discord Rich Presence to save CPU")

    private var lastGcTime = 0L
    private var wasInWorld = false
    private var originalFps = 60
    private var modifiedFps = false

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    override fun onEnable() {
        lastGcTime = System.currentTimeMillis()
        wasInWorld = Minecraft.getInstance().level != null
        originalFps = Minecraft.getInstance().options.framerateLimit.get()
        modifiedFps = false
        
        if (disableDiscord) {
            ModuleManager.get("DiscordStatus")?.disable()
        }
        
        System.gc()
    }

    override fun onDisable() {
        val mc = Minecraft.getInstance()
        if (modifiedFps) {
            mc.options.framerateLimit.set(originalFps)
            modifiedFps = false
        }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()

        // 1. Unfocused FPS Limiter
        if (unfocusedFps) {
            val windowActive = mc.isWindowActive
            if (!windowActive) {
                val currentLimit = mc.options.framerateLimit.get()
                if (currentLimit != 15) {
                    if (!modifiedFps) {
                        originalFps = currentLimit
                        modifiedFps = true
                    }
                    mc.options.framerateLimit.set(15)
                }
            } else {
                if (modifiedFps) {
                    mc.options.framerateLimit.set(originalFps)
                    modifiedFps = false
                }
            }
        } else {
            if (modifiedFps) {
                mc.options.framerateLimit.set(originalFps)
                modifiedFps = false
            }
        }

        // 2. Aggressive GC
        if (aggressiveGc) {
            val now = System.currentTimeMillis()
            if (now - lastGcTime > gcInterval * 1000L) {
                System.gc()
                lastGcTime = now
            }
        }

        // 3. GC on World Load/Unload
        if (gcOnWorldLoad) {
            val inWorld = mc.level != null
            if (inWorld != wasInWorld) {
                System.gc()
                wasInWorld = inWorld
            }
        }
        
        // 4. Force disable discord if set
        if (disableDiscord) {
            val discordMod = ModuleManager.get("DiscordStatus")
            if (discordMod != null && discordMod.enabled) {
                discordMod.disable()
            }
        }
    }
}
