package volthack.util.render

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Vector3d
import org.joml.Vector4f

object Render2DUtil {

    private val mc get() = Minecraft.getInstance()

    private val vec4 = Vector4f()
    private val vec3 = Vector3d()

    fun projectToScreen(entity: Entity, partialTicks: Float, modelView: Matrix4f, projection: Matrix4f): Vec3? {
        val x = entity.xo + (entity.x - entity.xo) * partialTicks
        val y = entity.yo + (entity.y - entity.yo) * partialTicks + entity.bbHeight * 0.5
        val z = entity.zo + (entity.z - entity.zo) * partialTicks
        return projectToScreen(x, y, z, modelView, projection)
    }

    fun projectToScreen(x: Double, y: Double, z: Double, modelView: Matrix4f, projection: Matrix4f): Vec3? {
        val camera = mc.gameRenderer.mainCamera
        val camPos = camera.position()

        vec4.set(
            (x - camPos.x).toFloat(),
            (y - camPos.y).toFloat(),
            (z - camPos.z).toFloat(),
            1.0f
        )

        vec4.mul(modelView)
        vec4.mul(projection)

        if (vec4.w <= 0.0f) return null

        val invW = 1.0f / vec4.w
        val screenX = (mc.window.guiScaledWidth / 2f) * (1.0f + vec4.x * invW)
        val screenY = (mc.window.guiScaledHeight / 2f) * (1.0f - vec4.y * invW)

        return Vec3(screenX.toDouble(), screenY.toDouble(), vec4.z.toDouble())
    }

    fun getEntityScreenBounds(
        entity: Entity,
        partialTicks: Float,
        modelView: Matrix4f,
        projection: Matrix4f
    ): AABB? {
        val bb = entity.boundingBox
        val x = entity.xo + (entity.x - entity.xo) * partialTicks
        val y = entity.yo + (entity.y - entity.yo) * partialTicks
        val z = entity.zo + (entity.z - entity.zo) * partialTicks

        val halfW = (bb.maxX - bb.minX) / 2.0
        val height = bb.maxY - bb.minY

        val corners = arrayOf(
            Vec3(x - halfW, y, z - halfW),
            Vec3(x + halfW, y, z - halfW),
            Vec3(x + halfW, y, z + halfW),
            Vec3(x - halfW, y, z + halfW),
            Vec3(x - halfW, y + height, z - halfW),
            Vec3(x + halfW, y + height, z - halfW),
            Vec3(x + halfW, y + height, z + halfW),
            Vec3(x - halfW, y + height, z + halfW)
        )

        var minX = Double.MAX_VALUE
        var minY = Double.MAX_VALUE
        var maxX = -Double.MAX_VALUE
        var maxY = -Double.MAX_VALUE

        for (corner in corners) {
            val projected = projectToScreen(corner.x, corner.y, corner.z, modelView, projection) ?: continue
            if (projected.x < minX) minX = projected.x
            if (projected.y < minY) minY = projected.y
            if (projected.x > maxX) maxX = projected.x
            if (projected.y > maxY) maxY = projected.y
        }

        if (minX == Double.MAX_VALUE) return null

        return AABB(minX, minY, 0.0, maxX, maxY, 0.0)
    }

    fun drawBox(ctx: GuiGraphics, x: Double, y: Double, w: Double, h: Double, color: Int) {
        ctx.fill(x.toInt(), y.toInt(), (x + w).toInt(), (y + h).toInt(), color)
    }

    fun drawOutline(ctx: GuiGraphics, x: Double, y: Double, w: Double, h: Double, color: Int, thickness: Int = 1) {
        val ix = x.toInt(); val iy = y.toInt(); val iw = w.toInt(); val ih = h.toInt()
        ctx.fill(ix, iy, ix + iw, iy + thickness, color)
        ctx.fill(ix, iy + ih - thickness, ix + iw, iy + ih, color)
        ctx.fill(ix, iy + thickness, ix + thickness, iy + ih - thickness, color)
        ctx.fill(ix + iw - thickness, iy + thickness, ix + iw, iy + ih - thickness, color)
    }

    fun drawEntity2D(
        ctx: GuiGraphics,
        entity: Entity,
        partialTicks: Float,
        modelView: Matrix4f,
        projection: Matrix4f,
        boxColor: Int,
        outlineColor: Int
    ) {
        val bounds = getEntityScreenBounds(entity, partialTicks, modelView, projection) ?: return
        val x = bounds.minX; val y = bounds.minY; val w = bounds.maxX - bounds.minX; val h = bounds.maxY - bounds.minY
        drawBox(ctx, x, y, w, h, boxColor)
        drawOutline(ctx, x, y, w, h, outlineColor, 1)
    }

    fun drawHealthBar(ctx: GuiGraphics, entity: net.minecraft.world.entity.LivingEntity, x: Int, y: Int, w: Int, h: Int) {
        val hp = entity.health
        val maxHp = entity.maxHealth.coerceAtLeast(1f)
        val pct = (hp / maxHp).coerceIn(0f, 1f)

        ctx.fill(x, y, x + w, y + h, 0x90000000.toInt())
        val barColor = when {
            pct > 0.6f -> 0xFF2ED573.toInt()
            pct > 0.3f -> 0xFFFFA502.toInt()
            else -> 0xFFFF4757.toInt()
        }
        ctx.fill(x, y, x + (w * pct).toInt(), y + h, barColor)
    }

    fun drawText(
        ctx: GuiGraphics,
        text: String,
        x: Double,
        y: Double,
        color: Int,
        centerX: Boolean = false,
        centerY: Boolean = false
    ) {
        val font = mc.font
        val tw = font.width(text)
        val tx = if (centerX) (x - tw / 2.0).toInt() else x.toInt()
        val ty = if (centerY) (y - font.lineHeight / 2.0).toInt() else y.toInt()
        ctx.drawString(font, text, tx, ty, color, true)
    }

    fun isOnScreen(pos: Vec3): Boolean {
        return pos.x in 0.0..mc.window.guiScaledWidth.toDouble() &&
               pos.y in 0.0..mc.window.guiScaledHeight.toDouble()
    }
}
