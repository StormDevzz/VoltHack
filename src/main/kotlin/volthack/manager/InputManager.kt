package volthack.manager

import net.minecraft.client.Minecraft
import volthack.gui.ClickGUI
import volthack.gui.ModuleSettingsScreen
import volthack.setting.ModuleManager

object InputManager {
    var bindTargetModule: volthack.setting.Module? = null

    @JvmStatic
    fun onKey(key: Int, action: Int) {
        val mc = Minecraft.getInstance()
        if (mc.screen != null) {
            if ((mc.screen is ClickGUI || mc.screen is ModuleSettingsScreen) && bindTargetModule != null) {
                if (action == 1) { // GLFW_PRESS
                    if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                        bindTargetModule?.bindKey = 0
                    } else {
                        bindTargetModule?.bindKey = key
                    }
                    // Save configuration immediately on key bind
                    val mod = bindTargetModule
                    if (mod != null) {
                        volthack.setting.ModuleConfig.save(mod.name, volthack.setting.ModuleConfig.ModuleData(enabled = mod.enabled, bindKey = mod.bindKey))
                    }
                    bindTargetModule = null
                }
            }
            return
        }

        if (action == 1) { // GLFW_PRESS
            for (module in ModuleManager.getAll()) {
                if (module.bindKey == key) {
                    module.toggle()
                }
            }
        }
    }

    @JvmStatic
    fun onMouse(button: Int, action: Int) {
        val mc = Minecraft.getInstance()
        val bindCode = -button - 1

        if (mc.screen != null) {
            if ((mc.screen is ClickGUI || mc.screen is ModuleSettingsScreen) && bindTargetModule != null) {
                if (action == 1) { // GLFW_PRESS
                    bindTargetModule?.bindKey = bindCode
                    val mod = bindTargetModule
                    if (mod != null) {
                        volthack.setting.ModuleConfig.save(mod.name, volthack.setting.ModuleConfig.ModuleData(enabled = mod.enabled, bindKey = mod.bindKey))
                    }
                    bindTargetModule = null
                }
            }
            return
        }

        if (action == 1) { // GLFW_PRESS
            for (module in ModuleManager.getAll()) {
                if (module.bindKey == bindCode) {
                    module.toggle()
                }
            }
        }
    }

    @JvmStatic
    fun onScroll(yoffset: Double) {
        val mc = Minecraft.getInstance()
        if (yoffset == 0.0) return

        if (mc.screen != null) {
            if ((mc.screen is ClickGUI || mc.screen is ModuleSettingsScreen) && bindTargetModule != null) {
                val bindCode = if (yoffset > 0.0) -100 else -101
                bindTargetModule?.bindKey = bindCode
                val mod = bindTargetModule
                if (mod != null) {
                    volthack.setting.ModuleConfig.save(mod.name, volthack.setting.ModuleConfig.ModuleData(enabled = mod.enabled, bindKey = mod.bindKey))
                }
                bindTargetModule = null
            }
            return
        }

        val bindCode = if (yoffset > 0.0) -100 else -101
        for (module in ModuleManager.getAll()) {
            if (module.bindKey == bindCode) {
                module.toggle()
            }
        }
    }

    fun getBindName(bindKey: Int): String {
        if (bindKey == 0) return "NONE"
        if (bindKey > 0) {
            return org.lwjgl.glfw.GLFW.glfwGetKeyName(bindKey, 0)?.uppercase() ?: "KEY_$bindKey"
        }
        if (bindKey == -100) return "SCROLL_UP"
        if (bindKey == -101) return "SCROLL_DOWN"
        val btn = -bindKey - 1
        return when (btn) {
            0 -> "LCLICK"
            1 -> "RCLICK"
            2 -> "MCLICK"
            else -> "MOUSE_$btn"
        }
    }
}
