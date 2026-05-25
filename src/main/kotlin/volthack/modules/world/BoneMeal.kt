package volthack.modules.world

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.BonemealableBlock
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module
import kotlin.math.floor

object BoneMeal : Module("BoneMeal", "Automatically fertilizes nearby crops using Bone Meal", Category.WORLD) {
    private val range by float("Range", 4.5f, 2.0f, 6.0f, 0.1f)
    private val delay by int("Delay (Ticks)", 2, 0, 10, "Delay between actions")

    private var timer = 0

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val level = mc.level ?: return

        timer++
        if (timer < delay) return

        val mainHand = player.mainHandItem.item == Items.BONE_MEAL
        val offHand = player.offhandItem.item == Items.BONE_MEAL
        if (!mainHand && !offHand) return

        val hand = if (mainHand) InteractionHand.MAIN_HAND else InteractionHand.OFF_HAND

        val px = floor(player.x).toInt()
        val py = floor(player.y).toInt()
        val pz = floor(player.z).toInt()
        val r = range.toInt() + 1

        for (x in -r..r) {
            for (y in -r..r) {
                for (z in -r..r) {
                    val pos = BlockPos(px + x, py + y, pz + z)
                    if (player.distanceToSqr(Vec3.atCenterOf(pos)) > range * range) continue

                    val state = level.getBlockState(pos)
                    val block = state.block
                    if (block is BonemealableBlock && block.isValidBonemealTarget(level, pos, state)) {
                        val hit = BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)
                        mc.gameMode?.useItemOn(player, hand, hit)
                        player.swing(hand)
                        timer = 0
                        return
                    }
                }
            }
        }
    }

    override fun onDisable() {
        timer = 0
    }
}
