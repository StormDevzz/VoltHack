package volthack.modules.render

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.phys.AABB
import volthack.event.EventBus
import volthack.event.Render3DEvent
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.render.Render2DUtil
import volthack.gui.font.GUIFontRenderer

object ESP : Module("ESP", "2D entity highlighting", Category.RENDER) {
    private val mode by mode("Mode", listOf("Box", "Outline", "Both"), "Both")
    private val colorMode by mode("Color Mode", listOf("Static", "Distance"), "Static")
    private val customColor by color("Color", 0xFF6C63FF.toInt())
    private val showHealth by boolean("Show Health", true)
    private val targetPlayers by boolean("Players", true)
    private val targetMonsters by boolean("Monsters", true)
    private val targetPassives by boolean("Passives", false)
    private val maxRange by float("Max Range", 64.0f, 8.0f, 128.0f, 4.0f)

    private data class EntityData(
        val bounds: AABB,
        val entity: LivingEntity,
        val color: Int
    )

    private var renderData = listOf<EntityData>()
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

        val entities = world.entitiesForRendering()
            .filterIsInstance<LivingEntity>()
            .filter { it != player && it.isAlive && !it.isDeadOrDying }
            .filter { isValidTarget(it) }
            .filter { player.distanceTo(it) <= maxRange }

        val newData = mutableListOf<EntityData>()
        for (entity in entities) {
            val bounds = Render2DUtil.getEntityScreenBounds(entity, partialTicks, modelView, projection) ?: continue
            if (bounds.maxX < 0 || bounds.minX > mc.window.guiScaledWidth ||
                bounds.maxY < 0 || bounds.minY > mc.window.guiScaledHeight) continue

            val color = if (colorMode == "Distance") {
                val dist = player.distanceTo(entity)
                val pct = (dist / maxRange).coerceIn(0f, 1f)
                val r = (255 * (1f - pct)).toInt().coerceIn(0, 255)
                val g = (255 * pct).toInt().coerceIn(0, 255)
                0xFF000000.toInt() or (r shl 16) or (g shl 8)
            } else {
                customColor
            }

            newData.add(EntityData(bounds, entity, color))
        }
        renderData = newData
    }

    fun render2D(ctx: GuiGraphics) {
        if (!enabled || !hasData) return

        for (data in renderData) {
            val b = data.bounds
            val x = b.minX; val y = b.minY
            val w = b.maxX - b.minX; val h = b.maxY - b.minY

            val boxColor = (data.color and 0x00FFFFFF) or 0x30000000
            val outlineColor = data.color

            when (mode) {
                "Box" -> Render2DUtil.drawBox(ctx, x, y, w, h, boxColor)
                "Outline" -> Render2DUtil.drawOutline(ctx, x, y, w, h, outlineColor, 1)
                "Both" -> {
                    Render2DUtil.drawBox(ctx, x, y, w, h, boxColor)
                    Render2DUtil.drawOutline(ctx, x, y, w, h, outlineColor, 1)
                }
            }

            if (showHealth) {
                val barW = (w.coerceAtLeast(20.0)).toInt()
                val barX = (x + (w - barW) / 2.0).toInt()
                val barY = (y + h + 2).toInt()
                Render2DUtil.drawHealthBar(ctx, data.entity, barX, barY, barW, 3)
            }
        }
    }

    private fun isValidTarget(entity: LivingEntity): Boolean {
        if (entity is Player) return targetPlayers
        if (entity is Animal) return targetPassives
        return targetMonsters
    }
}
