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

        // Animate colors using a smooth neon HSB cycle
        long time = System.currentTimeMillis();
        int cornerColor = (0xFF << 24) | (java.awt.Color.HSBtoRGB((time % 5000) / 5000f, 0.75f, 1.0f) & 0xFFFFFF);
        int barStartColor = (0xFF << 24) | (java.awt.Color.HSBtoRGB((time % 4000) / 4000f, 0.8f, 1.0f) & 0xFFFFFF);
        int barEndColor = (0xFF << 24) | (java.awt.Color.HSBtoRGB(((time + 1000) % 4000) / 4000f, 0.8f, 1.0f) & 0xFFFFFF);

        drawSafeText(mc, context, title, titleX, titleY, cornerColor, true);
        drawSafeText(mc, context, ver, verX, titleY + 14, 0xFF9090B0, false);

        int barW = 200;
        int barH = 4;
        int barX = cx - barW / 2;
        int barY = cy + 30;

        // Custom clean central graphics
        context.fill(cx - 50, cy - 20, cx + 50, cy - 19, cornerColor);
        context.fill(cx - 30, cy - 14, cx + 30, cy - 13, 0x44FFFFFF);

        context.fill(barX, barY, barX + barW, barY + barH, 0xFF14142B);
        if (currentProgress > 0.0f) {
            int filled = (int) (barW * currentProgress);
            if (filled > 0) {
                context.fillGradient(barX, barY, barX + filled, barY + barH, barStartColor, barEndColor);
            }
        }

        int cornerSize = 20;
        int cornerOffset = 10;
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
