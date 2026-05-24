package volthack.mixin;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import volthack.manager.InputManager;

@Mixin(KeyboardHandler.class)
public class MixinKeyboard {
    @Inject(method = "method_22676", at = @At("HEAD"))
    private void onKeyRaw(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        InputManager.INSTANCE.onKey(key, action);
    }
}
