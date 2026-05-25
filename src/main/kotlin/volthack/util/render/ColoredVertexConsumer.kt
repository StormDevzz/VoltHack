package volthack.util.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import org.joml.Matrix4fc
import org.joml.Vector3fc
import org.joml.Vector3f

class ColoredVertexConsumer(
    private val delegate: VertexConsumer,
    private val colorProvider: () -> Int
) : VertexConsumer {

    private fun getCustomColor(): Int {
        return colorProvider()
    }

    override fun addVertex(x: Float, y: Float, z: Float): VertexConsumer {
        delegate.addVertex(x, y, z)
        return this
    }

    override fun setColor(r: Int, g: Int, b: Int, a: Int): VertexConsumer {
        val color = getCustomColor()
        val ca = (color shr 24 and 255).let { if (it == 0) 255 else it }
        val cr = (color shr 16 and 255)
        val cg = (color shr 8 and 255)
        val cb = (color and 255)
        delegate.setColor(cr, cg, cb, ca)
        return this
    }

    override fun setColor(color: Int): VertexConsumer {
        delegate.setColor(getCustomColor())
        return this
    }

    override fun setUv(u: Float, v: Float): VertexConsumer {
        delegate.setUv(u, v)
        return this
    }

    override fun setUv1(u: Int, v: Int): VertexConsumer {
        delegate.setUv1(u, v)
        return this
    }

    override fun setUv2(u: Int, v: Int): VertexConsumer {
        delegate.setUv2(u, v)
        return this
    }

    override fun setNormal(x: Float, y: Float, z: Float): VertexConsumer {
        delegate.setNormal(x, y, z)
        return this
    }

    override fun setNormal(pose: PoseStack.Pose, vector3f: Vector3f): VertexConsumer {
        delegate.setNormal(pose, vector3f)
        return this
    }

    override fun addVertex(vector3fc: Vector3fc): VertexConsumer {
        delegate.addVertex(vector3fc)
        return this
    }

    override fun addVertex(matrix4fc: Matrix4fc, x: Float, y: Float, z: Float): VertexConsumer {
        delegate.addVertex(matrix4fc, x, y, z)
        return this
    }

    override fun setLineWidth(width: Float): VertexConsumer {
        delegate.setLineWidth(width)
        return this
    }

    override fun addVertex(
        f: Float, g: Float, h: Float, i: Int, j: Float, k: Float,
        l: Int, m: Int, n: Float, o: Float, p: Float
    ) {
        val color = getCustomColor()
        val ca = (color shr 24 and 255).let { if (it == 0) 255 else it }
        val cr = (color shr 16 and 255)
        val cg = (color shr 8 and 255)
        val cb = (color and 255)
        // Convert to ABGR or ARGB as expected by the vertex format
        val abgrColor = (ca and 255 shl 24) or (cb and 255 shl 16) or (cg and 255 shl 8) or (cr and 255)
        delegate.addVertex(f, g, h, abgrColor, j, k, l, m, n, o, p)
    }
}
