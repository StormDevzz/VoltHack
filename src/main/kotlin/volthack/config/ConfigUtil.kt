package volthack.config

import net.fabricmc.loader.api.FabricLoader
import java.io.File

object ConfigUtil {
    val configFolder: File by lazy {
        val folder = FabricLoader.getInstance().gameDir.resolve("VoltHack/configs").toFile()
        folder.mkdirs()
        folder
    }

    fun getConfigFile(name: String): File {
        val cleanName = if (name.endsWith(".vlth")) name else "$name.vlth"
        return File(configFolder, cleanName)
    }

    fun listConfigs(): List<Config> {
        val files = configFolder.listFiles() ?: return emptyList()
        return files.filter { it.isFile && it.name.endsWith(".vlth") }
            .map { Config(it.nameWithoutExtension, it) }
            .sortedBy { it.name.lowercase() }
    }
}
