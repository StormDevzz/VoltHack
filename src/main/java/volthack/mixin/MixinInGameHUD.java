package volthack.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import volthack.hud.HUDManager;

@Mixin(Gui.class)
public abstract class MixinInGameHUD {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        HUDManager.INSTANCE.render(context);
    }

    @Inject(method = "renderHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"), cancellable = true)
    private void onRenderHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        volthack.hud.HUDElement hotbar = HUDManager.INSTANCE.get("Hotbar");
        if (hotbar != null && hotbar.getEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        volthack.hud.HUDElement crosshair = HUDManager.INSTANCE.get("Crosshair");
        if (crosshair != null && crosshair.getEnabled()) {
            ci.cancel();
        }
    }
}
