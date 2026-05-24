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
    
    var currentTarget: LivingEntity? = null
    private var lastHitTime = 0L
    private var cachedName: String? = null
    private var cachedTargetId: Int = -1

    init {
        x = 400
        y = 200
        cachedWidth = 170
        cachedHeight = 65
        enabled = false
    }

    private fun detectTarget(): LivingEntity? {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return null

        // 1. Check KillAura target
        if (KillAura.enabled && KillAura.currentTarget != null) {
            return KillAura.currentTarget
        }

        // 2. Check Trigger target
        if (Trigger.enabled && Trigger.currentTarget != null) {
            return Trigger.currentTarget
        }

        // 3. Detect manual attacks
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
        }

        val target = currentTarget ?: return

        val now = System.currentTimeMillis()
        if (target.isDeadOrDying || !target.isAlive || now - lastHitTime > 8000) {
            currentTarget = null
            cachedName = null
            return
        }

        cachedWidth = 170
        cachedHeight = 65

        ctx.fill(x, y, x + cachedWidth, y + cachedHeight, VoltHackTheme.surface)
        ctx.fill(x, y, x + cachedWidth, y + 1, customColor)

        try {
            val scale = 20
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                ctx,
                x + 5, y + 5,
                x + 45, y + cachedHeight - 5,
                scale, 0.0625f, 0f, 0f, target
            )
        } catch (e: Exception) {}

        val nameText = if (target.id == cachedTargetId && cachedName != null) cachedName!! else {
            target.name.string.also { cachedName = it; cachedTargetId = target.id }
        }
        val nameX = x + 50f
        var cy = y + 6f
        GUIFontRenderer.draw(ctx, nameText, nameX, cy, VoltHackTheme.textPrimary)
        cy += GUIFontRenderer.height + 4f

        val hp = target.health
        val maxHp = target.maxHealth.coerceAtLeast(1f)
        val hpPct = (hp / maxHp).coerceIn(0f, 1f)

        val barX = x + 50
        val barY = cy.toInt()
        val barWidth = 110
        val barHeight = 6

        ctx.fill(barX, barY, barX + barWidth, barY + barHeight, VoltHackTheme.surfaceLight)
        val hpColor = when {
            hpPct > 0.5f -> 0xFF2ED573.toInt()
            hpPct > 0.2f -> 0xFFFFA502.toInt()
            else -> 0xFFFF4757.toInt()
        }
        ctx.fill(barX, barY, barX + (barWidth * hpPct).toInt(), barY + barHeight, hpColor)

        cy += barHeight + 4f

        val hpText = "${hp.roundToInt()} / ${maxHp.roundToInt()} HP"
        GUIFontRenderer.draw(ctx, hpText, nameX, cy, VoltHackTheme.textSecondary)
        cy += GUIFontRenderer.height + 4f

        var itemX = x + 50
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
}
