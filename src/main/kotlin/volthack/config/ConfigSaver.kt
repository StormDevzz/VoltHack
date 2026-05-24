package volthack.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import volthack.setting.ModuleManager
import volthack.setting.Setting
import java.io.File

object ConfigSaver {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun save(file: File) {
        val root = JsonObject()
        for (module in ModuleManager.getAll()) {
            val moduleObj = JsonObject()
            moduleObj.addProperty("enabled", module.enabled)
            moduleObj.addProperty("bindKey", module.bindKey)
            
            val settingsObj = JsonObject()
            for (setting in module.settings) {
                when (setting) {
                    is Setting.Boolean -> settingsObj.addProperty(setting.name, setting.value)
                    is Setting.Float -> settingsObj.addProperty(setting.name, setting.value)
                    is Setting.Int -> settingsObj.addProperty(setting.name, setting.value)
                    is Setting.Mode -> settingsObj.addProperty(setting.name, setting.value)
                    is Setting.Color -> settingsObj.addProperty(setting.name, setting.value)
                    is Setting.StringSetting -> settingsObj.addProperty(setting.name, setting.value)
                }
            }
            moduleObj.add("settings", settingsObj)
            root.add(module.name, moduleObj)
        }

        try {
            file.parentFile.mkdirs()
            file.writeText(gson.toJson(root))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
