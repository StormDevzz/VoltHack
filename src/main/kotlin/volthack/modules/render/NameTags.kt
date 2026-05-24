package volthack.modules.render

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.item.ItemStack
import volthack.event.EventBus
import volthack.event.Render3DEvent
import volthack.gui.font.GUIFontRenderer
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.render.Render2DUtil

object NameTags : Module("NameTags", "Shows info tags above entities", Category.RENDER) {
    private val colorMode by mode("Color Mode", listOf("Static", "Distance"), "Static")
    private val customColor by color("Color", 0xFF6C63FF.toInt())
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
            val headY = entity.yo + (entity.y - entity.yo) * partialTicks + entity.bbHeight + 0.5
            val headX = entity.xo + (entity.x - entity.xo) * partialTicks
            val headZ = entity.zo + (entity.z - entity.zo) * partialTicks

            val projected = Render2DUtil.projectToScreen(headX, headY, headZ, modelView, projection) ?: continue
            if (projected.x < -200 || projected.x > mc.window.guiScaledWidth + 200 ||
                projected.y < -200 || projected.y > mc.window.guiScaledHeight + 200) continue

            val color = if (colorMode == "Distance") {
                val refRange = 64.0
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
            val x = tag.screenX
            val y = tag.screenY
            val lineH = GUIFontRenderer.height + 2

            val lines = mutableListOf<Pair<String, Int>>()
            lines.add(tag.entity.name.string to tag.color)

            if (showHealth) {
                val hp = tag.entity.health
                val maxHp = tag.entity.maxHealth.coerceAtLeast(1f)
                val hpText = "HP: ${hp.toInt()}/${maxHp.toInt()}"
                val hpColor = when {
                    hp / maxHp > 0.6f -> 0xFF2ED573.toInt()
                    hp / maxHp > 0.3f -> 0xFFFFA502.toInt()
                    else -> 0xFFFF4757.toInt()
                }
                lines.add(hpText to hpColor)
            }

            if (showDistance) {
                val dist = "%.1fm".format(player.distanceTo(tag.entity))
                lines.add(dist to 0xFFAAAAAA.toInt())
            }

            if (showUUID && tag.entity is Player) {
                lines.add(tag.entity.uuid.toString().take(8) + "..." to 0xFF909090.toInt())
            }

            if (showGameMode && tag.entity is Player) {
                val info = mc.player?.connection?.getPlayerInfo(tag.entity.uuid)
                if (info != null) {
                    lines.add("GM: ${info.gameMode.name}" to 0xFFFFA500.toInt())
                }
            }

            val maxLineWidth = lines.maxOf { GUIFontRenderer.width(it.first) }

            val mcItemSize = 16
            val heldItem = tag.entity.mainHandItem
            val offhandItem = tag.entity.offhandItem
            val hasHeldItem = showHeldItem && !heldItem.isEmpty && tag.entity is Player
            val hasOffhand = showHeldItem && !offhandItem.isEmpty && tag.entity is Player
            val hasArmor = showArmor && (0 until 4).any {
                !tag.entity.getItemBySlot(EquipmentSlot.entries[it]).isEmpty
            }

            val itemSlots = mutableListOf<ItemStack>()
            if (hasOffhand) itemSlots.add(offhandItem)
            if (hasArmor) {
                val armorSlots = listOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
                armorSlots.forEach { itemSlots.add(tag.entity.getItemBySlot(it)) }
            }
            if (hasHeldItem) itemSlots.add(heldItem)

            val totalItemW = itemSlots.size * mcItemSize
            val itemH = if (itemSlots.isNotEmpty()) mcItemSize + 2 else 0

            val bgW = maxOf(maxLineWidth + 10, totalItemW)
            val linesH = lines.size * lineH + 4
            val bgH = linesH + itemH

            ctx.fill((x - bgW / 2.0).toInt(), (y - bgH / 2.0).toInt(),
                (x + bgW / 2.0).toInt(), (y + bgH / 2.0).toInt(), 0x90000000.toInt())

            var currentY = y - bgH / 2.0 + 2
            for ((text, color) in lines) {
                Render2DUtil.drawText(ctx, text, x, currentY, color, centerX = true)
                currentY += lineH
            }

            if (itemSlots.isNotEmpty()) {
                val startX = (x - totalItemW / 2.0).toInt()
                val itemY = (y + bgH / 2.0 - mcItemSize).toInt()
                for (i in itemSlots.indices) {
                    val stack = itemSlots[i]
                    if (!stack.isEmpty) {
                        ctx.renderItem(stack, startX + i * mcItemSize, itemY)
                        ctx.renderItemDecorations(mc.font, stack, startX + i * mcItemSize, itemY)
                    }
                }
            }
        }
    }

    private fun isValidTarget(entity: LivingEntity): Boolean {
        if (entity is Player) return targetPlayers
        if (entity is Animal) return targetPassives
        return targetMonsters
    }
}
