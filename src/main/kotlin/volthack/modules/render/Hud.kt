package volthack.modules.render

import net.minecraft.client.Minecraft
import volthack.gui.HUDEditorScreen
import volthack.setting.Category
import volthack.setting.Module

object Hud : Module("HUD Editor", "Open the HUD editor to arrange and toggle HUD elements", Category.RENDER) {
    override fun onEnable() {
        Minecraft.getInstance().setScreen(HUDEditorScreen())
        disable()
    }
}
