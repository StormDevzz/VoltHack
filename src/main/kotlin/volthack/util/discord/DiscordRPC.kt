package volthack.util.discord

import com.google.gson.JsonObject
import com.jagrosh.discordipc.IPCClient
import com.jagrosh.discordipc.IPCListener
import com.jagrosh.discordipc.entities.Packet
import com.jagrosh.discordipc.entities.ActivityType
import com.jagrosh.discordipc.entities.RichPresence
import com.jagrosh.discordipc.entities.User
import volthack.VoltHack
import volthack.util.discord.internal.runtime.ClientHandle

object DiscordRPC {
    private var client: IPCClient? = null
    private var connected = false
    private var startTime = 0L

    val isConnected: Boolean get() = connected

    fun start() {
        if (connected) return
        val id = ClientHandle.clientId
        VoltHack.LOGGER.info("Discord RPC connecting (id: ${id.toString().take(4)}...)")
        client = IPCClient(id)
        client?.setListener(object : IPCListener {
            override fun onReady(c: IPCClient?) {
                connected = true
                startTime = System.currentTimeMillis()
                VoltHack.LOGGER.info("Discord RPC connected successfully")
                update("VoltHack v${VoltHack.version}", "Hacking the mainframe")
            }

            override fun onPacketSent(c: IPCClient?, p: Packet?) {}
            override fun onPacketReceived(c: IPCClient?, p: Packet?) {}
            override fun onActivityJoin(c: IPCClient?, s: String?) {}
            override fun onActivitySpectate(c: IPCClient?, s: String?) {}
            override fun onActivityJoinRequest(c: IPCClient?, s: String?, u: User?) {}

            override fun onClose(c: IPCClient?, json: JsonObject?) {
                connected = false
                startTime = 0L
                VoltHack.LOGGER.info("Discord RPC closed")
            }

            override fun onDisconnect(c: IPCClient?, t: Throwable?) {
                connected = false
                startTime = 0L
                VoltHack.LOGGER.warn("Discord RPC disconnected: ${t?.message}")
            }
        })

        try {
            client?.connect()
            VoltHack.LOGGER.info("Discord RPC connection established")
        } catch (e: Exception) {
            VoltHack.LOGGER.warn("Discord RPC unavailable: ${e.message}")
        }
    }

    fun update(details: String, state: String, largeImage: String? = "volt") {
        if (!connected) return
        val builder = RichPresence.Builder()
            .setActivityType(ActivityType.Playing)
            .setDetails(details)
            .setState(state)

        if (startTime != 0L) {
            builder.setStartTimestamp(startTime)
        }

        if (largeImage != null) {
            builder.setLargeImage(largeImage, "VoltHack")
        }

        try {
            client?.sendRichPresence(builder.build())
        } catch (e: Exception) {
            VoltHack.LOGGER.warn("RPC send: ${e.message}")
        }
    }

    val discordUsername: String?
        get() = try { client?.currentUser?.name } catch (e: Exception) { null }

    val discordAvatarUrl: String?
        get() = try { client?.currentUser?.avatarUrl } catch (e: Exception) { null }

    fun stop() {
        try {
            client?.close()
        } catch (_: Exception) {}
        client = null
        connected = false
        startTime = 0L
        VoltHack.LOGGER.info("Discord RPC stopped")
    }
}
