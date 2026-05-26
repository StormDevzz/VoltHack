package volthack.modules.player

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.ai.attributes.Attributes
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module

object Reach : Module("Reach", "Increases your block break/build and entity attack interaction ranges", Category.PLAYER) {
    val blockReach by float("Block Reach", 6.0f, 3.0f, 10.0f, 0.1f)
    val entityReach by float("Entity Reach", 5.0f, 3.0f, 10.0f, 0.1f)

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val player = Minecraft.getInstance().player ?: return
        
        player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE)?.baseValue = blockReach.toDouble()
        player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE)?.baseValue = entityReach.toDouble()
    }

    override fun onDisable() {
        val player = Minecraft.getInstance().player ?: return
        // Reset to default survival values
        player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE)?.baseValue = 4.5
        player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE)?.baseValue = 3.0
    }
}
