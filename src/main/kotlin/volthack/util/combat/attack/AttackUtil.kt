package volthack.util.combat.attack

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

object AttackUtil {
    fun isWeaponCharged(): Boolean {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return false
        return player.getAttackStrengthScale(0.5f) >= 0.95f
    }

    fun getTargetUnderCursor(range: Double): Entity? {
        val mc = Minecraft.getInstance()
        val hit = mc.hitResult ?: return null
        if (hit.type == HitResult.Type.ENTITY) {
            val entityHit = hit as EntityHitResult
            val entity = entityHit.entity
            if (mc.player != null && mc.player!!.distanceTo(entity) <= range) {
                return entity
            }
        }
        return null
    }

    fun isEntityInRange(entity: Entity, range: Float): Boolean {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return false
        return player.distanceTo(entity) <= range
    }
}
