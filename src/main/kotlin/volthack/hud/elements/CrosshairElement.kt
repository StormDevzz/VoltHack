package volthack.hud.elements

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import volthack.hud.HUDElement
import kotlin.math.cos
import kotlin.math.sin

class CrosshairElement : HUDElement("Crosshair") {
    private val type by mode("Type", listOf("Cross", "Dot", "Circle", "CircleDot"), "Cross")
    private val crossColor by color("Color", 0xFF00D4FF.toInt())
    private val size by float("Size", 5f, 2f, 20f, 0.5f)
    private val gap by float("Gap", 2f, 0f, 10f, 0.5f)
    private val thickness by float("Thickness", 1f, 0.5f, 5f, 0.5f)
    private val dynamic by boolean("Dynamic Inertia", true, "Sways crosshair when rotating camera")
    private val dynamicExpansion by boolean("Dynamic Expansion", true, "Expands crosshair lines when moving")

    private var lastYaw = 0f
    private var lastPitch = 0f
    private var shiftX = 0f
    private var shiftY = 0f

    init {
        x = 0
        y = 0
        cachedWidth = 20
        cachedHeight = 20
        enabled = false
    }

    override fun draw(ctx: GuiGraphics) {
        val mc = Minecraft.getInstance()
        val window = mc.window
        val player = mc.player

        val screenWidth = window.guiScaledWidth
        val screenHeight = window.guiScaledHeight
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f

        var currentGap = gap
        var currentCenterX = centerX
        var currentCenterY = centerY

        if (player != null) {
            // Camera Inertia (Sway)
            if (dynamic) {
                val yawDiff = player.yRot - lastYaw
                val pitchDiff = player.xRot - lastPitch
                
                val targetShiftX = -yawDiff * 1.5f
                val targetShiftY = pitchDiff * 1.5f
                
                shiftX += (targetShiftX - shiftX) * 0.2f
                shiftY += (targetShiftY - shiftY) * 0.2f
                
                val maxShift = 12f
                shiftX = shiftX.coerceIn(-maxShift, maxShift)
                shiftY = shiftY.coerceIn(-maxShift, maxShift)

                currentCenterX += shiftX
                currentCenterY += shiftY

                lastYaw = player.yRot
                lastPitch = player.xRot
            }

            // Expansion during movement
            if (dynamicExpansion) {
                val speed = player.deltaMovement.length()
                currentGap += (speed * 12f).toFloat()
            }
        }

        val cX = currentCenterX.toInt()
        val cY = currentCenterY.toInt()
        val s = size.toInt()
        val g = currentGap.toInt()
        val t = thickness.toInt().coerceAtLeast(1)

        when (type) {
            "Dot" -> {
                ctx.fill(cX - t, cY - t, cX + t, cY + t, crossColor)
            }
            "Cross" -> {
                ctx.fill(cX - t / 2, cY - g - s, cX + t / 2 + 1, cY - g, crossColor)
                ctx.fill(cX - t / 2, cY + g + 1, cX + t / 2 + 1, cY + g + s + 1, crossColor)
                ctx.fill(cX - g - s, cY - t / 2, cX - g, cY + t / 2 + 1, crossColor)
                ctx.fill(cX + g + 1, cY - t / 2, cX + g + s + 1, cY + t / 2 + 1, crossColor)
            }
            "Circle" -> {
                drawRing(ctx, cX, cY, g + s, t, crossColor)
            }
            "CircleDot" -> {
                ctx.fill(cX - t, cY - t, cX + t, cY + t, crossColor)
                drawRing(ctx, cX, cY, g + s, t, crossColor)
            }
        }
    }

    private fun drawRing(ctx: GuiGraphics, cx: Int, cy: Int, radius: Int, thickness: Int, color: Int) {
        val segments = 24
        for (i in 0 until segments) {
            val angle = i * (2 * Math.PI) / segments
            val rx = cx + radius * cos(angle)
            val ry = cy + radius * sin(angle)
            ctx.fill(
                rx.toInt() - thickness / 2,
                ry.toInt() - thickness / 2,
                rx.toInt() + thickness / 2 + 1,
                ry.toInt() + thickness / 2 + 1,
                color
            )
        }
    }
}
