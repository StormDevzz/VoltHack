package volthack.modules.world

import net.minecraft.client.Minecraft
import volthack.event.EventBus
import volthack.event.Render3DEvent
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.render.Render3DUtil
import volthack.util.world.highway.*

object HighwayMake : Module("HighwayMake", "Builds automated Obsidian Highways (platforms and walls) and mines obstacles in front of you", Category.WORLD) {
    private val buildWallsSetting by boolean("Build Walls", true, "Build safety walls on the side of the highway")
    private val mineObstacles by boolean("Clear Obstacles", true, "Mine blocks in your way while building")
    private val autoWalkSetting by boolean("Auto Walk", true, "Automatically walks forward while building the highway")
    private val materialPriority by mode("Material Priority", listOf("Obsidian", "Netherrack", "Cobblestone", "Any"), "Obsidian", "Block material prioritized for construction")
    private val renderBlueprint by boolean("Render Blueprint", true, "Show visual 3D preview outline of the highway construction blueprint")
    private val renderColor by color("Render Color", 0xFF00E6FF.toInt(), "The color of the highway schematic outlines")

    private var placingDelay = 0

    init {
        EventBus.listen<TickEvent> { onTick() }
        EventBus.listen<Render3DEvent> { onRender3D(it) }
    }

    private fun onTick() {
        if (!enabled) return

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val world = mc.level ?: return

        // 1. Sync settings to modular configuration
        HighwayConfig.buildWalls = buildWallsSetting
        HighwayConfig.autoWalk = autoWalkSetting

        // 2. Find building blocks with material priority preference
        val buildSlot = HighwayInventory.findBuildingBlockSlot(materialPriority)
        if (buildSlot == -1) {
            HighwayMovement.stop()
            return
        }

        val playerPos = player.blockPosition()
        val dir = player.direction

        // 3. Clear tunnel obstructions first
        if (mineObstacles) {
            val tunnelBlocks = HighwayBlockPos.getMineBlocks(playerPos, dir, 1) + 
                               HighwayBlockPos.getMineBlocks(playerPos, dir, 2)
            
            for (pos in tunnelBlocks) {
                val state = world.getBlockState(pos)
                if (!state.isAir && !state.canBeReplaced()) {
                    HighwayMovement.stop()
                    HighwayMiner.mine(pos)
                    return
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
                    placingDelay = 2
                    break
                }
            }
        }

        // 5. Automate movement along finished platform
        if (autoWalkSetting) {
            val immediateFloor = HighwayBlockPos.getBuildBlocks(playerPos, dir, 0)
                .filter { it.y < playerPos.y }
            
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

    private fun onRender3D(event: Render3DEvent) {
        if (!enabled || !renderBlueprint) return

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        
        val playerPos = player.blockPosition()
        val dir = player.direction

        val buildBlocks = HighwayBlockPos.getBuildBlocks(playerPos, dir, 0) + 
                          HighwayBlockPos.getBuildBlocks(playerPos, dir, 1)

        val col = renderColor
        val r = (col shr 16) and 0xFF
        val g = (col shr 8) and 0xFF
        val b = col and 0xFF
        val a = (col shr 24) and 0xFF
        val finalAlpha = if (a == 0) 180 else a

        for (pos in buildBlocks) {
            val state = mc.level?.getBlockState(pos) ?: continue
            // Render outline for target positions that are still empty/replaceable
            if (state.isAir || state.canBeReplaced()) {
                Render3DUtil.drawBlockOutline(
                    event.modelViewMatrix,
                    pos.x.toDouble() + 0.5,
                    pos.y.toDouble(),
                    pos.z.toDouble() + 0.5,
                    1.0f, 1.0f,
                    r, g, b, finalAlpha,
                    true,
                    1.5f
                )
            }
        }
    }

    override fun onDisable() {
        HighwayMovement.stop()
        placingDelay = 0
    }
}
