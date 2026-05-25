package volthack.modules.world

import net.minecraft.client.Minecraft
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.world.highway.*

object HighwayMake : Module("HighwayMake", "Builds automated Obsidian Highways (platforms and walls) and mines obstacles in front of you", Category.WORLD) {
    private val buildWallsSetting by boolean("Build Walls", true, "Build safety walls on the side of the highway")
    private val mineObstacles by boolean("Clear Obstacles", true, "Mine blocks in your way while building")
    private val autoWalkSetting by boolean("Auto Walk", true, "Automatically walks forward while building the highway")

    private var placingDelay = 0

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val world = mc.level ?: return

        // 1. Sync settings to modular configuration
        HighwayConfig.buildWalls = buildWallsSetting
        HighwayConfig.autoWalk = autoWalkSetting

        // 2. Find building blocks
        val buildSlot = HighwayInventory.findBuildingBlockSlot()
        if (buildSlot == -1) {
            HighwayMovement.stop()
            return
        }

        val playerPos = player.blockPosition()
        val dir = player.direction

        // 3. Clear tunnel obstructions first
        if (mineObstacles) {
            // Check tunnel blocks directly ahead (distance 1 and 2)
            val tunnelBlocks = HighwayBlockPos.getMineBlocks(playerPos, dir, 1) + 
                               HighwayBlockPos.getMineBlocks(playerPos, dir, 2)
            
            for (pos in tunnelBlocks) {
                val state = world.getBlockState(pos)
                if (!state.isAir && !state.canBeReplaced()) {
                    HighwayMovement.stop()
                    HighwayMiner.mine(pos)
                    return // Prioritize mining over building
                }
            }
        }

        // 4. Build floor / walls (distance 0 and 1)
        val buildBlocks = HighwayBlockPos.getBuildBlocks(playerPos, dir, 0) + 
                          HighwayBlockPos.getBuildBlocks(playerPos, dir, 1)

        var placed = false
        if (placingDelay > 0) {
            placingDelay--
        } else {
            for (pos in buildBlocks) {
                val state = world.getBlockState(pos)
                if (state.isAir || state.canBeReplaced()) {
                    HighwayBuilder.build(pos, buildSlot)
                    placed = true
                    placingDelay = 2 // 2 ticks cooldown to prevent speed kicks
                    break
                }
            }
        }

        // 5. Automate movement along finished platform
        if (autoWalkSetting) {
            // Only walk forward if the floor beneath us (distance 0) and directly ahead (distance 1) is complete
            val immediateFloor = HighwayBlockPos.getBuildBlocks(playerPos, dir, 0)
                .filter { it.y < playerPos.y } // Floor blocks only
            
            val complete = immediateFloor.all { 
                val state = world.getBlockState(it)
                !state.isAir && !state.canBeReplaced()
            }

            if (complete && !placed) {
                HighwayMovement.autoWalk(dir)
            } else {
                HighwayMovement.stop()
            }
        }
    }

    override fun onDisable() {
        HighwayMovement.stop()
        placingDelay = 0
    }
}
