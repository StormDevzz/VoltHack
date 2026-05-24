package volthack.modules.misc

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket
import net.minecraft.world.level.block.entity.SignBlockEntity
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module

object AutoSign : Module("AutoSign", "Automatically writes text on signs when placed", Category.MISC) {
    private val line1 by text("Line 1", "VoltHack")
    private val line2 by text("Line 2", "")
    private val line3 by text("Line 3", "")
    private val line4 by text("Line 4", "")
    private val autoClose by boolean("Auto Close", true, "Close sign editor automatically")

    private var lastSignPos: BlockPos? = null
    private var signTicks = 0

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val world = mc.level ?: return

        val screen = mc.screen ?: run {
            lastSignPos = null
            signTicks = 0
            return
        }

        val className = screen.javaClass.name.lowercase()

        if (className.contains("sign")) {
            val lines = arrayOf(line1, line2, line3, line4)
            val signPos = findSignInFront()
            if (signPos != null) {
                if (lastSignPos != signPos) {
                    lastSignPos = signPos
                    signTicks = 0
                }
                signTicks++
                sendSignUpdate(signPos, lines)
                if (autoClose && signTicks > 2) {
                    mc.setScreen(null)
                }
            }
        } else {
            lastSignPos = null
            signTicks = 0
        }
    }

    private fun findSignInFront(): BlockPos? {
        val mc = Minecraft.getInstance()
        val hit = mc.hitResult
        if (hit != null && hit.type == HitResult.Type.BLOCK) {
            val blockHit = hit as BlockHitResult
            val pos = blockHit.blockPos
            val state = mc.level?.getBlockState(pos)
            if (state != null && (state.block == Blocks.OAK_SIGN || state.block == Blocks.OAK_WALL_SIGN ||
                    state.block == Blocks.SPRUCE_SIGN || state.block == Blocks.SPRUCE_WALL_SIGN ||
                    state.block == Blocks.BIRCH_SIGN || state.block == Blocks.BIRCH_WALL_SIGN ||
                    state.block == Blocks.JUNGLE_SIGN || state.block == Blocks.JUNGLE_WALL_SIGN ||
                    state.block == Blocks.ACACIA_SIGN || state.block == Blocks.ACACIA_WALL_SIGN ||
                    state.block == Blocks.DARK_OAK_SIGN || state.block == Blocks.DARK_OAK_WALL_SIGN ||
                    state.block == Blocks.MANGROVE_SIGN || state.block == Blocks.MANGROVE_WALL_SIGN ||
                    state.block == Blocks.CHERRY_SIGN || state.block == Blocks.CHERRY_WALL_SIGN ||
                    state.block == Blocks.BAMBOO_SIGN || state.block == Blocks.BAMBOO_WALL_SIGN ||
                    state.block == Blocks.CRIMSON_SIGN || state.block == Blocks.CRIMSON_WALL_SIGN ||
                    state.block == Blocks.WARPED_SIGN || state.block == Blocks.WARPED_WALL_SIGN)) {
                return pos
            }
        }
        return null
    }

    private fun sendSignUpdate(pos: BlockPos, lines: Array<String>) {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val blockEntity = mc.level?.getBlockEntity(pos)
        if (blockEntity is SignBlockEntity) {
            val updatedLines = if (blockEntity.isWaxed) {
                arrayOf("", "", "", "")
            } else lines.map { it.ifEmpty { " " } }.toTypedArray()
            player.connection?.send(ServerboundSignUpdatePacket(pos, true, updatedLines[0], updatedLines[1], updatedLines[2], updatedLines[3]))
        }
    }

    override fun onDisable() {
        lastSignPos = null
        signTicks = 0
    }
}
