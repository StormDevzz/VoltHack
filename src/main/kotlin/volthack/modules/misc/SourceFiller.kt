package volthack.modules.misc

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module
import volthack.util.misc.blocks.BlockFluidUtils
import volthack.util.misc.blocks.BlockInteractionUtils
import volthack.util.misc.blocks.BlockSearchUtils
import volthack.util.misc.blocks.BlockUtils

object SourceFiller : Module("SourceFiller", "Drains water sources with sponge", Category.MISC) {
    private val range by int("Range", 5, 2, 10, "Search range for water sources")
    private val autoSwitch by boolean("Auto Switch", true, "Automatically switch to sponge")
    private val silentSwitch by boolean("Silent Switch", false, "Switch without changing visual slot")
    private val delay by int("Delay (ms)", 100, 50, 500, "Delay between placements")
    private val onlySources by boolean("Only Sources", true, "Only drain water source blocks")
    private val placeOnTop by boolean("Place On Top", true, "Place sponge above water")
    private val notify by boolean("Notifications", true, "Show notifications for drained blocks")

    private var lastPlaceTime = 0L
    private var drained = 0
    private var lastDrainedPos: BlockPos? = null

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val world = mc.level ?: return

        if (!BlockInteractionUtils.hasSponge()) return
        if (System.currentTimeMillis() - lastPlaceTime < delay) return

        val waterSources = BlockFluidUtils.getWaterSourcesInRange(player.blockPosition(), range)
            .filter { BlockUtils.isWater(it) }
        if (waterSources.isEmpty()) return

        val target = waterSources.minByOrNull { player.distanceToSqr(it.center) } ?: return

        if (autoSwitch && !silentSwitch) {
            BlockInteractionUtils.selectSpongeInHotbar()
        }

        val placePos = if (placeOnTop) target.above() else target
        if (BlockUtils.isReplaceable(placePos)) {
            BlockInteractionUtils.placeBlock(placePos)
            BlockFluidUtils.drainWater(target)
            lastPlaceTime = System.currentTimeMillis()
            drained++
            lastDrainedPos = target
        }
    }

    override fun onDisable() {
        drained = 0
        lastDrainedPos = null
    }
}
