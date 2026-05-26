package volthack.modules.render

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import volthack.event.EventBus
import volthack.event.Render3DEvent
import volthack.setting.Category
import volthack.setting.Module

object Tracers : Module("Tracers", "Draws lines pointing to nearby entities", Category.RENDER) {
    val players by boolean("Players", true, "Trace lines to other players")
    val monsters by boolean("Monsters", false, "Trace lines to hostile mobs")
    val passives by boolean("Passives", false, "Trace lines to passive animals")
    val color by color("Color", 0xFF6C63FF.toInt(), "The color of tracer lines")
    val range by float("Range", 64.0f, 8.0f, 128.0f, 4.0f, "Maximum trace distance")

    init {
        EventBus.listen<Render3DEvent> { onRender3D(it) }
    }

    private fun onRender3D(event: Render3DEvent) {
        if (!enabled) return

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val world = mc.level ?: return
        val camera = mc.gameRenderer.mainCamera

        val entities = world.entitiesForRendering()
            .filterIsInstance<LivingEntity>()
            .filter { it != player && it.isAlive }
            .filter { player.distanceToSqr(it) <= range * range }
            .filter { isValidTarget(it) }

        if (entities.isEmpty()) return

        val bufferSource = mc.renderBuffers().bufferSource()
        val consumer = bufferSource.getBuffer(RenderTypes.lines())

        // Build a PoseStack seeded with the modelViewMatrix coming from the render event.
        // This matches exactly what Minecraft's 3D renderer is currently using, so all
        // camera-relative coordinates (including view-bob) are already baked in.
        val poseStack = PoseStack()
        poseStack.last().pose().set(event.modelViewMatrix)
        val pose = poseStack.last()

        val col = color
        val a = 255
        val r = (col shr 16) and 0xFF
        val g = (col shr 8) and 0xFF
        val b = col and 0xFF
        val argbColor = (a shl 24) or (r shl 16) or (g shl 8) or b

        val camX = camera.position().x
        val camY = camera.position().y
        val camZ = camera.position().z

        val pt = event.partialTicks

        for (entity in entities) {
            val ex = (entity.xo + (entity.x - entity.xo) * pt - camX).toFloat()
            val ey = (entity.yo + (entity.y - entity.yo) * pt + entity.bbHeight / 2.0 - camY).toFloat()
            val ez = (entity.zo + (entity.z - entity.zo) * pt - camZ).toFloat()

            // Origin (0,0,0) = camera position in world-space → screen center
            consumer.addVertex(pose, 0f, 0f, 0f)
                .setColor(argbColor)
                .setNormal(pose, 0f, 1f, 0f)
                .setLineWidth(1.5f)

            consumer.addVertex(pose, ex, ey, ez)
                .setColor(argbColor)
                .setNormal(pose, 0f, 1f, 0f)
                .setLineWidth(1.5f)
        }

        bufferSource.endBatch(RenderTypes.lines())
    }

    private fun isValidTarget(entity: LivingEntity): Boolean {
        if (entity is Player) return players
        if (entity is Monster) return monsters
        if (entity is Animal) return passives
        return false
    }
}
