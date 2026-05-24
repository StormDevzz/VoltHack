package volthack.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void onTurn(double yRot, double xRot, CallbackInfo ci) {
        if ((Object) this == net.minecraft.client.Minecraft.getInstance().player) {
            if (volthack.modules.render.FreeLook.INSTANCE.getEnabled()) {
                volthack.modules.render.FreeLook.INSTANCE.onMouseTurn(yRot, xRot);
                ci.cancel();
            }
        }
    }
}
