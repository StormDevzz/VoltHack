package volthack.modules.render

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import volthack.manager.FriendManager
import volthack.setting.Category
import volthack.setting.Module

object SmallUser : Module("SmallUser", "Visually scales down the player model size", Category.RENDER) {
    private val targetMode by mode("Target", listOf("Everyone", "Self", "Friends"), "Self")
    val scale by float("Scale", 0.5f, 0.1f, 1.0f, 0.05f)
    val forceBaby by boolean("Force Baby", false)

    fun getScaleFactor(): Float = scale

    fun shouldScale(state: LivingEntityRenderState): Boolean {
        if (state !is AvatarRenderState) return false
        val mc = Minecraft.getInstance()
        val level = mc.level ?: return false
        val entity = level.getEntity(state.id) ?: return false
        if (entity !is net.minecraft.world.entity.player.Player) return false
        val localPlayer = mc.player ?: return false
        val name = entity.gameProfile.name
        
        return when (targetMode) {
            "Self" -> entity == localPlayer
            "Friends" -> entity == localPlayer || FriendManager.isFriend(name)
            "Everyone" -> true
            else -> false
        }
    }
}
