package volthack.hud.elements

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme
import volthack.hud.HUDElement

class ArmorElement : HUDElement("Armor") {
    private val customColor by color("Color", 0xFF6C63FF.toInt())
    private val showDurability by boolean("Show Durability", true)

    init {
        x = 200
        y = 130
        cachedWidth = 130
        cachedHeight = 30
        enabled = false
    }

    override fun draw(ctx: GuiGraphics) {
        val player = Minecraft.getInstance().player ?: return

        val armorSlots = listOf(
            player.getItemBySlot(EquipmentSlot.FEET),
            player.getItemBySlot(EquipmentSlot.LEGS),
            player.getItemBySlot(EquipmentSlot.CHEST),
            player.getItemBySlot(EquipmentSlot.HEAD)
        )
        val hasArmor = armorSlots.any { !it.isEmpty }

        if (!hasArmor) {
            cachedWidth = GUIFontRenderer.width("No armor equipped") + 16
            cachedHeight = GUIFontRenderer.height + 10
            ctx.fill(x, y, x + cachedWidth, y + cachedHeight, VoltHackTheme.surface)
            ctx.fill(x, y, x + cachedWidth, y + 1, customColor)
            GUIFontRenderer.draw(
                ctx, "No armor equipped",
                (x + 8).toFloat(), (y + (cachedHeight - GUIFontRenderer.height) / 2f),
                VoltHackTheme.textSecondary
            )
            return
        }

        val slotW = 22
        val padding = 4
        val totalW = 4 * slotW + padding * 2
        cachedWidth = totalW
        cachedHeight = 30

        ctx.fill(x, y, x + cachedWidth, y + cachedHeight, VoltHackTheme.surface)
        ctx.fill(x, y, x + cachedWidth, y + 1, customColor)

        val armorLabels = listOf("B", "L", "C", "H")
        for (i in armorSlots.indices) {
            val stack = armorSlots[i]
            val slotX = x + padding + i * slotW
            val slotY = y + 4

            ctx.fill(slotX, slotY, slotX + 20, slotY + 20, VoltHackTheme.surfaceLight)

            GUIFontRenderer.draw(
                ctx, armorLabels[i],
                (slotX + 1).toFloat(), (slotY + 1).toFloat(),
                VoltHackTheme.textDisabled
            )

            if (!stack.isEmpty) {
                ctx.renderItem(stack, slotX + 2, slotY + 4)
                if (showDurability && stack.isDamaged) {
                    val pct = (1f - stack.damageValue.toFloat() / stack.maxDamage.toFloat()).coerceIn(0f, 1f)
                    val barColor = when {
                        pct > 0.5f -> 0xFF2ED573.toInt()
                        pct > 0.2f -> 0xFFFFA502.toInt()
                        else -> 0xFFFF4757.toInt()
                    }
                    ctx.fill(slotX + 2, slotY + 18, slotX + 18, slotY + 19, 0xFF000000.toInt())
                    ctx.fill(slotX + 2, slotY + 18, slotX + 2 + (16 * pct).toInt(), slotY + 19, barColor)
                }
            }
        }
    }
}
