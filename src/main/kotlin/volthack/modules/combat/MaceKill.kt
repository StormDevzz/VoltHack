package volthack.modules.combat

import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Items
import volthack.event.EventBus
import volthack.event.TickEvent
import volthack.setting.Category
import volthack.setting.Module

object MaceKill : Module("MaceKill", "Spoofs fall distance using packets to one-shot targets with a Mace", Category.COMBAT) {
    private val range by float("Range", 4.5f, 2.0f, 6.0f, 0.1f)
    private val height by float("Spoof Height", 15.0f, 5.0f, 40.0f, 1.0f)

    init {
        EventBus.listen<TickEvent> { onTick() }
    }

    private fun onTick() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val level = mc.level ?: return

        if (player.mainHandItem.item != Items.MACE) return
        if (player.getAttackStrengthScale(0.5f) < 0.9f) return

        val target = (level.entitiesForRendering() as Iterable<Entity>)
            .filterIsInstance<LivingEntity>()
            .filter { it != player && !it.isDeadOrDying }
            .filter { player.distanceTo(it) <= range }
            .minByOrNull { player.distanceTo(it) } ?: return

        val conn = player.connection ?: return

        // 1. Spoof going high up in the air
        conn.send(ServerboundMovePlayerPacket.Pos(player.x, player.y + height, player.z, false, false))
        
        // 2. Spoof falling back down
        conn.send(ServerboundMovePlayerPacket.Pos(player.x, player.y, player.z, false, false))

        // 3. Perform attack
        mc.gameMode?.attack(player, target)
        player.swing(InteractionHand.MAIN_HAND)
    }
}
