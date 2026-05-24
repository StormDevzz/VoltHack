package volthack.hud.elements

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme
import volthack.hud.HUDElement
import kotlin.math.sqrt

class IndicatorsElement : HUDElement("Indicators") {
    private val showHealth by boolean("Health", true)
    private val showArmor by boolean("Armor", true)
    private val showHunger by boolean("Hunger", true)
    private val showSpeed by boolean("Speed", true)
    private val showTPS by boolean("TPS", true)
    private val showPing by boolean("Ping", true)
    private val customColor by color("Color", 0xFF00D4FF.toInt())

    // Local tick-based TPS tracking
    private var lastTickTime = 0L
    private val tpsHistory = DoubleArray(40) { 20.0 }
    private var tpsIndex = 0

    init {
        x = 4
        y = 250
        cachedWidth = 200
        cachedHeight = 60
        enabled = false
    }

    private fun updateTPS() {
        val now = System.currentTimeMillis()
        if (lastTickTime != 0L) {
            val diff = now - lastTickTime
            val tps = if (diff > 0) 1000.0 / diff else 20.0
            val clamped = tps.coerceIn(0.1, 20.0)
            tpsHistory[tpsIndex] = clamped
            tpsIndex = (tpsIndex + 1) % tpsHistory.size
        }
        lastTickTime = now
    }

    private fun getTPS(): Double {
        return tpsHistory.average()
    }

    override fun draw(ctx: GuiGraphics) {
        updateTPS()
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        // Collect all active indicators to draw
        data class IndicatorData(val label: String, val valueText: String, val pct: Float, val color: Int)
        val list = mutableListOf<IndicatorData>()

        // 1. Health
        if (showHealth) {
            val hp = player.health
            val maxHp = player.maxHealth.coerceAtLeast(1f)
            val pct = (hp / maxHp).coerceIn(0f, 1f)
            list.add(IndicatorData("HP", "${hp.toInt()}", pct, 0xFFFF4A4A.toInt()))
        }

        // 2. Armor
        if (showArmor) {
            val armor = player.armorValue
            val pct = (armor / 20f).coerceIn(0f, 1f)
            list.add(IndicatorData("ARM", "$armor", pct, 0xFF4A90E2.toInt()))
        }

        // 3. Hunger
        if (showHunger) {
            val hunger = player.foodData.foodLevel
            val pct = (hunger / 20f).coerceIn(0f, 1f)
            list.add(IndicatorData("HUN", "$hunger", pct, 0xFFFF9F0A.toInt()))
        }

        // 4. Speed
        if (showSpeed) {
            val deltaX = player.x - player.xo
            val deltaZ = player.z - player.zo
            val bps = sqrt(deltaX * deltaX + deltaZ * deltaZ) * 20
            val pct = (bps / 30.0).coerceIn(0.0, 1.0).toFloat()
            val speedPct = (pct * 100).toInt()
            list.add(IndicatorData("SPD", "$speedPct%", pct, 0xFF30D158.toInt()))
        }

        // 5. TPS
        if (showTPS) {
            val tps = getTPS()
            val pct = (tps / 20.0).coerceIn(0.0, 1.0).toFloat()
            list.add(IndicatorData("TPS", "%.1f".format(tps), pct, 0xFFBF5AF2.toInt()))
        }

        // 6. Ping
        if (showPing) {
            val ping = mc.connection?.getPlayerInfo(player.uuid)?.latency ?: 0
            val pct = (1f - ping / 300f).coerceIn(0f, 1f)
            list.add(IndicatorData("PNG", "$ping", pct, 0xFFFFD60A.toInt()))
        }

        if (list.isEmpty()) {
            cachedWidth = 0
            cachedHeight = 0
            return
        }

        val circleRadius = 15
        val circleDiameter = circleRadius * 2
        val spacing = 12
        val itemWidth = circleDiameter + 10
        val itemHeight = circleDiameter + 24

        cachedWidth = list.size * itemWidth + (list.size - 1) * spacing + 16
        cachedHeight = itemHeight + 12

        // Draw card background
        ctx.fill(x, y, x + cachedWidth, y + cachedHeight, VoltHackTheme.surface)
        ctx.fill(x, y, x + cachedWidth, y + 1, customColor)

        var cx = x + 8 + itemWidth / 2
        val cy = y + 8 + circleRadius

        for (ind in list) {
            // Draw dark backdrop disk for circle
            drawBackdropCircle(ctx, cx, cy, circleRadius)

            // Draw circular progress ring
            drawProgressRing(ctx, cx, cy, circleRadius, ind.pct, ind.color)

            // Draw centered value inside the circle
            val tw = GUIFontRenderer.width(ind.valueText)
            val tx = cx - tw / 2f
            val ty = cy - GUIFontRenderer.height / 2f
            GUIFontRenderer.draw(ctx, ind.valueText, tx, ty, VoltHackTheme.textPrimary)

            // Draw label below the circle
            val lw = GUIFontRenderer.width(ind.label)
            val lx = cx - lw / 2f
            val ly = cy + circleRadius + 4f
            GUIFontRenderer.draw(ctx, ind.label, lx, ly, VoltHackTheme.textSecondary)

            cx += itemWidth + spacing
        }
    }

    private fun drawBackdropCircle(ctx: GuiGraphics, cx: Int, cy: Int, radius: Int) {
        val segments = 16
        for (i in 0 until segments) {
            val angle = i * (2 * Math.PI) / segments
            val rx = cx + radius * Math.cos(angle)
            val ry = cy + radius * Math.sin(angle)
            ctx.fill(rx.toInt(), ry.toInt(), rx.toInt() + 1, ry.toInt() + 1, 0x1AFFFFFF.toInt())
        }
    }

    private fun drawProgressRing(ctx: GuiGraphics, cx: Int, cy: Int, radius: Int, pct: Float, color: Int) {
        val segments = 24
        val activeSegments = (pct * segments).toInt().coerceIn(0, segments)
        if (activeSegments == 0) return
        for (i in 0 until activeSegments) {
            val angle = -Math.PI / 2 + i * (2 * Math.PI) / segments
            val rx = cx + radius * Math.cos(angle)
            val ry = cy + radius * Math.sin(angle)
            ctx.fill(rx.toInt(), ry.toInt(), rx.toInt() + 1, ry.toInt() + 1, color)
        }
    }
}
