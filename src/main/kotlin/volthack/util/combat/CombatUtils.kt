package volthack.util.combat

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.acos
import kotlin.math.sqrt

object CombatUtils {
    private val mc get() = Minecraft.getInstance()
    private val player get() = mc.player
    private val level get() = mc.level

    fun getAttackRange(): Double = 3.0

    fun canAttack(entity: Entity): Boolean {
        val p = player ?: return false
        return entity is LivingEntity && entity.isAlive && entity != p &&
               !entity.isAlliedTo(p) && entity.isAttackable
    }

    fun getTargets(range: Double = getAttackRange(), filter: ((LivingEntity) -> Boolean)? = null): List<LivingEntity> {
        val p = player ?: return emptyList()
        val entities = level?.getEntitiesOfClass(
            LivingEntity::class.java,
            AABB.ofSize(p.position(), range * 2, range * 2, range * 2),
            { it != p && it.isAlive && canAttack(it) }
        ) ?: return emptyList()
        return if (filter != null) entities.filter(filter) else entities
    }

    fun getClosest(range: Double = getAttackRange(), filter: ((LivingEntity) -> Boolean)? = null): LivingEntity? {
        val p = player ?: return null
        return getTargets(range, filter).minByOrNull { p.distanceToSqr(it) }
    }

    fun isInFov(entity: Entity, fov: Float = 60f): Boolean {
        val p = player ?: return false
        val look = p.getLookAngle()
        val toTarget = entity.position().subtract(p.position()).normalize()
        val dot = look.dot(toTarget).coerceIn(-1.0, 1.0)
        val angle = Math.toDegrees(acos(dot))
        return angle <= fov / 2
    }

    fun getDistanceToEntity(entity: Entity): Double {
        val p = player ?: return Double.MAX_VALUE
        return sqrt(p.distanceToSqr(entity))
    }

    fun predictPosition(entity: LivingEntity, ticksAhead: Int = 1): Vec3? {
        val pos = entity.position()
        val motion = entity.deltaMovement
        return pos.add(motion.x * ticksAhead, motion.y * ticksAhead, motion.z * ticksAhead)
    }

    fun isInCrystalRange(entity: LivingEntity, crystalPos: Vec3): Boolean {
        return crystalPos.distanceToSqr(entity.position()) <= 16.0
    }

    fun getEnemyCount(range: Double = 10.0): Int {
        return getTargets(range).count()
    }

    fun getLowestHealthTarget(range: Double = getAttackRange()): LivingEntity? {
        return getTargets(range).minByOrNull { it.health }
    }
}