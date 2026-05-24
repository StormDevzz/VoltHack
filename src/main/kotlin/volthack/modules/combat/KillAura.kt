package volthack.modules.combat

import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.entity.animal.Animal
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module
import kotlin.math.atan2
import kotlin.math.sqrt

object KillAura : Module("KillAura", "Attacks entities around you automatically", Category.COMBAT) {
    private val range by float("Range", 4.2f, 1f, 6f, 0.1f)
    private val mode by mode("Mode", listOf("Single", "Multi"), "Single")
    private val cooldown by boolean("Smart Cooldown", true, "Attacks only when weapon is fully charged")
    
    // Rotations settings
    private val rotations by boolean("Rotations", true, "Enable rotations toward targets")
    private val rotMode by mode("Rotation Mode", listOf("Snap", "Track", "Alternative"), "Track")
    private val rotVis by mode("Rotation Vis", listOf("Client", "Server"), "Client")

    private val targetPlayers by boolean("Players", true, "Target other players")
    private val targetMonsters by boolean("Monsters", true, "Target hostile mobs")
    private val targetPassives by boolean("Passives", false, "Target passive mobs")

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private var currentTarget: LivingEntity? = null

    // Helper to wrap degrees to range [-180, 180) to prevent 360-degree rotation glitches
    private fun wrapDegrees(value: Float): Float {
        var deg = value % 360f
        if (deg >= 180f) deg -= 360f
        if (deg < -180f) deg += 360f
        return deg
    }

    private fun onTick() {
        if (!enabled) return

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val world = mc.level ?: return

        @Suppress("UNCHECKED_CAST")
        val targets = (world.entitiesForRendering() as Iterable<Entity>)
            .filterIsInstance<LivingEntity>()
            .filter { it != player && !it.isDeadOrDying }
            .filter { player.distanceTo(it) <= range }
            .filter { isValidTarget(it) }
            .sortedBy { player.distanceTo(it) }

        if (targets.isEmpty()) {
            currentTarget = null
            return
        }

        val primaryTarget = targets.first()
        currentTarget = primaryTarget

        // Calculate rotations to primary target
        val diffX = primaryTarget.x - player.x
        val diffY = (primaryTarget.eyeY - 0.4) - (player.y + player.eyeHeight)
        val diffZ = primaryTarget.z - player.z
        val dist = sqrt(diffX * diffX + diffZ * diffZ)
        
        val targetYaw = (Math.toDegrees(atan2(diffZ, diffX)) - 90.0).toFloat()
        val targetPitch = (-Math.toDegrees(atan2(diffY, dist))).toFloat()

        // Handle continuous rotations (Track, Alternative) on tick
        if (rotations) {
            when (rotMode) {
                "Track" -> {
                    if (rotVis == "Client") {
                        player.yRot = targetYaw
                        player.yRotO = targetYaw
                        player.xRot = targetPitch
                        player.xRotO = targetPitch
                    }
                }
                "Alternative" -> {
                    val speed = 0.25f
                    val deltaYaw = wrapDegrees(targetYaw - player.yRot)
                    val deltaPitch = wrapDegrees(targetPitch - player.xRot)
                    if (rotVis == "Client") {
                        player.yRot += deltaYaw * speed
                        player.xRot += deltaPitch * speed
                    }
                }
            }
        }

        // Cooldown check to ensure maximum damage on every hit
        if (cooldown && player.getAttackStrengthScale(0.5f) < 0.95f) {
            return
        }

        if (mode == "Multi") {
            targets.forEach {
                attack(player, it)
            }
        } else {
            attack(player, primaryTarget)
        }
    }

    private fun isValidTarget(entity: LivingEntity): Boolean {
        if (entity is Player && targetPlayers) return true
        if (entity is Enemy && targetMonsters) return true
        if (entity is Animal && targetPassives) return true
        return false
    }

    private fun attack(player: Player, target: Entity) {
        val mc = Minecraft.getInstance()
        
        var appliedYaw = player.yRot
        var appliedPitch = player.xRot
        
        if (rotations) {
            val diffX = target.x - player.x
            val diffY = (target.eyeY - 0.4) - (player.y + player.eyeHeight)
            val diffZ = target.z - player.z
            val dist = sqrt(diffX * diffX + diffZ * diffZ)
            val targetYaw = (Math.toDegrees(atan2(diffZ, diffX)) - 90.0).toFloat()
            val targetPitch = (-Math.toDegrees(atan2(diffY, dist))).toFloat()
            
            when (rotMode) {
                "Snap" -> {
                    appliedYaw = targetYaw
                    appliedPitch = targetPitch
                }
                "Track" -> {
                    appliedYaw = targetYaw
                    appliedPitch = targetPitch
                }
                "Alternative" -> {
                    val speed = 0.25f
                    val deltaYaw = wrapDegrees(targetYaw - player.yRot)
                    val deltaPitch = wrapDegrees(targetPitch - player.xRot)
                    appliedYaw = player.yRot + deltaYaw * speed
                    appliedPitch = player.xRot + deltaPitch * speed
                }
            }
        }

        val prevYaw = player.yRot
        val prevPitch = player.xRot

        // Apply visual or silent rotations right before calling attack
        if (rotations) {
            player.yRot = appliedYaw
            player.xRot = appliedPitch
            if (rotVis == "Client") {
                player.yRotO = appliedYaw
                player.xRotO = appliedPitch
            }
        }

        mc.gameMode?.attack(player, target)
        player.swing(InteractionHand.MAIN_HAND)

        // If silent rotation (Server), restore client-side view instantly
        if (rotations && rotVis == "Server") {
            player.yRot = prevYaw
            player.xRot = prevPitch
        }
    }
}
