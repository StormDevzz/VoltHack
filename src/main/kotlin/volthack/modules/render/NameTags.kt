package volthack.modules.render

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.item.ItemStack
import volthack.event.EventBus
import volthack.event.Render3DEvent
import volthack.gui.font.GUIFontRenderer
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.render.Render2DUtil
import kotlin.math.roundToInt

object NameTags : Module("NameTags", "Shows info tags above entities", Category.RENDER) {
    private val colorMode by mode("Color Mode", listOf("Static", "Distance"), "Static")
    private val customColor by color("Color", 0xFF6C63FF.toInt())
    private val bgColor by color("BG Color", 0xE00D0D1A.toInt(), "The background color of the tag plate")
    private val showHealth by boolean("Show Health", true)
    private val showDistance by boolean("Show Distance", true)
    private val showArmor by boolean("Show Armor", true)
    private val showHeldItem by boolean("Show Held Item", true)
    private val showUUID by boolean("Show UUID", false)
    private val showGameMode by boolean("Show GameMode", false)
    private val targetPlayers by boolean("Players", true)
    private val targetMonsters by boolean("Monsters", true)
    private val targetPassives by boolean("Passives", false)
    private val maxRange by float("Max Range", 64.0f, 8.0f, 128.0f, 4.0f)
    private val scale by float("Scale", 1.0f, 0.5f, 3.0f, 0.1f)

    private data class TagData(
        val entity: LivingEntity,
        val screenX: Double,
        val screenY: Double,
        val color: Int
    )

    private var tags = listOf<TagData>()
    private var modelView = org.joml.Matrix4f()
    private var projection = org.joml.Matrix4f()
    private var partialTicks = 0f
    private var hasData = false

    init {
        EventBus.listen<Render3DEvent> { onRender3D(it) }
    }

    private fun onRender3D(event: Render3DEvent) {
        if (!enabled) return
        modelView = event.modelViewMatrix
        projection = event.projectionMatrix
        partialTicks = event.partialTicks
        hasData = true

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val world = mc.level ?: return
        val camPos = mc.gameRenderer.mainCamera.position()

        val newTags = mutableListOf<TagData>()
        val entities = world.entitiesForRendering()
            .filterIsInstance<LivingEntity>()
            .filter { it != player && it.isAlive && !it.isDeadOrDying }
            .filter { isValidTarget(it) }
            .filter { camPos.distanceToSqr(it.position()) <= maxRange * maxRange }

        for (entity in entities) {
            val headY = entity.yo + (entity.y - entity.yo) * partialTicks + entity.bbHeight + 0.35
            val headX = entity.xo + (entity.x - entity.xo) * partialTicks
            val headZ = entity.zo + (entity.z - entity.zo) * partialTicks

            val projected = Render2DUtil.projectToScreen(headX, headY, headZ, modelView, projection) ?: continue
            if (projected.x < -200 || projected.x > mc.window.guiScaledWidth + 200 ||
                projected.y < -200 || projected.y > mc.window.guiScaledHeight + 200) continue

            val color = if (colorMode == "Distance") {
                val refRange = maxRange.toDouble()
                val pct = (camPos.distanceTo(entity.position()) / refRange).coerceIn(0.0, 1.0)
                val cr = (255 * (1f - pct)).toInt().coerceIn(0, 255)
                val cg = (255 * pct).toInt().coerceIn(0, 255)
                0xFF000000.toInt() or (cr shl 16) or (cg shl 8)
            } else {
                customColor
            }

            newTags.add(TagData(entity, projected.x, projected.y, color))
        }
        tags = newTags
    }

