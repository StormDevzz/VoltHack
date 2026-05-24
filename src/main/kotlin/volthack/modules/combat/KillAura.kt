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
import volthack.manager.RotationManager
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.combat.attack.AttackUtil
import kotlin.math.atan2
import kotlin.math.sqrt

object KillAura : Module("KillAura", "Attacks entities around you automatically", Category.COMBAT) {
    private val range by float("Range", 4.2f, 1f, 6f, 0.1f)
    private val mode by mode("Mode", listOf("Single", "Multi"), "Single")
    private val cooldown by boolean("Smart Cooldown", true, "Attacks only when weapon is fully charged")
    
    // Rotations settings
    private val rotations by boolean("Rotations", true, "Enable rotations toward targets")
    private val rotVis by mode("Rotation Vis", listOf("Client", "Server"), "Client")

    private val targetPlayers by boolean("Players", true, "Target other players")
    private val targetMonsters by boolean("Monsters", true, "Target hostile mobs")
    private val targetPassives by boolean("Passives", false, "Target passive mobs")

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private var currentTarget: LivingEntity? = null

    private fun onTick() {
        if (!enabled) return

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val world = mc.level ?: return

        @Suppress("UNCHECKED_CAST")
        val targets = (world.entitiesForRendering() as Iterable<Entity>)
            .filterIsInstance<LivingEntity>()
            .filter { it != player && !it.isDeadOrDying }
            .filter { AttackUtil.isEntityInRange(it, range) }
            .filter { isValidTarget(it) }
            .sortedBy { player.distanceTo(it) }

        if (targets.isEmpty()) {
            currentTarget = null
            RotationManager.reset()
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

        if (rotations) {
            val isSilent = rotVis == "Server"
            RotationManager.setRotations(targetYaw, targetPitch, isSilent)
        } else {
            RotationManager.reset()
        }

        // Cooldown check to ensure maximum damage on every hit
        if (cooldown && !AttackUtil.isWeaponCharged()) {
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
        if (rotations) {
            RotationManager.runRotation {
                mc.gameMode?.attack(player, target)
                player.swing(InteractionHand.MAIN_HAND)
            }
        } else {
            mc.gameMode?.attack(player, target)
            player.swing(InteractionHand.MAIN_HAND)
        }
    }
}
