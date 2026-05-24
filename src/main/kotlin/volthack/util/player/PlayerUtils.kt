package volthack.util.player

import net.minecraft.client.Minecraft
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3

object PlayerUtils {
    private val mc get() = Minecraft.getInstance()
    private val player get() = mc.player

    val x: Double get() = player?.x ?: 0.0
    val y: Double get() = player?.y ?: 0.0
    val z: Double get() = player?.z ?: 0.0
    val pos: Vec3? get() = player?.position()
    val blockPos: BlockPos? get() = player?.blockPosition()
    val health: Float get() = player?.health ?: 0f
    val maxHealth: Float get() = player?.maxHealth ?: 0f
    val hunger: Int get() = player?.foodData?.foodLevel ?: 0
    val air: Int get() = player?.airSupply ?: 0
    val isInWater: Boolean get() = player?.isInWater ?: false
    val onGround: Boolean get() = player?.onGround() ?: false
    val isSprinting: Boolean get() = player?.isSprinting ?: false
    val isSneaking: Boolean get() = player?.isShiftKeyDown() ?: false
    val isAlive: Boolean get() = player?.isAlive ?: false

    fun distanceTo(e: Entity): Double {
        val p = player ?: return Double.MAX_VALUE
        return kotlin.math.sqrt(p.distanceToSqr(e))
    }

    fun distanceTo(x: Double, y: Double, z: Double): Double {
        val p = player ?: return Double.MAX_VALUE
        val dx = p.x - x; val dy = p.y - y; val dz = p.z - z
        return kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun lookAt(x: Double, y: Double, z: Double) {
        player?.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3(x, y, z))
    }

    fun hasEntityInSight(range: Double = 4.5): LivingEntity? {
        val p = player ?: return null
        val level = mc.level ?: return null
        val entities = level.getEntitiesOfClass(
            LivingEntity::class.java,
            p.getBoundingBox().inflate(range),
            { it != p && it.isAlive }
        )
        return entities.minByOrNull { distanceTo(it) }
    }

    fun swingHand() {
        player?.swing(InteractionHand.MAIN_HAND)
    }

    fun attack(entity: Entity) {
        val p = player ?: return
        mc.gameMode?.attack(p, entity)
        swingHand()
    }

    fun interact(entity: Entity) {
        val p = player ?: return
        mc.gameMode?.interact(p, entity, InteractionHand.MAIN_HAND)
    }

    fun jump() {
        player?.jumpFromGround()
    }

    val isMoving: Boolean get() {
        val p = player ?: return false
        return p.xxa != 0f || p.zza != 0f
    }

    val speed: Double get() {
        val p = player ?: return 0.0
        val dx = p.x - p.xo; val dz = p.z - p.zo
        return kotlin.math.sqrt(dx * dx + dz * dz)
    }
}