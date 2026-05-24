package volthack.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.DisconnectionDetails;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import volthack.manager.NetworkManager;
import volthack.modules.player.AutoReconnect;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class MixinClientPacketListener {
    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void onDisconnect(DisconnectionDetails details, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        ServerData serverData = mc.getCurrentServer();
        if (serverData != null) {
            ServerAddress serverAddress = ServerAddress.parseString(serverData.ip);
            AutoReconnect.INSTANCE.onDisconnected(serverData, serverAddress);
        }
    }
}
