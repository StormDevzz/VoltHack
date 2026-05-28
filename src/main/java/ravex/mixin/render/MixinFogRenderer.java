package ravex.mixin.render;

import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ravex.modules.render.CustomFog;

@Mixin(FogRenderer.class)
public class MixinFogRenderer {
    /**
     * setupFog is the public method that computes and returns the final fog color Vector4f.
     * We override the return value when CustomFog is enabled.
     */
    @Inject(method = "setupFog", at = @At("RETURN"), cancellable = true)
    private void onSetupFog(Camera camera, int renderDistance, DeltaTracker deltaTracker, float bossColorModifier, ClientLevel level, CallbackInfoReturnable<org.joml.Vector4f> cir) {
        if (!CustomFog.INSTANCE.getEnabled()) return;

        int argb = CustomFog.INSTANCE.color.getValue();
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >>  8) & 0xFF) / 255.0f;
        float b = ( argb        & 0xFF) / 255.0f;
        float a = 1.0f;

        cir.setReturnValue(new org.joml.Vector4f(r, g, b, a));
    }
}
