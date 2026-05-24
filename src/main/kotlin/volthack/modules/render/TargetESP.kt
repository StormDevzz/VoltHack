package volthack.modules.render

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import volthack.event.EventBus
import volthack.event.Render3DEvent
import volthack.hud.HUDManager
import volthack.hud.elements.TargetRenderElement
import volthack.modules.combat.KillAura
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.render.Render3DUtil
import kotlin.math.sin
import kotlin.math.PI

object TargetESP : Module("TargetESP", "Highlights target with a beautiful 3D rotating circle", Category.RENDER) {
    private val espMode by mode("Style", listOf("Ring", "Band", "GlowBand"), "GlowBand")
    private val colorMode by mode("Color Mode", listOf("Static", "Rainbow"), "Static")
    private val customColor by color("Color", 0xFF6C63FF.toInt())
    
    private val radiusOffset by float("Radius Offset", 0.2f, -0.5f, 1.0f, 0.05f)
    private val heightSpeed by float("Height Speed", 2.0f, 0.5f, 5.0f, 0.1f)
    private val spinSpeed by float("Spin Speed", 2.0f, 0.0f, 10.0f, 0.2f)
    
    private val throughWalls by boolean("Through Walls", true)

    init {
        EventBus.listen<Render3DEvent> { onRender3D(it) }
    }

    private fun getTarget(): LivingEntity? {
        if (KillAura.enabled && KillAura.currentTarget != null) {
            return KillAura.currentTarget
        }
        val targetRender = HUDManager.get("TargetRender") as? TargetRenderElement
        if (targetRender != null && targetRender.enabled && targetRender.currentTarget != null) {
            return targetRender.currentTarget
        }
        val mc = Minecraft.getInstance()
        val hit = mc.hitResult
        if (hit != null && hit.type == HitResult.Type.ENTITY) {
            val entity = (hit as EntityHitResult).entity
            if (entity is LivingEntity && entity != mc.player) {
                return entity
            }
        }
        return null
    }

    private fun onRender3D(event: Render3DEvent) {
        if (!enabled) return

        val target = getTarget() ?: return
        val mc = Minecraft.getInstance()
        val camera = mc.gameRenderer.mainCamera

        val partialTicks = event.partialTicks
        val cameraPos = camera.position()
        val renderX = target.xo + (target.x - target.xo) * partialTicks - cameraPos.x
        val renderY = target.yo + (target.y - target.yo) * partialTicks - cameraPos.y
        val renderZ = target.zo + (target.z - target.zo) * partialTicks - cameraPos.z

        val time = System.currentTimeMillis()

        val scaleY = target.bbHeight
        val heightProgress = (sin((time * 0.001 * heightSpeed) * PI) * 0.5 + 0.5)
        val yOffset = heightProgress * scaleY

        val radius = (target.bbWidth / 2.0f) + radiusOffset

        val baseColor = if (colorMode == "Rainbow") {
            val hue = (time * 0.001f) % 1.0f
            java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f)
        } else {
            customColor
        }

        val r = (baseColor shr 16 and 255)
        val g = (baseColor shr 8 and 255)
        val b = (baseColor and 255)
        val alpha = (baseColor shr 24 and 255).let { if (it == 0) 255 else it }

        val spinAngle = (time * 0.001 * spinSpeed)

        when (espMode) {
            "Ring" -> {
                Render3DUtil.drawRing(
                    renderX, renderY + yOffset, renderZ,
                    radius,
                    r, g, b, alpha,
                    spinAngle,
                    throughWalls
                )
            }
            "Band" -> {
                Render3DUtil.drawBand(
                    renderX, renderY + yOffset, renderZ,
                    radius, 0.15f,
                    r, g, b, alpha,
                    spinAngle,
                    throughWalls
                )
            }
            "GlowBand" -> {
                Render3DUtil.drawGlowBand(
                    renderX, renderY + yOffset, renderZ,
                    radius, 0.25f,
                    r, g, b, alpha,
                    spinAngle,
                    throughWalls
                )
            }
        }
    }
}
