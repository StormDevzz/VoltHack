package volthack.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {
    @Inject(method = "scale(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("HEAD"))
    private void onScale(LivingEntityRenderState state, PoseStack poseStack, CallbackInfo ci) {
        if (volthack.modules.render.SmallUser.INSTANCE.getEnabled()) {
            if (volthack.modules.render.SmallUser.INSTANCE.shouldScale(state)) {
                float size = volthack.modules.render.SmallUser.INSTANCE.getScaleFactor();
                poseStack.scale(size, size, size);
            }
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void onExtractRenderState(net.minecraft.world.entity.LivingEntity entity, LivingEntityRenderState state, float f, CallbackInfo ci) {
        if (volthack.modules.render.SmallUser.INSTANCE.getEnabled()) {
            if (volthack.modules.render.SmallUser.INSTANCE.shouldScale(state)) {
                if (volthack.modules.render.SmallUser.INSTANCE.getForceBaby()) {
                    state.isBaby = true;
                }
            }
        }
    }
}
