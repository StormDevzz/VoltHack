package volthack.mixin.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import volthack.modules.player.AutoReconnect;

@Mixin(PauseScreen.class)
public abstract class MixinPauseScreen {
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!AutoReconnect.INSTANCE.getEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getCurrentServer() == null) return;
        if (mc.hasSingleplayerServer()) return;
        if (mc.getLevelSource() != null && mc.getSingleplayerServer() != null) return;

        PauseScreen screen = (PauseScreen) (Object) this;
        screen.addRenderableWidget(Button.builder(
            Component.literal("Reconnect"),
            button -> AutoReconnect.INSTANCE.reconnect()
        ).bounds(screen.width / 2 + 104, screen.height / 4 + 48, 98, 20).build());
    }
}
