package volthack.manager

import volthack.config.Config
import volthack.config.ConfigLoader
import volthack.config.ConfigSaver
import volthack.config.ConfigUtil
import java.io.File

object ConfigManager {
    fun createConfig(name: String) {
        val file = ConfigUtil.getConfigFile(name)
        ConfigSaver.save(file)
    }

    fun saveConfig(name: String) {
        val file = ConfigUtil.getConfigFile(name)
        ConfigSaver.save(file)
    }

    fun loadConfig(name: String): Boolean {
        val file = ConfigUtil.getConfigFile(name)
        if (!file.exists()) return false
        ConfigLoader.load(file)
        return true
    }

    fun deleteConfig(name: String): Boolean {
        val file = ConfigUtil.getConfigFile(name)
        if (file.exists()) {
            return file.delete()
        }
        return false
    }

    fun getConfigs(): List<Config> {
        return ConfigUtil.listConfigs()
    }
}
