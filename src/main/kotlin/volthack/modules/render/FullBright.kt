package volthack.modules.render

import net.minecraft.client.Minecraft
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import volthack.setting.Category
import volthack.setting.Module

object FullBright : Module("FullBright", "Makes everything bright", Category.RENDER) {
    private val mode by mode("Mode", listOf("Gamma", "Potion"), "Gamma")
    private var previousGamma = -1.0

    override fun onEnable() {
        when (mode) {
            "Gamma" -> {
                previousGamma = Minecraft.getInstance().options.gamma().get()
                Minecraft.getInstance().options.gamma().set(1.0)
            }
            "Potion" -> {
                Minecraft.getInstance().player?.addEffect(
                    MobEffectInstance(MobEffects.NIGHT_VISION, 999999, 0, false, false, false)
                )
            }
        }
    }

    override fun onDisable() {
        when (mode) {
            "Gamma" -> {
                if (previousGamma >= 0) {
                    Minecraft.getInstance().options.gamma().set(previousGamma)
                }
            }
            "Potion" -> {
                Minecraft.getInstance().player?.removeEffect(MobEffects.NIGHT_VISION)
            }
        }
    }
}
