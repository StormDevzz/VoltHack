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
            if (!element.enabled) continue
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
                loaded++
            }
            VoltHack.LOGGER.info("       -> $loaded element positions restored")
        } catch (e: Exception) {
            VoltHack.LOGGER.warn("Failed to load HUD config: ${e.message}")
        }
    }

    fun save() {
        try {
            val data = mutableMapOf<String, Map<String, Any>>()
            for (element in elements) {
                data[element.name] = mapOf(
                    "x" to element.x,
                    "y" to element.y,
                    "enabled" to element.enabled
                )
            }
            configFile.parent.toFile().mkdirs()
            configFile.toFile().writeText(gson.toJson(data))
        } catch (e: Exception) {
            VoltHack.LOGGER.warn("Failed to save HUD config: ${e.message}")
        }
    }
}
