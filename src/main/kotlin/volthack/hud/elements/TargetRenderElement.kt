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
import volthack.modules.combat.KillAura
import volthack.modules.combat.Trigger
import kotlin.math.roundToInt

class TargetRenderElement : HUDElement("TargetRender") {
    private val customColor by color("Color", 0xFF6C63FF.toInt())
    private val bgColor by color("BG Color", 0xEE16162A.toInt(), "The background color of target rendering card")
    private val hpStyle by mode("HP Style", listOf("Bar", "Circle"), "Bar", "How target health is displayed")
    
    var currentTarget: LivingEntity? = null
    private var lastHitTime = 0L
    private var cachedName: String? = null
    private var cachedTargetId: Int = -1
    private var smoothHpPct = -1f

    init {
        x = 400
        y = 200
        cachedWidth = 180
        cachedHeight = 70
        enabled = false
    }

    private fun detectTarget(): LivingEntity? {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return null

        if (KillAura.enabled && KillAura.currentTarget != null) {
            return KillAura.currentTarget
        }

        if (Trigger.enabled && Trigger.currentTarget != null) {
            return Trigger.currentTarget
        }

        if (mc.options.keyAttack.isDown && mc.hitResult?.type == HitResult.Type.ENTITY) {
            val hit = mc.hitResult as? EntityHitResult
            val entity = hit?.entity
            if (entity is LivingEntity && entity != player) {
                return entity
            }
        }

        return currentTarget
    }

    override fun draw(ctx: GuiGraphics) {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        val newTarget = detectTarget()
        if (newTarget != null && newTarget != currentTarget) {
            currentTarget = newTarget
            lastHitTime = System.currentTimeMillis()
            cachedName = null
            smoothHpPct = -1f
        }

        val target = currentTarget ?: return

        val now = System.currentTimeMillis()
        if (target.isDeadOrDying || !target.isAlive || now - lastHitTime > 8000) {
            currentTarget = null
            cachedName = null
            smoothHpPct = -1f
            return
        }

        cachedWidth = 180
        cachedHeight = 70

        // Premium translucent glassmorphic card container with custom glow borders
        val glowColor = (0x33000000.toLong() or (customColor.toLong() and 0x00FFFFFF)).toInt()
        
        ctx.fill(x - 2, y - 2, x + cachedWidth + 2, y + cachedHeight + 2, glowColor)
        ctx.fill(x - 1, y - 1, x + cachedWidth + 1, y + cachedHeight + 1, customColor)
        ctx.fill(x, y, x + cachedWidth, y + cachedHeight, bgColor)

        try {
            val scale = 22
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                ctx,
                x + 4, y + 4,
                x + 42, y + cachedHeight - 4,
                scale, 0.0625f, 0f, 0f, target
            )
        } catch (e: Exception) {}

        val nameText = if (target.id == cachedTargetId && cachedName != null) cachedName!! else {
            target.name.string.also { cachedName = it; cachedTargetId = target.id }
        }
        
        val nameX = x + 48f
        var cy = y + 6f
        GUIFontRenderer.draw(ctx, nameText, nameX, cy, VoltHackTheme.textPrimary)
        cy += GUIFontRenderer.height + 3f

        val hp = target.health
        val maxHp = target.maxHealth.coerceAtLeast(1f)
        val hpPct = (hp / maxHp).coerceIn(0f, 1f)

        if (smoothHpPct < 0f || smoothHpPct.isNaN()) {
            smoothHpPct = hpPct
        } else {
            smoothHpPct += (hpPct - smoothHpPct) * 0.14f
        }
        smoothHpPct = smoothHpPct.coerceIn(0f, 1f)

        val hpColor = when {
            hpPct > 0.5f -> 0xFF2ED573.toInt()
            hpPct > 0.2f -> 0xFFFFA502.toInt()
            else -> 0xFFFF4757.toInt()
        }

