package volthack.config

import com.google.gson.JsonParser
import volthack.setting.ModuleManager
import volthack.setting.Setting
import java.io.File

object ConfigLoader {
    fun load(file: File) {
        if (!file.exists()) return
        try {
            val root = JsonParser.parseString(file.readText()).asJsonObject
            for (module in ModuleManager.getAll()) {
                if (root.has(module.name)) {
                    val moduleObj = root.getAsJsonObject(module.name)
                    
                    // First restore settings so that enabling calls onEnable with correct parameters
                    if (moduleObj.has("settings")) {
                        val settingsObj = moduleObj.getAsJsonObject("settings")
                        for (setting in module.settings) {
                            if (settingsObj.has(setting.name)) {
                                val element = settingsObj.get(setting.name)
                                when (setting) {
                                    is Setting.Boolean -> setting.value = element.asBoolean
                                    is Setting.Float -> setting.value = element.asFloat
                                    is Setting.Int -> setting.value = element.asInt
                                    is Setting.Mode -> setting.value = element.asString
                                    is Setting.Color -> setting.value = element.asInt
                                    is Setting.StringSetting -> setting.value = element.asString
                                }
                            }
                        }
                    }

                    if (moduleObj.has("bindKey")) {
                        module.bindKey = moduleObj.get("bindKey").asInt
                    }

                    if (moduleObj.has("enabled")) {
                        val enabled = moduleObj.get("enabled").asBoolean
                        if (enabled) module.enable() else module.disable()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
