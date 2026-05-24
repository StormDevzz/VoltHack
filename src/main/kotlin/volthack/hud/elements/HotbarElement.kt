package volthack.hud.elements

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack
import volthack.gui.theme.VoltHackTheme
import volthack.hud.HUDElement

class HotbarElement : HUDElement("Hotbar") {
    private val customColor by color("Color", 0xFF6C63FF.toInt())

    init {
        x = 300
        y = 500
        cachedWidth = 182
        cachedHeight = 22
        enabled = false
    }

    override fun draw(ctx: GuiGraphics) {
        val mc = Minecraft.getInstance()
        val player = mc.player

        cachedWidth = 182
        cachedHeight = 24

        // Draw card background
        ctx.fill(x, y, x + cachedWidth, y + cachedHeight, VoltHackTheme.surface)
        ctx.fill(x, y, x + cachedWidth, y + 1, customColor)

        // Draw 9 slots
        for (i in 0..8) {
            val slotX = x + i * 20 + 2
            val slotY = y + 2
            ctx.fill(slotX, slotY, slotX + 18, slotY + 20, VoltHackTheme.surfaceLight)

            if (player != null) {
                val stack = player.inventory.getItem(i)
                if (!stack.isEmpty) {
                    ctx.renderItem(stack, slotX + 1, slotY + 2)
                    ctx.renderItemDecorations(mc.font, stack, slotX + 1, slotY + 2)
                }
            }
        }
    }
}
