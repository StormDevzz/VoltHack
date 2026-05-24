package volthack.hud.elements

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme
import volthack.hud.HUDElement
import kotlin.math.roundToInt

class TargetRenderElement : HUDElement("TargetRender") {
    private val customColor by color("Color", 0xFF6C63FF.toInt())
    
    private var currentTarget: LivingEntity? = null
    private var lastHitTime = 0L

    init {
        x = 400
        y = 200
        cachedWidth = 170
        cachedHeight = 65
        enabled = false
    }

    override fun draw(ctx: GuiGraphics) {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val world = mc.level ?: return

        // 1. Detect if player attacks a living entity
        if (mc.options.keyAttack.isDown && mc.hitResult?.type == HitResult.Type.ENTITY) {
            val hit = mc.hitResult as? EntityHitResult
            val entity = hit?.entity
            if (entity is LivingEntity && entity != player) {
                currentTarget = entity
                lastHitTime = System.currentTimeMillis()
            }
        }

        val target = currentTarget ?: return

        // 2. Hide target if it's dead, removed from world, or has not been hit for 8 seconds
        val now = System.currentTimeMillis()
        if (target.isDeadOrDying || !target.isAlive || now - lastHitTime > 8000) {
            currentTarget = null
            return
        }

        cachedWidth = 170
        cachedHeight = 65

        // Draw card background
        ctx.fill(x, y, x + cachedWidth, y + cachedHeight, VoltHackTheme.surface)
        ctx.fill(x, y, x + cachedWidth, y + 1, customColor)

        // Draw 3D Target Entity model
        try {
            val scale = 20
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                ctx,
                x + 5,
                y + 5,
                x + 45,
                y + cachedHeight - 5,
                scale,
                0.0625f,
                0f,
                0f,
                target
            )
        } catch (e: Exception) {
            // fallback in case rendering fails
        }

        // Draw Target Name
        val nameText = target.name.string
        val nameX = x + 50f
        var cy = y + 6f
        GUIFontRenderer.draw(ctx, nameText, nameX, cy, VoltHackTheme.textPrimary)
        cy += GUIFontRenderer.height + 4f

        // Draw Health Bar
        val hp = target.health
        val maxHp = target.maxHealth.coerceAtLeast(1f)
        val hpPct = (hp / maxHp).coerceIn(0f, 1f)
        
        val barX = x + 50
        val barY = cy.toInt()
        val barWidth = 110
        val barHeight = 6
        
        // draw background bar
        ctx.fill(barX, barY, barX + barWidth, barY + barHeight, VoltHackTheme.surfaceLight)
        // draw filled bar
        val hpColor = when {
            hpPct > 0.5f -> 0xFF2ED573.toInt() // Green
            hpPct > 0.2f -> 0xFFFFA502.toInt() // Orange
            else -> 0xFFFF4757.toInt() // Red
        }
        ctx.fill(barX, barY, barX + (barWidth * hpPct).toInt(), barY + barHeight, hpColor)
        
        cy += barHeight + 4f

        // Draw Health Text
        val hpText = "${hp.roundToInt()} / ${maxHp.roundToInt()} HP"
        GUIFontRenderer.draw(ctx, hpText, nameX, cy, VoltHackTheme.textSecondary)
        cy += GUIFontRenderer.height + 4f

        // Draw Target Armor and Hand Items
        var itemX = x + 50
        val itemY = cy.toInt() - 2
        
        val itemsToDraw = listOf(
            target.mainHandItem,
            target.offhandItem,
            target.getItemBySlot(EquipmentSlot.HEAD),
            target.getItemBySlot(EquipmentSlot.CHEST),
            target.getItemBySlot(EquipmentSlot.LEGS),
            target.getItemBySlot(EquipmentSlot.FEET)
        )

        for (stack in itemsToDraw) {
            if (stack != null && !stack.isEmpty) {
                ctx.renderItem(stack, itemX, itemY)
                ctx.renderItemDecorations(mc.font, stack, itemX, itemY)
                itemX += 18
            }
        }
    }
}
