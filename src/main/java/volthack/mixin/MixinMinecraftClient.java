package volthack.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import volthack.VoltHack;
import volthack.event.EventBus;
import volthack.event.TickEvent;

@Mixin(Minecraft.class)
public abstract class MixinMinecraftClient {
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        EventBus.INSTANCE.emit(new TickEvent());
        VoltHack.onClientTick();
    }
}
