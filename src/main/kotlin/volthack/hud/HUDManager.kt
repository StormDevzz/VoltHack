package volthack.hud

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import volthack.VoltHack
import java.nio.file.Path

object HUDManager {
    private val elements = mutableListOf<HUDElement>()
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val configFile: Path by lazy {
        FabricLoader.getInstance().gameDir.resolve("VoltHack/hud.json")
    }

    fun register(element: HUDElement) {
        elements.add(element)
    }

    fun render(ctx: GuiGraphics) {
        for (element in elements) {
            element.render(ctx)
        }
    }

    fun getAll(): List<HUDElement> = elements.toList()
    fun get(name: String): HUDElement? = elements.find { it.name == name }

    fun load() {
        try {
            if (!configFile.toFile().exists()) {
                VoltHack.LOGGER.info("       -> No HUD config found, using defaults")
                return
            }
            val json = configFile.toFile().readText()
            val type = object : TypeToken<Map<String, Map<String, Any>>>() {}.type
            val data: Map<String, Map<String, Any>> = gson.fromJson(json, type)

            var loaded = 0
            for ((name, props) in data) {
                val element = get(name) ?: continue
                element.x = (props["x"] as? Double)?.toInt() ?: element.x
                element.y = (props["y"] as? Double)?.toInt() ?: element.y
                element.enabled = (props["enabled"] as? Boolean) ?: element.enabled

                @Suppress("UNCHECKED_CAST")
                val settingsData = props["settings"] as? Map<String, Any>
                if (settingsData != null) {
                    for (setting in element.settings) {
                        val savedVal = settingsData[setting.name] ?: continue
                        try {
                            when (setting) {
                                is volthack.setting.Setting.Boolean -> setting.value = savedVal as Boolean
                                is volthack.setting.Setting.Float -> setting.value = (savedVal as Number).toFloat()
                                is volthack.setting.Setting.Int -> setting.value = (savedVal as Number).toInt()
                                is volthack.setting.Setting.Mode -> setting.value = savedVal as String
                                is volthack.setting.Setting.Color -> setting.value = (savedVal as Number).toInt()
                            }
                        } catch (e: Exception) {
                            // ignore loading error for single key mismatch
                        }
                    }
                }
                loaded++
            }
            VoltHack.LOGGER.info("       -> $loaded element positions and settings restored")
        } catch (e: Exception) {
            VoltHack.LOGGER.warn("Failed to load HUD config: ${e.message}")
        }
    }

    fun save() {
        try {
            val data = mutableMapOf<String, Map<String, Any>>()
            for (element in elements) {
                val settingsMap = mutableMapOf<String, Any>()
                for (setting in element.settings) {
                    val value = setting.value
                    if (value != null) {
                        settingsMap[setting.name] = value as Any
                    }
                }
                data[element.name] = mapOf(
                    "x" to element.x,
                    "y" to element.y,
                    "enabled" to element.enabled,
                    "settings" to settingsMap
                )
            }
            configFile.parent.toFile().mkdirs()
            configFile.toFile().writeText(gson.toJson(data))
        } catch (e: Exception) {
            VoltHack.LOGGER.warn("Failed to save HUD config: ${e.message}")
        }
    }
}
