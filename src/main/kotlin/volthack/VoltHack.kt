package volthack

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import volthack.gui.ClickGUI
import volthack.gui.loading.LoadingState
import volthack.hud.HUDManager
import volthack.hud.elements.ArrayListElement
import volthack.hud.elements.WatermarkElement
import volthack.lang.LanguageManager
import volthack.modules.ModRegistry
import volthack.setting.ModuleConfig
import volthack.setting.ModuleManager
import volthack.util.discord.DiscordRPC
import volthack.util.github.GitHubClient
import kotlin.concurrent.thread

class VoltHack : ClientModInitializer {
    private val github = GitHubClient("StormDevzz/VoltHack")

    override fun onInitializeClient() {
        val start = System.currentTimeMillis()
        LOGGER.info("===== Initializing VoltHack v$version =====")
        LoadingState.reset()
        val runtime = Runtime.getRuntime()
        val used = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        LOGGER.debug("Memory: ${used}MB used / ${runtime.maxMemory() / 1024 / 1024}MB max")

        // 1. Load languages
        LoadingState.step(0.10f, "Loading languages...",
            "[1/7] Loading language files...")
        LOGGER.info("[1/7] Loading language files...")
        LanguageManager.initialize()
        LOGGER.info("       -> Language manager ready")

        // 2. Register modules
        LoadingState.step(0.28f, "Registering modules...",
            "[2/7] Registering modules...")
        LOGGER.info("[2/7] Registering modules...")
        ModRegistry.register()
        LOGGER.info("       -> ${ModuleManager.getAll().size} modules registered")
        ModuleManager.getAll().forEach {
            LOGGER.info("          - ${it.name} [${it.category.displayName}]")
        }

        // 3. Load module configs
        LoadingState.step(0.42f, "Loading module configs...",
            "[3/7] Loading module configurations...")
        LOGGER.info("[3/7] Loading module configurations...")
        ModuleConfig.init()
        ModuleManager.loadConfigs()

        // 4. Initialize HUD
        LoadingState.step(0.55f, "Initializing HUD...",
            "[4/7] Initializing HUD elements...")
        LOGGER.info("[4/7] Initializing HUD elements...")
        HUDManager.register(WatermarkElement())
        HUDManager.register(ArrayListElement())
        HUDManager.load()
        LOGGER.info("       -> ${HUDManager.getAll().size} HUD elements loaded")

        // 5. Check GitHub
        LoadingState.step(0.70f, "Checking GitHub...",
            "[5/7] Checking GitHub connectivity...")
        LOGGER.info("[5/7] Checking GitHub connectivity...")
        thread { checkGitHub() }

        // 6. Setup keybinds
        LoadingState.step(0.82f, "Setting up keybinds...",
            "[6/7] Setting up keybinds...")
        LOGGER.info("[6/7] Setting up keybinds...")
        LOGGER.info("       -> ClickGUI: RSHIFT")
        LOGGER.info("       -> DiscordStatus: toggle in GUI")

        // 7. Initialize Discord
        LoadingState.step(0.92f, "Connecting Discord...",
            "[7/7] Initializing Discord RPC...")
        LOGGER.info("[7/7] Initializing Discord RPC...")
        thread { initDiscord() }

        // Done
        val elapsed = System.currentTimeMillis() - start
        LoadingState.step(0.95f, "Finalizing...")
        LoadingState.finish()
        LOGGER.info("===== VoltHack initialized (${elapsed}ms) =====")
        val runtime2 = Runtime.getRuntime()
        val used2 = (runtime2.totalMemory() - runtime2.freeMemory()) / 1024 / 1024
        LOGGER.debug("Memory: ${used2}MB used / ${runtime2.maxMemory() / 1024 / 1024}MB max")
    }

    private fun checkGitHub() {
        try {
            LOGGER.info("       -> Fetching latest release...")
            val release = github.getLatestRelease()
            if (release != null) {
                LOGGER.info("       -> GitHub OK - latest: ${release.tag}")
                val update = github.checkUpdate(version)
                if (update != null) {
                    LOGGER.info("       -> Update available: ${update.tag}")
                } else {
                    LOGGER.info("       -> Version is up to date")
                }
            } else {
                LOGGER.info("       -> GitHub: no release found")
            }
        } catch (e: Exception) {
            LOGGER.warn("       -> GitHub check failed: ${e.message}")
        }
    }

    private fun initDiscord() {
        DiscordRPC.start()
        val connected = DiscordRPC.isConnected
        if (connected) {
            LOGGER.info("       -> Discord RPC connected")
        } else {
            LOGGER.info("       -> Discord RPC unavailable (Discord not running?)")
        }
    }

    companion object {
        const val MOD_ID = "volthack"
        val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

        val KEY_BINDING = KeyMapping(
            "key.volthack.open_gui",
            GLFW_KEY_RIGHT_SHIFT,
            KeyMapping.Category.MISC
        )

        @JvmStatic
        val version: String by lazy {
            FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .orElseThrow()
                .metadata
                .version
                .friendlyString
        }

        @JvmStatic
        fun onClientTick() {
            val mc = Minecraft.getInstance()
            while (KEY_BINDING.consumeClick()) {
                when (mc.screen) {
                    is ClickGUI -> mc.setScreen(null)
                    else -> mc.setScreen(ClickGUI())
                }
            }
        }
    }
}
