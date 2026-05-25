package volthack.modules.movement

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module

object Jesus : Module("Jesus", "Allows walking on water", Category.MOVEMENT) {
    private val mode by mode("Mode", listOf("Solid", "Bounce"), "Solid")
    private val speed by float("Speed", 1.0f, 0.5f, 2.0f, 0.1f)
    private val floatSpeed by float("Float Speed", 0.15f, 0.05f, 0.5f, 0.05f)

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val level = mc.level ?: return

        val inWater = player.isInWater
        val onWaterSurface = isPlayerOnWaterSurface()

        if (mode == "Solid") {
            if (inWater) {
                // Float up to the surface
                player.setDeltaMovement(player.deltaMovement.x, floatSpeed.toDouble(), player.deltaMovement.z)
            } else if (onWaterSurface && !player.isShiftKeyDown) {
                // Stand on top of the water!
                val motion = player.deltaMovement
                val vx = motion.x * speed
                val vz = motion.z * speed
                
                // If player jumps, let them jump up
                val vy = if (mc.options.keyJump.isDown) 0.42 else -0.005
                player.setDeltaMovement(vx, vy, vz)
                
                // Set onGround to true to allow sprinting and jumping
                player.setOnGround(true)
            }
        } else if (mode == "Bounce") {
            if (inWater || onWaterSurface) {
                if (!player.isShiftKeyDown) {
                    player.setDeltaMovement(player.deltaMovement.x, 0.35, player.deltaMovement.z)
                }
            }
        }
    }

    private fun isPlayerOnWaterSurface(): Boolean {
        val player = Minecraft.getInstance().player ?: return false
        val level = Minecraft.getInstance().level ?: return false
        
        // Check blocks slightly below the player's bounding box
        val bb = player.boundingBox
        val y = player.y - 0.05
        val minX = net.minecraft.util.Mth.floor(bb.minX)
        val maxX = net.minecraft.util.Mth.floor(bb.maxX)
        val minZ = net.minecraft.util.Mth.floor(bb.minZ)
        val maxZ = net.minecraft.util.Mth.floor(bb.maxZ)
        
        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                val pos = BlockPos(x, net.minecraft.util.Mth.floor(y), z)
                val state = level.getBlockState(pos)
                if (state.block == Blocks.WATER) {
                    return true
                }
            }
        }
        return false
    }
}
