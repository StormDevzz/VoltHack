package ravex.mixin.render;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
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
                    PoseStack poseStack = new PoseStack();
                    
                    // 1. Recreate camera rotation matrix using mapping-independent reflection pitch and yaw helpers
                    float pitch = Render3DUtils.getPitch(camera);
                    float yaw = Render3DUtils.getYaw(camera);
                    
                    poseStack.mulPose(new org.joml.Quaternionf().rotationX((float) Math.toRadians(pitch)));
                    poseStack.mulPose(new org.joml.Quaternionf().rotationY((float) Math.toRadians(yaw + 180.0f)));
                    
                    // 2. Translate coordinates relative to camera position to place in absolute world coordinates
                    double[] camPos = Render3DUtils.getCameraPos(camera);
                    poseStack.translate(
                        (float) (ravex.modules.player.AirPlace.highlightPos.x - camPos[0]),
                        (float) (ravex.modules.player.AirPlace.highlightPos.y - camPos[1]),
                        (float) (ravex.modules.player.AirPlace.highlightPos.z - camPos[2])
                    );
                    
                    org.joml.Matrix4f matrix = poseStack.last().pose();
                    
                    MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
                    VertexConsumer vertexConsumer = Render3DUtils.getLinesConsumer(bufferSource);
                    if (vertexConsumer != null) {
                        float r = 1.0f;
                        float g = 0.2f;
                        float b = 0.2f;
                        float a = ravex.modules.player.AirPlace.renderAlpha * 0.8f; // scale maximum alpha elegantly
                        
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
