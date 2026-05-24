package volthack.modules.combat

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.item.Items
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.manager.CrystalManager
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.combat.crystal.CrystalUtil

object AutoCrystal : Module("AutoCrystal", "Place/break End Crystals", Category.COMBAT) {
    private val range by float("Range", 4.5f, 1.0f, 6.0f, 0.1f)
    private val breakDelay by int("Break Delay (ms)", 50, 0, 500)
    private val placeDelay by int("Place Delay (ms)", 50, 0, 500)

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private var lastBreakTime = 0L
    private var lastPlaceTime = 0L

    private fun onTick() {
        if (!enabled) return

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val world = mc.level ?: return

        // 1. Locate targets (other players or enemies)
        val target = world.entitiesForRendering()
            .filterIsInstance<LivingEntity>()
            .filter { it != player && !it.isDeadOrDying }
            .filter { it is Player || it is Enemy }
            .minByOrNull { player.distanceTo(it) } ?: return

        if (player.distanceTo(target) > 10f) return

        // 2. Break phase: search for crystals near the target / in range
        val crystals = CrystalUtil.findCrystalsInRange(range)
        if (crystals.isNotEmpty()) {
            val now = System.currentTimeMillis()
            if (now - lastBreakTime >= breakDelay) {
                val crystal = crystals.minByOrNull { it.distanceTo(target) }
                if (crystal != null) {
                    CrystalManager.breakCrystal(crystal)
                    lastBreakTime = now
                    return // prioritize breaking to avoid entity limits
                }
            }
        }

        // 3. Place phase: find best obsidian position in range
        val mainHand = player.mainHandItem.item == Items.END_CRYSTAL
        val offHand = player.offhandItem.item == Items.END_CRYSTAL
        if (!mainHand && !offHand) return

        val hand = if (offHand) InteractionHand.OFF_HAND else InteractionHand.MAIN_HAND

        val now = System.currentTimeMillis()
        if (now - lastPlaceTime >= placeDelay) {
            val r = range.toInt() + 1
            var bestPos: BlockPos? = null
            var bestDist = Double.MAX_VALUE

            val playerPos = player.blockPosition()
            for (dx in -r..r) {
                for (dy in -r..r) {
                    for (dz in -r..r) {
                        val pos = playerPos.offset(dx, dy, dz)
                        if (CrystalUtil.isWithinPlaceRange(pos, range) && CrystalManager.canPlaceCrystal(pos)) {
                            // Find pos closest to the target
                            val dist = pos.distToCenterSqr(target.x, target.y, target.z)
                            if (dist < bestDist) {
                                bestDist = dist
                                bestPos = pos
                            }
                        }
                    }
                }
            }

            if (bestPos != null) {
                CrystalManager.placeCrystal(bestPos, hand)
                lastPlaceTime = now
            }
        }
    }
}