    fun render2D(ctx: GuiGraphics) {
        if (!enabled || !hasData) return

        val player = Minecraft.getInstance().player ?: return
        val mc = Minecraft.getInstance()

        for (tag in tags) {
            val entity = tag.entity
            val x = tag.screenX
            val y = tag.screenY

            val scaleVal = scale
            val stack = (ctx as volthack.mixin.render.GuiGraphicsAccessor).pose
            stack.pushMatrix()
            stack.translate(x.toFloat(), y.toFloat())
            stack.scale(scaleVal, scaleVal)
            stack.translate(-x.toFloat(), -y.toFloat())

            // Build premium horizontal formatted tag
            val sb = StringBuilder()
            
            // The entity name
            sb.append("§f").append(entity.name.string)

            if (showHealth) {
                val hp = entity.health.roundToInt()
                val maxHp = entity.maxHealth.coerceAtLeast(1f).roundToInt()
                val hpPct = entity.health / entity.maxHealth
                val hpColorCode = when {
                    hpPct > 0.6f -> "§a"
                    hpPct > 0.3f -> "§e"
                    else -> "§c"
                }
                sb.append(" ").append(hpColorCode).append(hp)
            }

            if (showDistance) {
                val dist = player.distanceTo(entity).roundToInt()
                sb.append(" §7").append(dist).append("m")
            }

            if (showUUID && entity is Player) {
                sb.append(" §8(").append(entity.uuid.toString().take(6)).append(")")
            }

            if (showGameMode && entity is Player) {
                val info = mc.player?.connection?.getPlayerInfo(entity.uuid)
                if (info != null) {
                    val gmText = when (info.gameMode.name.lowercase()) {
                        "creative" -> "§6C"
                        "survival" -> "§aS"
                        "adventure" -> "§eA"
                        else -> "§7Sp"
                    }
                    sb.append(" [").append(gmText).append("§f]")
                }
            }

            val text = sb.toString()
            val textW = GUIFontRenderer.width(text)
            val lineH = GUIFontRenderer.height

            val mcItemSize = 16
            val heldItem = entity.mainHandItem
            val offhandItem = entity.offhandItem
            val hasHeldItem = showHeldItem && !heldItem.isEmpty && entity is Player
            val hasOffhand = showHeldItem && !offhandItem.isEmpty && entity is Player
            val hasArmor = showArmor && (0 until 4).any {
                !entity.getItemBySlot(EquipmentSlot.entries[it]).isEmpty
            }

            val itemSlots = mutableListOf<ItemStack>()
            if (hasOffhand) itemSlots.add(offhandItem)
            if (hasArmor) {
                val armorSlots = listOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
                armorSlots.forEach { itemSlots.add(entity.getItemBySlot(it)) }
            }
            if (hasHeldItem) itemSlots.add(heldItem)

            val totalItemW = itemSlots.size * mcItemSize
            val itemH = if (itemSlots.isNotEmpty()) mcItemSize + 4 else 0

            val bgW = maxOf(textW + 12, totalItemW + 12)
            val bgH = lineH + itemH + 6

            val bx = (x - bgW / 2.0).toInt()
            val by = (y - bgH / 2.0).toInt()
            val bx2 = (x + bgW / 2.0).toInt()
            val by2 = (y + bgH / 2.0).toInt()

            // Draw glowing outline border and translucent dark base
            ctx.fill(bx - 1, by - 1, bx2 + 1, by2 + 1, tag.color)
            ctx.fill(bx, by, bx2, by2, bgColor)

            // Render single row text
            val textY = by + 4f
            Render2DUtil.drawText(ctx, text, x, textY.toDouble(), 0xFFFFFFFF.toInt(), centerX = true)

            // Render armor and items
            if (itemSlots.isNotEmpty()) {
                val startX = (x - totalItemW / 2.0).toInt()
                val itemY = (by2 - mcItemSize - 4).toInt()
                for (i in itemSlots.indices) {
                    val stack = itemSlots[i]
                    if (!stack.isEmpty) {
                        ctx.renderItem(stack, startX + i * mcItemSize, itemY)
                        ctx.renderItemDecorations(mc.font, stack, startX + i * mcItemSize, itemY)
                    }
                }
            }
            stack.popMatrix()
        }
    }

    private fun isValidTarget(entity: LivingEntity): Boolean {
        if (entity is Player) return targetPlayers
        if (entity is Animal) return targetPassives
        if (entity is Monster) return targetMonsters
        return false
    }
}
