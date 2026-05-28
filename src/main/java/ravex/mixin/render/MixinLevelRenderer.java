package ravex.mixin.render;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ravex.utility.render.BlockRenderer;
import ravex.utility.render.Render3DUtils;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void onRenderLevel(
        com.mojang.blaze3d.resource.GraphicsResourceAllocator graphicsResourceAllocator,
        net.minecraft.client.DeltaTracker deltaTracker,
        boolean renderBlockOutline,
        net.minecraft.client.Camera camera,
        org.joml.Matrix4f modelViewMatrix,
        org.joml.Matrix4f projectionMatrix,
        org.joml.Matrix4f matrix3,
        com.mojang.blaze3d.buffers.GpuBufferSlice gpuBufferSlice,
        org.joml.Vector4f vector4f,
        boolean bool2,
        CallbackInfo ci
    ) {
        if (ravex.modules.player.AirPlace.INSTANCE.getEnabled() && ravex.modules.player.AirPlace.highlightPos != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                try {
                    // Use modelViewMatrix directly — it already contains the correct camera transform.
                    // We just need to translate it to our target world position relative to the camera.
                    double[] camPos = Render3DUtils.getCameraPos(camera);
                    Vec3 hp = ravex.modules.player.AirPlace.highlightPos;

                    org.joml.Matrix4f matrix = new org.joml.Matrix4f(modelViewMatrix)
                        .translate(
                            (float)(hp.x - camPos[0]),
                            (float)(hp.y - camPos[1]),
                            (float)(hp.z - camPos[2])
                        );

                    MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
                    VertexConsumer vertexConsumer = Render3DUtils.getLinesConsumer(bufferSource);
                    if (vertexConsumer != null) {
                        float r = 0.4f;
                        float g = 0.8f;
                        float b = 1.0f;
                        float a = ravex.modules.player.AirPlace.renderAlpha * 0.9f;

                        double size = ravex.modules.player.AirPlace.renderSize;

                        BlockRenderer.renderWireframe(vertexConsumer, matrix, size, r, g, b, a);
                        Render3DUtils.endLinesBatch(bufferSource);
                    }
                } catch (Exception e) {
                    // Fail silently to prevent spam
                }
            }
        }
    }
}
