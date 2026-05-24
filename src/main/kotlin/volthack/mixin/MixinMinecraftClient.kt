package volthack.mixin

import net.minecraft.client.Minecraft
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import volthack.VoltHack
import volthack.event.EventBus
import volthack.event.TickEvent

@Mixin(Minecraft::class)
abstract class MixinMinecraftClient {
    @Inject(method = ["tick"], at = [At("TAIL")])
    private fun onTick(ci: CallbackInfo) {
        EventBus.emit(TickEvent())
        VoltHack.onClientTick()
    }
}
