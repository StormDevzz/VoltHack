package volthack.mixin.render;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import volthack.modules.player.FastBreak;

@Mixin(MultiPlayerGameMode.class)
public class MixinMultiPlayerGameMode {
    @Shadow
    private int destroyDelay;

    @Shadow
    private float destroyProgress;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (FastBreak.INSTANCE.getEnabled()) {
            this.destroyDelay = 0;
        }
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    private void onContinueDestroyBlock(net.minecraft.core.BlockPos blockPos, net.minecraft.core.Direction direction, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (FastBreak.INSTANCE.getEnabled()) {
            float mult = FastBreak.INSTANCE.getSpeed();
            if (mult > 1.0f && this.destroyProgress > 0.0f) {
                this.destroyProgress += 0.05f * (mult - 1.0f);
            }
        }
    }
}
