package volthack.util.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.rendertype.RenderTypes

object Render3DUtil {

    fun drawBlockOutline(
        x: Double, y: Double, z: Double,
        width: Float, height: Float,
        r: Int, g: Int, b: Int, a: Int,
        throughWalls: Boolean,
        lineWidth: Float = 2.0f
    ) {
        val mc = Minecraft.getInstance()
        val bufferSource = mc.renderBuffers().bufferSource()
        val renderType = if (throughWalls) RenderTypes.LINES_TRANSLUCENT else RenderTypes.LINES_TRANSLUCENT
        val consumer = bufferSource.getBuffer(renderType)
        val pose = PoseStack().last()

        val hw = width / 2.0
        val color = argb(r, g, b, a)
        val nx = 0.0f
        val ny = 1.0f
        val nz = 0.0f

        val corners = arrayOf(
            doubleArrayOf(x - hw, y, z - hw), doubleArrayOf(x + hw, y, z - hw),
            doubleArrayOf(x + hw, y, z + hw), doubleArrayOf(x - hw, y, z + hw),
            doubleArrayOf(x - hw, y + height, z - hw), doubleArrayOf(x + hw, y + height, z - hw),
            doubleArrayOf(x + hw, y + height, z + hw), doubleArrayOf(x - hw, y + height, z + hw)
        )

        val edges = arrayOf(
            intArrayOf(0, 1), intArrayOf(1, 2), intArrayOf(2, 3), intArrayOf(3, 0),
            intArrayOf(4, 5), intArrayOf(5, 6), intArrayOf(6, 7), intArrayOf(7, 4),
            intArrayOf(0, 4), intArrayOf(1, 5), intArrayOf(2, 6), intArrayOf(3, 7)
        )

        for (edge in edges) {
            val p1 = corners[edge[0]]
            val p2 = corners[edge[1]]
            val ex = (p2[0] - p1[0]).toFloat()
            val ey = (p2[1] - p1[1]).toFloat()
            val ez = (p2[2] - p1[2]).toFloat()
            val len = Math.sqrt((ex * ex + ey * ey + ez * ez).toDouble()).toFloat()
            if (len > 0.001f) {
                consumer.addVertex(pose, p1[0].toFloat(), p1[1].toFloat(), p1[2].toFloat())
                    .setColor(color)
                    .setNormal(pose, ex / len, ey / len, ez / len)
                    .setLineWidth(lineWidth)
                consumer.addVertex(pose, p2[0].toFloat(), p2[1].toFloat(), p2[2].toFloat())
                    .setColor(color)
                    .setNormal(pose, ex / len, ey / len, ez / len)
                    .setLineWidth(lineWidth)
            }
        }

        bufferSource.endBatch(renderType)
    }

    fun drawRing(
        x: Double, y: Double, z: Double,
        radius: Float,
        r: Int, g: Int, b: Int, a: Int,
        spinAngle: Double,
        throughWalls: Boolean
    ) {
        val mc = Minecraft.getInstance()
        val bufferSource = mc.renderBuffers().bufferSource()
        val consumer = bufferSource.getBuffer(RenderTypes.lines())
        val pose = PoseStack().last()

        val color = argb(r, g, b, a)
        val nx = 0.0f
        val ny = 1.0f
        val nz = 0.0f
        val segments = 32

        for (i in 0..segments) {
            val angle = (i * 2 * Math.PI / segments) + spinAngle
            val dx = Math.cos(angle) * radius
            val dz = Math.sin(angle) * radius
            consumer.addVertex(pose, (x + dx).toFloat(), y.toFloat(), (z + dz).toFloat())
                .setColor(color)
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(2.0f)
        }

        bufferSource.endBatch(RenderTypes.lines())
    }

