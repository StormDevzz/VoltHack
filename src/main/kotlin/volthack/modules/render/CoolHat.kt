package volthack.modules.render

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import volthack.event.EventBus
import volthack.event.Render3DEvent
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.render.Render3DUtil

object CoolHat : Module("CoolHat", "Draws a cool hat above entities", Category.RENDER) {
    private val mode by mode("Hat Mode", listOf("Ring", "Crown", "TopHat"), "Ring")
    private val colorMode by mode("Color Mode", listOf("Static", "Rainbow"), "Static")
    private val customColor by color("Color", 0xFF6C63FF.toInt())
    private val radius by float("Radius", 0.5f, 0.2f, 1.5f, 0.1f)
    private val height by float("Height", 0.3f, 0.1f, 1.0f, 0.1f)
    private val onlyPlayers by boolean("Only Players", true)
    private val includeSelf by boolean("Include Self", true, "Draw hat on yourself too")

    init {
        EventBus.listen<Render3DEvent> { onRender3D(it) }
    }

    private fun onRender3D(event: Render3DEvent) {
        if (!enabled) return

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val world = mc.level ?: return
        val camPos = mc.gameRenderer.mainCamera.position()

        val entities = world.entitiesForRendering()
            .filterIsInstance<LivingEntity>()
            .filter { it.isAlive }
            .filter { includeSelf || it != player }
            .filter { !onlyPlayers || it is Player }
            .filter { camPos.distanceToSqr(it.position()) < 4096.0 }

        val time = System.currentTimeMillis() / 1000.0
        val spinAngle = if (mode == "Ring") time * 2.0 else 0.0
        val color = getColor(time)

        val r = (color shr 16 and 255)
        val g = (color shr 8 and 255)
        val b = (color and 255)
        val a = 200

        for (entity in entities) {
            val ex = entity.xo + (entity.x - entity.xo) * event.partialTicks
            val ey = entity.yo + (entity.y - entity.yo) * event.partialTicks + entity.bbHeight + height.toDouble()
            val ez = entity.zo + (entity.z - entity.zo) * event.partialTicks

            val rx = ex - camPos.x
            val ry = ey - camPos.y
            val rz = ez - camPos.z

            when (mode) {
                "Ring" -> {
                    Render3DUtil.drawRing(rx, ry, rz, radius, r, g, b, a, spinAngle, true)
                }
                "Crown" -> {
                    Render3DUtil.drawRing(rx, ry, rz, radius * 0.7f, r, g, b, a, spinAngle, true)
                    Render3DUtil.drawRing(rx, ry + 0.15, rz, radius * 0.5f, r, g, b, a, -spinAngle, true)
                }
                "TopHat" -> {
                    Render3DUtil.drawRing(rx, ry, rz, radius, r, g, b, a, 0.0, true)
                    Render3DUtil.drawRing(rx, ry + 0.3, rz, radius * 0.6f, r, g, b, a, 0.0, true)
                    Render3DUtil.drawBlockOutline(rx, ry, rz, radius * 2f, 0.3f, r, g, b, a, true, 2f)
                }
            }
        }
    }

    private fun getColor(time: Double): Int {
        return if (colorMode == "Rainbow") {
            val hue = (time * 0.5) % 1.0
            java.awt.Color.HSBtoRGB(hue.toFloat(), 0.8f, 1.0f)
        } else {
            customColor
        }
    }
}
