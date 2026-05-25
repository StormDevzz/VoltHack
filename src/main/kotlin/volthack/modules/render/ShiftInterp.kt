package volthack.modules.render

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import volthack.manager.FriendManager
import volthack.setting.Category
import volthack.setting.Module

object ShiftInterp : Module("ShiftInterp", "Visually makes target players appear to be crouching/sneaking", Category.RENDER) {
    private val targetMode by mode("Target", listOf("Everyone", "Self", "Friends"), "Everyone")

    fun shouldCrouch(entity: Player): Boolean {
        val mc = Minecraft.getInstance()
        val local = mc.player ?: return false
        
        return when (targetMode) {
            "Self" -> entity == local
            "Friends" -> entity == local || FriendManager.isFriend(entity.gameProfile.name)
            "Everyone" -> true
            else -> false
        }
    }
}
