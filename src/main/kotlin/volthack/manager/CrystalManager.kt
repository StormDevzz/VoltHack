package volthack.manager

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.boss.enderdragon.EndCrystal
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import volthack.util.combat.obs.ObsidianUtil

object CrystalManager {
    fun canPlaceCrystal(pos: BlockPos): Boolean {
        val mc = Minecraft.getInstance()
        val world = mc.level ?: return false
        return ObsidianUtil.canPlaceCrystal(pos, world)
    }

    fun placeCrystal(pos: BlockPos, hand: InteractionHand) {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val gameMode = mc.gameMode ?: return

        val hitVec = Vec3(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5)
        val hitResult = BlockHitResult(hitVec, Direction.UP, pos, false)

        gameMode.useItemOn(player, hand, hitResult)
        player.swing(hand)
    }

    fun breakCrystal(crystal: EndCrystal) {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val gameMode = mc.gameMode ?: return

        gameMode.attack(player, crystal)
        player.swing(InteractionHand.MAIN_HAND)
    }
}
