package volthack.command

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ServerboundChatPacket
import volthack.event.PacketSendEvent
import volthack.manager.PacketManager
import volthack.manager.FriendManager
import volthack.setting.ModuleManager
import java.util.Locale

object CommandManager {
    private val commands = mutableMapOf<String, Command>()

    fun register(command: Command) {
        commands[command.name.lowercase()] = command
        command.aliases.forEach { commands[it.lowercase()] = command }
    }

    fun init() {
        register(HelpCommand)
        register(EnableCommand)
        register(DisableCommand)
        register(ToggleCommand)
        register(BindCommand)
        register(FriendCommand)

        PacketManager.registerSendListener { onPacketSend(it) }
    }

    private fun onPacketSend(event: PacketSendEvent) {
        if (event.packet !is ServerboundChatPacket) return
        val msg = (event.packet as ServerboundChatPacket).message
        if (!msg.startsWith(".")) return
        event.cancelled = true

        val parts = msg.substring(1).split(" ")
        val name = parts[0].lowercase()
        val args = parts.drop(1).toTypedArray()

        val command = commands[name]
        if (command != null) {
            command.execute(args)
        } else {
            msg("§7Unknown command. Use §b.help §7for a list of commands.")
        }
    }

    fun msg(text: String) {
        Minecraft.getInstance().player?.displayClientMessage(Component.literal("§8[§bVoltHack§8] §7$text"), false)
    }

    fun help(): String {
        return commands.values.distinct().joinToString("\n") { "§b.${it.name} §8- §7${it.description}" }
    }
}

interface Command {
    val name: String
    val description: String
    val aliases: List<String>
    fun execute(args: Array<out String>)
}

object HelpCommand : Command {
    override val name = "help"
    override val description = "Shows this help message"
    override val aliases = listOf("h", "commands", "?")

    override fun execute(args: Array<out String>) {
        CommandManager.msg(CommandManager.help())
    }
}

object EnableCommand : Command {
    override val name = "enable"
    override val description = "Enables a module"
    override val aliases = listOf("on")

    override fun execute(args: Array<out String>) {
        if (args.isEmpty()) { CommandManager.msg("§cUsage: .enable <module>"); return }
        val mod = ModuleManager.get(args.joinToString(" "))
        if (mod == null) { CommandManager.msg("§cModule '${args.joinToString(" ")}' not found."); return }
        mod.enable()
        CommandManager.msg("§a${mod.name} §7enabled.")
    }
}

object DisableCommand : Command {
    override val name = "disable"
    override val description = "Disables a module"
    override val aliases = listOf("off")

    override fun execute(args: Array<out String>) {
        if (args.isEmpty()) { CommandManager.msg("§cUsage: .disable <module>"); return }
        val mod = ModuleManager.get(args.joinToString(" "))
        if (mod == null) { CommandManager.msg("§cModule '${args.joinToString(" ")}' not found."); return }
        mod.disable()
        CommandManager.msg("§c${mod.name} §7disabled.")
    }
}

object ToggleCommand : Command {
    override val name = "toggle"
    override val description = "Toggles a module on/off"
    override val aliases = listOf("t")

    override fun execute(args: Array<out String>) {
        if (args.isEmpty()) { CommandManager.msg("§cUsage: .toggle <module>"); return }
        val mod = ModuleManager.get(args.joinToString(" "))
        if (mod == null) { CommandManager.msg("§cModule '${args.joinToString(" ")}' not found."); return }
        mod.toggle()
        CommandManager.msg("§7${mod.name} §8→ §${if (mod.enabled) "aenabled" else "cdisabled"}§7.")
    }
}

object BindCommand : Command {
    override val name = "bind"
    override val description = "Bind a key to a module"
    override val aliases = listOf("b")

    override fun execute(args: Array<out String>) {
        if (args.size < 2) { CommandManager.msg("§cUsage: .bind <module> <key>"); return }
        val modName = args.dropLast(1).joinToString(" ")
        val keyName = args.last().uppercase(Locale.ROOT)
        val mod = ModuleManager.get(modName)
        if (mod == null) { CommandManager.msg("§cModule '$modName' not found."); return }

        val keyMap = mapOf(
            "NONE" to 0, "RSHIFT" to 340, "LSHIFT" to 344, "LCONTROL" to 341,
            "RCONTROL" to 345, "LALT" to 342, "RALT" to 346, "TAB" to 258,
            "SPACE" to 32, "ESCAPE" to 256, "ENTER" to 257, "BACKSPACE" to 259,
            "UP" to 265, "DOWN" to 264, "LEFT" to 263, "RIGHT" to 262,
            "LCLICK" to -1, "RCLICK" to -2, "MCLICK" to -3
        )

        val keyCode = if (keyName.length == 1) {
            keyName[0].uppercaseChar().code
        } else keyMap[keyName] ?: run {
            CommandManager.msg("§cUnknown key: $keyName"); return
        }

        mod.bindKey = keyCode
        volthack.setting.ModuleConfig.save(mod.name, volthack.setting.ModuleConfig.ModuleData(enabled = mod.enabled, bindKey = mod.bindKey))
        CommandManager.msg("§7Bound §b${mod.name} §7to §a$keyName§7.")
    }
}

object FriendCommand : Command {
    override val name = "friend"
    override val description = "Manage friends (add/remove/list)"
    override val aliases = listOf("f")

    override fun execute(args: Array<out String>) {
        if (args.isEmpty()) { CommandManager.msg("§cUsage: .friend <add|remove|list> [name]"); return }
        when (args[0].lowercase()) {
            "add" -> {
                if (args.size < 2) { CommandManager.msg("§cUsage: .friend add <name>"); return }
                val name = args.drop(1).joinToString(" ")
                if (FriendManager.add(name)) CommandManager.msg("§a$name §7added to friends.")
                else CommandManager.msg("§e$name §7is already a friend.")
            }
            "remove", "rm" -> {
                if (args.size < 2) { CommandManager.msg("§cUsage: .friend remove <name>"); return }
                val name = args.drop(1).joinToString(" ")
                if (FriendManager.remove(name)) CommandManager.msg("§c$name §7removed from friends.")
                else CommandManager.msg("§e$name §7is not a friend.")
            }
            "list", "ls" -> {
                val list = FriendManager.list()
                if (list.isEmpty()) CommandManager.msg("§7No friends added.")
                else CommandManager.msg("§7Friends: §b${list.joinToString("§7, §b")}")
            }
            else -> CommandManager.msg("§cUsage: .friend <add|remove|list> [name]")
        }
    }
}
