package volthack.mixin.render;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import volthack.event.EventBus;
import volthack.event.Render3DEvent;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void onRenderLevel(
            com.mojang.blaze3d.resource.GraphicsResourceAllocator graphicsResourceAllocator,
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            Matrix4f modelViewMatrix,
            Matrix4f projectionMatrix,
            Matrix4f matrix4f3,
            com.mojang.blaze3d.buffers.GpuBufferSlice gpuBufferSlice,
            org.joml.Vector4f vector4f,
            boolean bl2,
            CallbackInfo ci
    ) {
        EventBus.INSTANCE.emit(new Render3DEvent(
            deltaTracker.getGameTimeDeltaPartialTick(true),
            modelViewMatrix,
            projectionMatrix
        ));
    }
}
