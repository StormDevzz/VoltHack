package volthack.hud

import net.minecraft.client.gui.GuiGraphics

abstract class HUDElement(val name: String) {
    var x = 0
    var y = 0
    var enabled = true
    var cachedWidth = 0
    var cachedHeight = 0

    abstract fun render(ctx: GuiGraphics)
}
