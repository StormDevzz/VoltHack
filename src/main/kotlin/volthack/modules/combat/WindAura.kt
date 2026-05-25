package volthack.modules.combat

import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.manager.RotationManager
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.player.HotbarUtils
import kotlin.math.atan2
import kotlin.math.sqrt

object WindAura : Module("WindAura", "Automatically shoots Wind Charges at targets", Category.COMBAT) {
    private val range by float("Range", 15.0f, 5.0f, 30.0f, 1.0f)
    private val delay by int("Delay (Ticks)", 10, 2, 40, "Ticks between shots")
    private val targetPlayers by boolean("Players", true, "Target other players")
    private val targetMonsters by boolean("Monsters", true, "Target hostile mobs")
    private val targetPassives by boolean("Passives", false, "Target passive mobs")
    private val rotations by boolean("Rotations", true, "Aim rotations at the target")
    private val silentRotations by boolean("Silent Rotations", true, "Silent server-side rotations")

    private var tickCounter = 0

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val level = mc.level ?: return

        tickCounter++
        if (tickCounter < delay) return

        // 1. Locate Wind Charge in hotbar or offhand
        val hasWindChargeOffhand = player.offhandItem.item == Items.WIND_CHARGE
        val windChargeSlot = HotbarUtils.find { it.item == Items.WIND_CHARGE }
        
        if (!hasWindChargeOffhand && windChargeSlot == -1) return

        // 2. Find closest valid target in range
        val target = (level.entitiesForRendering() as Iterable<Entity>)
            .filterIsInstance<LivingEntity>()
            .filter { it != player && !it.isDeadOrDying }
            .filter { player.distanceTo(it) <= range }
            .filter { isValidTarget(it) }
            .minByOrNull { player.distanceTo(it) } ?: return

        // 3. Aim Rotations
        val diffX = target.x - player.x
        val diffY = (target.eyeY - 0.2) - (player.y + player.eyeHeight)
        val diffZ = target.z - player.z
        val dist = sqrt(diffX * diffX + diffZ * diffZ)
        
        val targetYaw = (Math.toDegrees(atan2(diffZ, diffX)) - 90.0).toFloat()
        val targetPitch = (-Math.toDegrees(atan2(diffY, dist))).toFloat()

        if (rotations) {
            RotationManager.setRotations(targetYaw, targetPitch, silentRotations)
        }

        // 4. Perform Right-Click (Use Wind Charge)
        val performShoot = {
            if (hasWindChargeOffhand) {
                mc.gameMode?.useItem(player, InteractionHand.OFF_HAND)
                player.swing(InteractionHand.OFF_HAND)
            } else {
                val oldSlot = HotbarUtils.selectedSlot
                HotbarUtils.select(windChargeSlot)
                mc.gameMode?.useItem(player, InteractionHand.MAIN_HAND)
                player.swing(InteractionHand.MAIN_HAND)
                HotbarUtils.select(oldSlot)
            }
            tickCounter = 0
        }

        if (rotations) {
            RotationManager.runRotation {
                performShoot()
            }
        } else {
            performShoot()
        }
    }

    private fun isValidTarget(entity: LivingEntity): Boolean {
        if (entity is Player && targetPlayers) return true
        if (entity is Enemy && targetMonsters) return true
        if (entity is Animal && targetPassives) return true
        return false
    }

    override fun onDisable() {
        tickCounter = 0
        RotationManager.reset()
    }
}
