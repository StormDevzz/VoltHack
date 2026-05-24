package volthack.setting

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import volthack.VoltHack
import java.nio.file.Path

object ModuleConfig {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val configDir: Path by lazy {
        FabricLoader.getInstance().gameDir.resolve("VoltHack/modules")
    }

    private val firstLaunchFlag: Path by lazy {
        FabricLoader.getInstance().gameDir.resolve("VoltHack/.initialized")
    }

    var isFirstLaunch: Boolean = true
        private set

    data class ModuleData(
        val enabled: Boolean = false,
        val bindKey: Int = 0
    )

    fun init() {
        configDir.toFile().mkdirs()
        isFirstLaunch = !firstLaunchFlag.toFile().exists()
        if (isFirstLaunch) {
            firstLaunchFlag.toFile().writeText("initialized")
            VoltHack.LOGGER.info("First launch detected — auto-enabling ONCE modules")
        }
    }

    fun load(moduleName: String): ModuleData {
        val file = configDir.resolve("$moduleName.json").toFile()
        if (!file.exists()) return ModuleData()
        return try {
            gson.fromJson(file.readText(), ModuleData::class.java) ?: ModuleData()
        } catch (e: Exception) {
            VoltHack.LOGGER.warn("Failed to load config for [$moduleName]: ${e.message}")
            ModuleData()
        }
    }

    fun save(moduleName: String, data: ModuleData) {
        val file = configDir.resolve("$moduleName.json").toFile()
        try {
            file.parentFile.mkdirs()
            file.writeText(gson.toJson(data))
        } catch (e: Exception) {
            VoltHack.LOGGER.warn("Failed to save config for [$moduleName]: ${e.message}")
        }
    }
}