    fun drawBand(
        x: Double, y: Double, z: Double,
        radius: Float, height: Float,
        r: Int, g: Int, b: Int, a: Int,
        spinAngle: Double,
        throughWalls: Boolean
    ) {
        val mc = Minecraft.getInstance()
        val bufferSource = mc.renderBuffers().bufferSource()
        val consumer = bufferSource.getBuffer(RenderTypes.LINES_TRANSLUCENT)
        val pose = PoseStack().last()

        val color = argb(r, g, b, a)
        val segments = 32

        for (i in 0 until segments) {
            val angle1 = (i * 2 * Math.PI / segments) + spinAngle
            val angle2 = ((i + 1) * 2 * Math.PI / segments) + spinAngle

            val dx1 = Math.cos(angle1) * radius
            val dz1 = Math.sin(angle1) * radius
            val dx2 = Math.cos(angle2) * radius
            val dz2 = Math.sin(angle2) * radius

            val ex = ((x + dx2) - (x + dx1)).toFloat()
            val ez = ((z + dz2) - (z + dz1)).toFloat()
            val len = Math.sqrt((ex * ex + ez * ez).toDouble()).toFloat()
            val nLen = if (len > 0.001f) len else 1.0f

            consumer.addVertex(pose, (x + dx1).toFloat(), y.toFloat(), (z + dz1).toFloat())
                .setColor(color)
                .setNormal(pose, ex / nLen, 0.0f, ez / nLen)
                .setLineWidth(1.0f)
            consumer.addVertex(pose, (x + dx1).toFloat(), (y + height).toFloat(), (z + dz1).toFloat())
                .setColor(color)
                .setNormal(pose, ex / nLen, 0.0f, ez / nLen)
                .setLineWidth(1.0f)
            consumer.addVertex(pose, (x + dx2).toFloat(), (y + height).toFloat(), (z + dz2).toFloat())
                .setColor(color)
                .setNormal(pose, ex / nLen, 0.0f, ez / nLen)
                .setLineWidth(1.0f)
            consumer.addVertex(pose, (x + dx2).toFloat(), y.toFloat(), (z + dz2).toFloat())
                .setColor(color)
                .setNormal(pose, ex / nLen, 0.0f, ez / nLen)
                .setLineWidth(1.0f)
        }

        bufferSource.endBatch(RenderTypes.LINES_TRANSLUCENT)
    }

    fun drawGlowBand(
        x: Double, y: Double, z: Double,
        radius: Float, height: Float,
        r: Int, g: Int, b: Int, a: Int,
        spinAngle: Double,
        throughWalls: Boolean
    ) {
        val mc = Minecraft.getInstance()
        val bufferSource = mc.renderBuffers().bufferSource()
        val consumer = bufferSource.getBuffer(RenderTypes.LINES_TRANSLUCENT)
        val pose = PoseStack().last()

        val segments = 32

        for (i in 0 until segments) {
            val angle1 = (i * 2 * Math.PI / segments) + spinAngle
            val angle2 = ((i + 1) * 2 * Math.PI / segments) + spinAngle

            val dx1 = Math.cos(angle1) * radius
            val dz1 = Math.sin(angle1) * radius
            val dx2 = Math.cos(angle2) * radius
            val dz2 = Math.sin(angle2) * radius

            val ex = ((x + dx2) - (x + dx1)).toFloat()
            val ez = ((z + dz2) - (z + dz1)).toFloat()
            val len = Math.sqrt((ex * ex + ez * ez).toDouble()).toFloat()
            val nLen = if (len > 0.001f) len else 1.0f

            consumer.addVertex(pose, (x + dx1).toFloat(), y.toFloat(), (z + dz1).toFloat())
                .setColor(argb(r, g, b, (a * 0.6f).toInt()))
                .setNormal(pose, ex / nLen, 0.0f, ez / nLen)
                .setLineWidth(1.0f)
            consumer.addVertex(pose, (x + dx1).toFloat(), (y + height).toFloat(), (z + dz1).toFloat())
                .setColor(argb(r, g, b, 0))
                .setNormal(pose, ex / nLen, 0.0f, ez / nLen)
                .setLineWidth(1.0f)
            consumer.addVertex(pose, (x + dx2).toFloat(), (y + height).toFloat(), (z + dz2).toFloat())
                .setColor(argb(r, g, b, 0))
                .setNormal(pose, ex / nLen, 0.0f, ez / nLen)
                .setLineWidth(1.0f)
            consumer.addVertex(pose, (x + dx2).toFloat(), y.toFloat(), (z + dz2).toFloat())
                .setColor(argb(r, g, b, (a * 0.6f).toInt()))
                .setNormal(pose, ex / nLen, 0.0f, ez / nLen)
                .setLineWidth(1.0f)
        }

        bufferSource.endBatch(RenderTypes.LINES_TRANSLUCENT)
    }

    private fun argb(r: Int, g: Int, b: Int, a: Int): Int {
        return (a and 255) shl 24 or ((r and 255) shl 16) or ((g and 255) shl 8) or (b and 255)
    }
}
