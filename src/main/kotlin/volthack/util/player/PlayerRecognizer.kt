package volthack.util.player

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.monster.Enemy

object PlayerRecognizer {
    private val mc get() = Minecraft.getInstance()
    private val player get() = mc.player

    fun isPlayer(entity: LivingEntity): Boolean = entity is Player
    fun isMonster(entity: LivingEntity): Boolean = entity is Enemy
    fun isAnimal(entity: LivingEntity): Boolean = entity is Animal
    fun isHostile(entity: LivingEntity): Boolean = entity is Enemy
    fun isPassive(entity: LivingEntity): Boolean = entity is Animal

    fun classify(entity: LivingEntity): EntityCategory {
        if (entity is Player) return EntityCategory.PLAYER
        if (entity is Enemy) return EntityCategory.MONSTER
        if (entity is Animal) return EntityCategory.ANIMAL
        return EntityCategory.OTHER
    }

    fun getTargetPriority(entity: LivingEntity): Int = when {
        entity is Player -> 3
        entity is Enemy -> 2
        entity is Animal -> 1
        else -> 0
    }

    fun isTeammate(entity: Player): Boolean {
        val p = player ?: return false
        if (entity == p) return true
        val team = p.team
        return team != null && team == entity.team
    }

    fun isInRange(entity: LivingEntity, range: Double): Boolean {
        val p = player ?: return false
        return p.distanceTo(entity) <= range
    }

    fun getClosestPlayer(range: Double = 64.0): Player? {
        val p = player ?: return null
        val level = mc.level ?: return null
        return level.players()
            .filter { it != p && it.isAlive && p.distanceTo(it) <= range }
            .minByOrNull { p.distanceTo(it) }
    }

    fun getEntitiesInRange(range: Double = 64.0): List<LivingEntity> {
        val p = player ?: return emptyList()
        val level = mc.level ?: return emptyList()
        return level.entitiesForRendering()
            .filterIsInstance<LivingEntity>()
            .filter { it != p && it.isAlive && p.distanceTo(it) <= range }
    }

    fun getPlayersInRange(range: Double = 64.0): List<Player> {
        val p = player ?: return emptyList()
        val level = mc.level ?: return emptyList()
        return level.players()
            .filter { it != p && it.isAlive && p.distanceTo(it) <= range }
    }

    fun countPlayersInRange(range: Double = 64.0): Int = getPlayersInRange(range).size
}

enum class EntityCategory {
    PLAYER,
    MONSTER,
    ANIMAL,
    OTHER
}
