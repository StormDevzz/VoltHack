package volthack.hud.elements

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import volthack.hud.HUDElement

class PlayerViewElement : HUDElement("PlayerView") {
    private val viewScale by int("Scale", 30, 10, 80)

    init {
        x = 10
        y = 50
        cachedWidth = 60
        cachedHeight = 80
        enabled = false
    }

    override fun draw(ctx: GuiGraphics) {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        cachedWidth = (viewScale * 2).coerceAtLeast(40)
        cachedHeight = (viewScale * 2.6).toInt().coerceAtLeast(50)

        // Renders 3D player following mouse dynamically using exact 1.21.11 signature
        InventoryScreen.renderEntityInInventoryFollowsMouse(
            ctx,
            x,
            y,
            x + cachedWidth,
            y + cachedHeight,
            viewScale,
            0.0625f,
            0f,
            0f,
            player
        )
    }
}
