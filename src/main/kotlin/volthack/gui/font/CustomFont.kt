package volthack.gui.font

import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.Identifier
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.function.Supplier
import javax.imageio.ImageIO

class CustomFont(val fontName: String, val size: Float) {
    private val font: Font
    private val charWidths = IntArray(256)
    private val charU = FloatArray(256)
    private val charV = FloatArray(256)
    private val charW = FloatArray(256)
    private val charH = FloatArray(256)
    
    private var textureId: Identifier? = null
    private val texWidth = 512
    private val texHeight = 512
    var lineHeight = 9

    /** True once the glyph atlas has been successfully uploaded to the GPU texture manager. */
    fun isReady(): Boolean = textureId != null

    init {
        var derivedFont: Font
        try {
            val stream: InputStream? = javaClass.getResourceAsStream("/assets/volthack/fonts/$fontName.ttf")
                ?: javaClass.getResourceAsStream("/assets/volthack/fonts/${fontName}.ttf")
            derivedFont = if (stream != null) {
                Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(size)
            } else {
                Font("Arial", Font.PLAIN, size.toInt())
            }
        } catch (e: Exception) {
            derivedFont = Font("Arial", Font.PLAIN, size.toInt())
        }
        font = derivedFont
        setupGlyphs()
    }

    private fun setupGlyphs() {
        val img = BufferedImage(texWidth, texHeight, BufferedImage.TYPE_INT_ARGB)
        val g = img.graphics as Graphics2D
        g.font = font
        g.color = Color(255, 255, 255, 0)
        g.fillRect(0, 0, texWidth, texHeight)
        g.color = Color.WHITE
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val fm = g.fontMetrics
        lineHeight = fm.height

        var curX = 2
        var curY = 2

        for (i in 0..255) {
            val c = i.toChar()
            val bounds = fm.getStringBounds(c.toString(), g)
            val w = bounds.width.toInt().coerceAtLeast(1)
            val h = bounds.height.toInt().coerceAtLeast(1)

            if (curX + w >= texWidth) {
                curX = 2
                curY += fm.height + 4
            }

            g.drawString(c.toString(), curX, curY + fm.ascent)

            charWidths[i] = w
            charU[i] = curX.toFloat() / texWidth
            charV[i] = curY.toFloat() / texHeight
            charW[i] = w.toFloat() / texWidth
            charH[i] = h.toFloat() / texHeight

            curX += w + 4
        }

        g.dispose()

        val mc = Minecraft.getInstance()
        mc.execute {
            try {
                val baos = ByteArrayOutputStream()
                ImageIO.write(img, "png", baos)
                val bytes = baos.toByteArray()
                val nativeImg = NativeImage.read(ByteArrayInputStream(bytes))
                
                val dynTex = DynamicTexture(Supplier { "volthack_font_${fontName.lowercase()}" }, nativeImg)
                val id = Identifier.tryParse("volthack:fonts_${fontName.lowercase().replace("-", "_")}")
                if (id != null) {
                    mc.textureManager.register(id, dynTex)
                    textureId = id
                }
            } catch (e: Exception) {}
        }
    }

    fun drawString(ctx: GuiGraphics, text: String, x: Float, y: Float, color: Int) {
        val tex = textureId ?: return
        
        val r = (color shr 16 and 255)
        val g = (color shr 8 and 255)
        val b = (color and 255)
        val a = (color shr 24 and 255)
        val packedColor = (a shl 24) or (b shl 16) or (g shl 8) or r

        val pose = ctx.pose()
        val m00 = pose.m00()
        val m01 = pose.m01()
        val m10 = pose.m10()
        val m11 = pose.m11()
        val m20 = pose.m20()
        val m21 = pose.m21()

        fun transformX(px: Float, py: Float): Float = px * m00 + py * m10 + m20
        fun transformY(px: Float, py: Float): Float = px * m01 + py * m11 + m21

        val renderType = RenderTypes.text(tex)
        val mc = Minecraft.getInstance()
        val consumer = mc.renderBuffers().bufferSource().getBuffer(renderType)

        var curX = x
        for (char in text) {
            val idx = char.code
            if (idx >= 256) continue
            
            val w = charWidths[idx]
            val h = lineHeight
            
            val u = charU[idx]
            val v = charV[idx]
            val uw = charW[idx]
            val vh = charH[idx]

            consumer.addVertex(transformX(curX, y), transformY(curX, y), 0f, packedColor, u, v, 0, 15728880, 0f, 0f, -1f)
            consumer.addVertex(transformX(curX, y + h.toFloat()), transformY(curX, y + h.toFloat()), 0f, packedColor, u, v + vh, 0, 15728880, 0f, 0f, -1f)
            consumer.addVertex(transformX(curX + w.toFloat(), y + h.toFloat()), transformY(curX + w.toFloat(), y + h.toFloat()), 0f, packedColor, u + uw, v + vh, 0, 15728880, 0f, 0f, -1f)
            consumer.addVertex(transformX(curX + w.toFloat(), y), transformY(curX + w.toFloat(), y), 0f, packedColor, u + uw, v, 0, 15728880, 0f, 0f, -1f)

            curX += w
        }
        mc.renderBuffers().bufferSource().endBatch(renderType)
    }

    fun getWidth(text: String): Int {
        var w = 0
        for (char in text) {
            val idx = char.code
            if (idx < 256) {
                w += charWidths[idx]
            }
        }
        return w
    }
}
