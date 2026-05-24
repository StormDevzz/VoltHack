package volthack.util.render

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.rendertype.RenderTypes
import org.lwjgl.opengl.GL11

object Render3DUtil {

    fun drawRing(
        x: Double, y: Double, z: Double,
        radius: Float,
        r: Int, g: Int, b: Int, a: Int,
        spinAngle: Double,
        throughWalls: Boolean
    ) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDisable(GL11.GL_CULL_FACE)
        if (throughWalls) {
            GL11.glDisable(GL11.GL_DEPTH_TEST)
        } else {
            GL11.glEnable(GL11.GL_DEPTH_TEST)
        }

        val tesselator = Tesselator.getInstance()
        val builder = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR)
        
        val segments = 64
        for (i in 0..segments) {
            val angle = (i * 2 * Math.PI / segments) + spinAngle
            val dx = Math.cos(angle) * radius
            val dz = Math.sin(angle) * radius
            builder.addVertex((x + dx).toFloat(), y.toFloat(), (z + dz).toFloat())
                .setColor(r, g, b, a)
        }
        
        val buffer = builder.build()
        if (buffer != null) {
            RenderTypes.lines().draw(buffer)
        }
        
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glDisable(GL11.GL_BLEND)
    }

    fun drawBand(
        x: Double, y: Double, z: Double,
        radius: Float, height: Float,
        r: Int, g: Int, b: Int, a: Int,
        spinAngle: Double,
        throughWalls: Boolean
    ) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDisable(GL11.GL_CULL_FACE)
        if (throughWalls) {
            GL11.glDisable(GL11.GL_DEPTH_TEST)
        } else {
            GL11.glEnable(GL11.GL_DEPTH_TEST)
        }

        val tesselator = Tesselator.getInstance()
        val builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)
        
        val segments = 64
        for (i in 0 until segments) {
            val angle1 = (i * 2 * Math.PI / segments) + spinAngle
            val angle2 = ((i + 1) * 2 * Math.PI / segments) + spinAngle

            val dx1 = Math.cos(angle1) * radius
            val dz1 = Math.sin(angle1) * radius
            val dx2 = Math.cos(angle2) * radius
            val dz2 = Math.sin(angle2) * radius

            builder.addVertex((x + dx1).toFloat(), y.toFloat(), (z + dz1).toFloat())
                .setColor(r, g, b, a)
            builder.addVertex((x + dx1).toFloat(), (y + height).toFloat(), (z + dz1).toFloat())
                .setColor(r, g, b, a)
            builder.addVertex((x + dx2).toFloat(), (y + height).toFloat(), (z + dz2).toFloat())
                .setColor(r, g, b, a)
            builder.addVertex((x + dx2).toFloat(), y.toFloat(), (z + dz2).toFloat())
                .setColor(r, g, b, a)
        }
        
        val buffer = builder.build()
        if (buffer != null) {
            RenderTypes.debugQuads().draw(buffer)
        }
        
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glDisable(GL11.GL_BLEND)
    }

    fun drawGlowBand(
        x: Double, y: Double, z: Double,
        radius: Float, height: Float,
        r: Int, g: Int, b: Int, a: Int,
        spinAngle: Double,
        throughWalls: Boolean
    ) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDisable(GL11.GL_CULL_FACE)
        if (throughWalls) {
            GL11.glDisable(GL11.GL_DEPTH_TEST)
        } else {
            GL11.glEnable(GL11.GL_DEPTH_TEST)
        }

        val tesselator = Tesselator.getInstance()
        val builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)
        
        val segments = 64
        for (i in 0 until segments) {
            val angle1 = (i * 2 * Math.PI / segments) + spinAngle
            val angle2 = ((i + 1) * 2 * Math.PI / segments) + spinAngle

            val dx1 = Math.cos(angle1) * radius
            val dz1 = Math.sin(angle1) * radius
            val dx2 = Math.cos(angle2) * radius
            val dz2 = Math.sin(angle2) * radius

            builder.addVertex((x + dx1).toFloat(), y.toFloat(), (z + dz1).toFloat())
                .setColor(r, g, b, (a * 0.6f).toInt())
            builder.addVertex((x + dx1).toFloat(), (y + height).toFloat(), (z + dz1).toFloat())
                .setColor(r, g, b, 0)
            builder.addVertex((x + dx2).toFloat(), (y + height).toFloat(), (z + dz2).toFloat())
                .setColor(r, g, b, 0)
            builder.addVertex((x + dx2).toFloat(), y.toFloat(), (z + dz2).toFloat())
                .setColor(r, g, b, (a * 0.6f).toInt())
        }
        
        val buffer = builder.build()
        if (buffer != null) {
            RenderTypes.debugQuads().draw(buffer)
        }
        
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glDisable(GL11.GL_BLEND)
    }
}
