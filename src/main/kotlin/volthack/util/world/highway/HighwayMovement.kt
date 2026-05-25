package volthack.util.world.highway

import net.minecraft.client.Minecraft
import net.minecraft.core.Direction

object HighwayMovement {
    fun autoWalk(dir: Direction) {
        if (!HighwayConfig.autoWalk) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        mc.options.keyUp.isDown = true
    }

    fun stop() {
        val mc = Minecraft.getInstance()
        mc.options.keyUp.isDown = false
    }
}
