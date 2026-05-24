package volthack.mixin.render;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import volthack.hud.HUDManager;
import volthack.hud.elements.ChatElement;

@Mixin(Gui.class)
public abstract class MixinChat {
    @Inject(method = "renderChat", at = @At("HEAD"), cancellable = true)
    private void onRenderChat(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ChatElement chat = (ChatElement) HUDManager.INSTANCE.get("Chat");
        if (chat != null && chat.getEnabled()) {
            ci.cancel();
        }
    }
}
