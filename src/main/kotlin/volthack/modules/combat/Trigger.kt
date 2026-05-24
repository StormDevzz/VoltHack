package volthack.modules.combat

import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.entity.animal.Animal
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.combat.attack.AttackUtil

object Trigger : Module("Trigger", "Attacks target under crosshair", Category.COMBAT) {
    private val range by float("Range", 4.0f, 1.0f, 6.0f, 0.1f)
    private val cooldown by boolean("Smart Cooldown", true, "Attacks only when weapon is fully charged")
    private val targetPlayers by boolean("Players", true)
    private val targetMonsters by boolean("Monsters", true)
    private val targetPassives by boolean("Passives", false)

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    var currentTarget: LivingEntity? = null

    private fun onTick() {
        currentTarget = null
        if (!enabled) return

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        val target = AttackUtil.getTargetUnderCursor(range.toDouble()) ?: return
        if (target !is LivingEntity || target.isDeadOrDying) return

        if (!isValidTarget(target)) return
        currentTarget = target

        if (cooldown && !AttackUtil.isWeaponCharged()) return

        mc.gameMode?.attack(player, target)
        player.swing(InteractionHand.MAIN_HAND)
    }

    private fun isValidTarget(entity: LivingEntity): Boolean {
        if (entity is Player && targetPlayers) return true
        if (entity is Enemy && targetMonsters) return true
        if (entity is Animal && targetPassives) return true
        return false
    }
}
