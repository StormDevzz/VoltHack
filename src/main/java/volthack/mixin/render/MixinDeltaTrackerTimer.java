package volthack.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(net.minecraft.client.DeltaTracker.Timer.class)
public class MixinDeltaTrackerTimer {
    @Shadow private long lastMs;

    @ModifyVariable(method = "advanceTime(JZ)I", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private long modifyTime(long time) {
        if (volthack.modules.movement.Timer.INSTANCE.getEnabled()) {
            float speed = volthack.modules.movement.Timer.INSTANCE.getTimerSpeed();
            if (this.lastMs > 0L) {
                long elapsed = time - this.lastMs;
                return this.lastMs + (long) (elapsed * speed);
            }
        }
        return time;
    }
}
