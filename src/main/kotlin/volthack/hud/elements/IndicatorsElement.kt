package volthack.hud.elements

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme
import volthack.hud.HUDElement
import kotlin.math.sqrt

class IndicatorsElement : HUDElement("Indicators") {
    private val showFPS by boolean("Show FPS", true)
    private val showPing by boolean("Show Ping", true)
    private val showBPS by boolean("Show BPS", true)
    private val customColor by color("Color", 0xFF00D4FF.toInt())

    init {
        x = 4
        y = 250
        cachedWidth = 100
        cachedHeight = 40
        enabled = false
    }

    override fun render(ctx: GuiGraphics) {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        val lines = mutableListOf<String>()

        if (showFPS) {
            val fps = mc.fps
            lines.add("FPS: $fps")
        }
        if (showPing) {
            val ping = mc.connection?.getPlayerInfo(player.uuid)?.latency ?: 0
            lines.add("Ping: ${ping}ms")
        }
        if (showBPS) {
            val deltaX = player.x - player.xo
            val deltaZ = player.z - player.zo
            val bps = "%.2f".format(sqrt(deltaX * deltaX + deltaZ * deltaZ) * 20)
            lines.add("Speed: ${bps} BPS")
        }

        if (lines.isEmpty()) {
            cachedWidth = 0
            cachedHeight = 0
            return
        }

        var cy = y
        var maxW = 0
        for (line in lines) {
            GUIFontRenderer.draw(ctx, line, x.toFloat(), cy.toFloat(), customColor)
            maxW = maxOf(maxW, GUIFontRenderer.width(line))
            cy += GUIFontRenderer.height + 2
        }

        cachedWidth = maxW.coerceAtLeast(60)
        cachedHeight = cy - y
    }
}
