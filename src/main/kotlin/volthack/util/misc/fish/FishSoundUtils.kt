package volthack.util.misc.fish

import net.minecraft.client.Minecraft
import net.minecraft.sounds.SoundEvents

object FishSoundUtils {
    private val mc get() = Minecraft.getInstance()

    fun playReelSound() {
        mc.player?.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 1.0f, 1.0f)
    }

    fun playCatchSound() {
        mc.player?.playSound(SoundEvents.FISHING_BOBBER_RETRIEVE, 1.0f, 1.0f)
    }
}
