package volthack.hud.elements

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.Identifier
import volthack.hud.HUDElement

class CompanionElement : HUDElement("Companion") {
    private val companionType by mode("Companion Image", listOf("kotost", "ninja", "vanya"), "kotost")
    private val imageWidth by int("Width", 100, 40, 300)
    private val imageHeight by int("Height", 100, 40, 300)

    init {
        x = 10
        y = 100
        cachedWidth = imageWidth
        cachedHeight = imageHeight
        enabled = false
    }

    override fun render(ctx: GuiGraphics) {
        cachedWidth = imageWidth
        cachedHeight = imageHeight

        val path = "textures/${companionType}.png"
        val loc = Identifier.tryParse("companion:$path") ?: return

        // 9-argument blit: Identifier, x, y, u, v, width, height, textureWidth, textureHeight
        ctx.blit(
            loc,
            x,
            y,
            0,
            0,
            imageWidth.toFloat(),
            imageHeight.toFloat(),
            imageWidth.toFloat(),
            imageHeight.toFloat()
        )
    }
}