        if (hpStyle == "Bar") {
            val barX = x + 48
            val barY = cy.toInt()
            val barWidth = 124
            val barHeight = 7

            // Dark track background
            ctx.fill(barX, barY, barX + barWidth, barY + barHeight, 0x30FFFFFF.toInt())
            // Render dynamic smooth bar
            val drawW = (barWidth * smoothHpPct).toInt()
            ctx.fill(barX, barY, barX + drawW, barY + barHeight, hpColor)

            cy += barHeight + 4f

            val hpText = "${hp.roundToInt()} / ${maxHp.roundToInt()} HP"
            GUIFontRenderer.draw(ctx, hpText, nameX, cy, VoltHackTheme.textSecondary)
            cy += GUIFontRenderer.height + 4f
        } else {
            // Circle Mode: Draw a gorgeous circular progress ring on the right side
            val circleRadius = 14
            val cx = x + 154
            val cyCircle = y + 34
            
            // Backdrop
            drawBackdropCircle(ctx, cx, cyCircle, circleRadius)
            // Active HP Ring
            drawProgressRing(ctx, cx, cyCircle, circleRadius, smoothHpPct, hpColor)

            // Centered HP percentage text inside the circle
            val pctStr = "${(hpPct * 100).toInt()}%"
            val tw = GUIFontRenderer.width(pctStr)
            GUIFontRenderer.draw(ctx, pctStr, cx - tw / 2f, cyCircle - GUIFontRenderer.height / 2f, VoltHackTheme.textPrimary)

            val hpText = "${hp.roundToInt()}/${maxHp.roundToInt()} HP"
            GUIFontRenderer.draw(ctx, hpText, nameX, cy, VoltHackTheme.textSecondary)
            cy += GUIFontRenderer.height + 8f
        }

        var itemX = x + 48
        val itemY = cy.toInt() - 2

        val itemsToDraw = listOfNotNull(
            target.mainHandItem.let { if (it.isEmpty) null else it },
            target.offhandItem.let { if (it.isEmpty) null else it },
            target.getItemBySlot(EquipmentSlot.HEAD).let { if (it.isEmpty) null else it },
            target.getItemBySlot(EquipmentSlot.CHEST).let { if (it.isEmpty) null else it },
            target.getItemBySlot(EquipmentSlot.LEGS).let { if (it.isEmpty) null else it },
            target.getItemBySlot(EquipmentSlot.FEET).let { if (it.isEmpty) null else it }
        )

        for (stack in itemsToDraw) {
            ctx.renderItem(stack, itemX, itemY)
            ctx.renderItemDecorations(mc.font, stack, itemX, itemY)
            itemX += 18
        }
    }

    private fun drawBackdropCircle(ctx: GuiGraphics, cx: Int, cy: Int, radius: Int) {
        val segments = 360
        for (i in 0 until segments) {
            val angle = i * (2.0 * Math.PI) / segments
            val rx = cx + radius * Math.cos(angle)
            val ry = cy + radius * Math.sin(angle)
            ctx.fill((rx - 0.75).toInt(), (ry - 0.75).toInt(), (rx + 1.75).toInt(), (ry + 1.75).toInt(), 0x33FFFFFF.toInt())
        }
    }

    private fun drawProgressRing(ctx: GuiGraphics, cx: Int, cy: Int, radius: Int, pct: Float, color: Int) {
        val segments = 360
        val activeSegments = (pct * segments).toInt().coerceIn(0, segments)
        if (activeSegments == 0) return
        
        for (i in 0 until activeSegments) {
            val angle = -Math.PI / 2 + i * (2.0 * Math.PI) / segments
            val rx = cx + radius * Math.cos(angle)
            val ry = cy + radius * Math.sin(angle)
            ctx.fill((rx - 0.9).toInt(), (ry - 0.9).toInt(), (rx + 1.9).toInt(), (ry + 1.9).toInt(), color)
        }
    }
}
