package volthack.mixin.render;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import volthack.modules.player.MultiTask;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @Inject(method = "isUsingItem", at = @At("HEAD"), cancellable = true)
    private void onIsUsingItem(CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof net.minecraft.client.player.LocalPlayer) {
            if (MultiTask.INSTANCE.getEnabled()) {
                for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                    String methodName = element.getMethodName();
                    if (methodName.contains("startAttack") || 
                        methodName.contains("continueAttack") || 
                        methodName.contains("handleKeybinds") ||
                        methodName.contains("handleMouseClick")) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }
        }
    }
}
