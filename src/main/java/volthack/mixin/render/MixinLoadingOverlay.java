package volthack.mixin.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import volthack.VoltHack;

@Mixin(LoadingOverlay.class)
public class MixinLoadingOverlay {

    @Shadow
    private float currentProgress;

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        context.fill(0, 0, w, h, 0xFF0D0D1A);

        int cx = w / 2;
        int cy = h / 2;

        String title = "VoltHack";
        String ver = "v" + VoltHack.getVersion();
        int titleW = getTextWidth(mc, title);
        int verW = getTextWidth(mc, ver);
        int titleX = cx - titleW / 2;
        int verX = cx - verW / 2;
        int titleY = cy - 50;

        drawSafeText(mc, context, title, titleX, titleY, 0xFF6C63FF, true);
        drawSafeText(mc, context, ver, verX, titleY + 14, 0xFF9090B0, false);

        int barW = 200;
        int barH = 3;
        int barX = cx - barW / 2;
        int barY = cy + 30;

        context.fill(cx - 60, cy - 22, cx + 60, cy - 18, 0xFF6C63FF);
        context.fill(cx - 40, cy - 14, cx + 40, cy - 12, 0xFF4038CC);

        context.fill(barX, barY, barX + barW, barY + barH, 0xFF1E1E3A);
        if (currentProgress > 0.0f) {
            int filled = (int) (barW * currentProgress);
            if (filled > 0) {
                context.fill(barX, barY, barX + filled, barY + barH, 0xFF6C63FF);
            }
        }

        int cornerSize = 16;
        int cornerOffset = 12;
        int cornerColor = 0xFF6C63FF;
        context.fill(cornerOffset, cornerOffset, cornerOffset + 2, cornerOffset + cornerSize, cornerColor);
        context.fill(cornerOffset, cornerOffset, cornerOffset + cornerSize, cornerOffset + 2, cornerColor);
        context.fill(w - cornerOffset - 2, cornerOffset, w - cornerOffset, cornerOffset + cornerSize, cornerColor);
        context.fill(w - cornerOffset - cornerSize, cornerOffset, w - cornerOffset, cornerOffset + 2, cornerColor);
        context.fill(cornerOffset, h - cornerOffset - cornerSize, cornerOffset + 2, h - cornerOffset, cornerColor);
        context.fill(cornerOffset, h - cornerOffset - 2, cornerOffset + cornerSize, h - cornerOffset, cornerColor);
        context.fill(w - cornerOffset - 2, h - cornerOffset - cornerSize, w - cornerOffset, h - cornerOffset, cornerColor);
        context.fill(w - cornerOffset - cornerSize, h - cornerOffset - 2, w - cornerOffset, h - cornerOffset, cornerColor);
    }

    private void drawSafeText(Minecraft mc, GuiGraphics ctx, String text, int x, int y, int color, boolean shadow) {
        try {
            if (mc.font != null) {
                ctx.drawString(mc.font, text, x, y, color, shadow);
            }
        } catch (Exception ignored) {
        }
    }

    private int getTextWidth(Minecraft mc, String text) {
        try {
            if (mc.font != null) return mc.font.width(text);
        } catch (Exception ignored) {
        }
        return text.length() * 6;
    }
}
