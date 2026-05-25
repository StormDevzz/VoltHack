package volthack.modules.movement

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.BlockItem
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.player.HotbarUtils
import kotlin.math.floor

object Scaffold : Module("Scaffold", "Automatically places blocks beneath your feet", Category.MOVEMENT) {
    private val delay by int("Delay (Ticks)", 1, 0, 5, "Ticks between placements")
    private val instant by boolean("Instant", true, "Place blocks instantly")
    private val keepY by boolean("Keep Y", false, "Keep bridge at the same Y level")
    private val swing by boolean("Swing Hand", true, "Swing hand when placing blocks")
    private val downwards by boolean("Downwards", false, "Place downwards when sneaking")
    private val expand by int("Expand", 1, 0, 4, "Number of blocks to place ahead")

    private var tickTimer = 0
    private var startY = -1.0

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    override fun onEnable() {
        val player = Minecraft.getInstance().player ?: return
        startY = player.y
        tickTimer = 0
    }

    override fun onDisable() {
        tickTimer = 0
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val level = mc.level ?: return

        if (!instant) {
            tickTimer++
            if (tickTimer < delay) return
        }

        // Determine base Y position
        val yPos = if (keepY && startY != -1.0) {
            floor(startY - 0.5).toInt()
        } else {
            floor(player.y - 0.5).toInt()
        }

        val yaw = Math.toRadians(player.yRot.toDouble())
        val forwardX = -Math.sin(yaw)
        val forwardZ = Math.cos(yaw)

        // Loop over the expand range to place multiple blocks if needed
        for (i in 0..expand) {
            var targetPos = BlockPos(
                floor(player.x + forwardX * i).toInt(),
                yPos,
                floor(player.z + forwardZ * i).toInt()
            )

            // If downwards is enabled and player is sneaking, place below the target Y
            if (downwards && player.isCrouching) {
                targetPos = targetPos.below()
            }

            if (!level.getBlockState(targetPos).isAir) continue

            // Find neighbors to place against
            if (placeBlockAt(targetPos)) {
                if (!instant) {
                    tickTimer = 0
                    return
                }
            }
        }
    }

    private fun placeBlockAt(pos: BlockPos): Boolean {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return false
        val level = mc.level ?: return false

        for (dir in Direction.entries) {
            val neighbor = pos.relative(dir)
            val state = level.getBlockState(neighbor)
            if (!state.isAir && state.fluidState.isEmpty) {
                val blockSlot = HotbarUtils.find { it.item is BlockItem }
                if (blockSlot != -1) {
                    val oldSlot = HotbarUtils.selectedSlot
                    val hitResult = BlockHitResult(
                        Vec3.atCenterOf(neighbor).add(
                            Vec3(
                                dir.opposite.stepX * 0.5,
                                dir.opposite.stepY * 0.5,
                                dir.opposite.stepZ * 0.5
                            )
                        ),
                        dir.opposite,
                        neighbor,
                        false
                    )

                    HotbarUtils.select(blockSlot)
                    mc.gameMode?.useItemOn(player, InteractionHand.MAIN_HAND, hitResult)
                    if (swing) {
                        player.swing(InteractionHand.MAIN_HAND)
                    }
                    HotbarUtils.select(oldSlot)
                    return true
                }
            }
        }
        return false
    }
}
