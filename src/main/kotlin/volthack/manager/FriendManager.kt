package volthack.manager

import java.io.File
import java.nio.file.Files

object FriendManager {
    private val friends = mutableSetOf<String>()
    private val file: File by lazy {
        val dir = File(net.minecraft.client.Minecraft.getInstance().gameDirectory, "VoltHack")
        dir.mkdirs()
        File(dir, "friends.txt")
    }

    fun load() {
        if (!file.exists()) return
        friends.clear()
        try {
            Files.readAllLines(file.toPath()).forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty()) friends.add(trimmed.lowercase())
            }
        } catch (_: Exception) {}
    }

    private fun save() {
        try {
            file.writeText(friends.joinToString("\n"))
        } catch (_: Exception) {}
    }

    fun add(name: String): Boolean {
        val key = name.lowercase()
        if (friends.add(key)) { save(); return true }
        return false
    }

    fun remove(name: String): Boolean {
        val key = name.lowercase()
        if (friends.remove(key)) { save(); return true }
        return false
    }

    fun isFriend(name: String): Boolean = friends.contains(name.lowercase())
    fun list(): List<String> = friends.toList().sorted()
    fun clear() { friends.clear(); save() }
}
