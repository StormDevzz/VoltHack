package volthack.mixin.render;

import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import volthack.hud.HUDManager;
import volthack.hud.elements.ChatElement;

@Mixin(ChatComponent.class)
public abstract class MixinChatComponent {
    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"))
    private void onAddMessage(Component component, CallbackInfo ci) {
        ChatElement chat = (ChatElement) HUDManager.INSTANCE.get("Chat");
        if (chat != null && chat.getEnabled()) {
            chat.addMessage(component);
        }
    }
}
