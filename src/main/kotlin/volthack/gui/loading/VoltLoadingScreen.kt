package volthack.gui.loading

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import volthack.VoltHack
import volthack.gui.menu.VoltMainMenu
import volthack.gui.theme.VoltHackTheme
import volthack.util.render.FontUtils
import volthack.util.render.RenderUtils

class VoltLoadingScreen : Screen(Component.literal("Loading")) {

    private var startTime = 0L
    private var fadeOut = 0f

    private val displaySteps = listOf(
        "Loading languages...",
        "Registering modules...",
        "Loading module configs...",
        "Initializing HUD...",
        "Checking GitHub...",
        "Setting up keybinds...",
        "Connecting Discord...",
        "VoltHack loaded!"
    )

    override fun init() {
        startTime = System.currentTimeMillis()
    }

    override fun tick() {
        if (LoadingState.complete && fadeOut < 1f) {
            fadeOut += 0.04f
            if (fadeOut >= 1f) {
                Minecraft.getInstance().setScreen(VoltMainMenu())
            }
        }
    }

    override fun render(ctx: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val elapsed = System.currentTimeMillis() - startTime
        val et = elapsed / 1000f
        val alphaMul = (1f - fadeOut).coerceIn(0f, 1f)
        if (alphaMul <= 0f) return

        ctx.fill(0, 0, width, height, 0xFF0D0D1A.toInt())

        val cx = width / 2f
        val titleStr = "VoltHack"
        val titleW = FontUtils.width(titleStr)
        val titleY = height / 2f - 50f

        FontUtils.drawGradientText(ctx, titleStr, cx - titleW / 2f, titleY,
            0xFF6C63FF.toInt(), 0xFF00D4FF.toInt())

        FontUtils.drawCentered(ctx, "v${VoltHack.version}", cx,
            titleY + FontUtils.height + 6f,
            RenderUtils.withAlpha(VoltHackTheme.textSecondary, (160 * alphaMul).toInt()))

        val stepCount = displaySteps.size.coerceAtLeast(1)
        val rawProgress = LoadingState.progress.coerceIn(0f, 1f)
        val displayIdx = (rawProgress * (stepCount - 1)).toInt().coerceIn(0, stepCount - 1)
        val displayText = displaySteps[displayIdx]

        FontUtils.drawCentered(ctx, displayText, cx, height / 2f + 20f,
            RenderUtils.withAlpha(VoltHackTheme.textPrimary, (200 * alphaMul).toInt()))

        val barW = 260; val barH = 3
        val barX = (cx - barW / 2).toInt(); val barY = height / 2 + 44

        ctx.fill(barX, barY, barX + barW, barY + barH,
            RenderUtils.withAlpha(VoltHackTheme.surfaceLight, (120 * alphaMul).toInt()))

        val filled = (barW * rawProgress).toInt()
        if (filled > 0) {
            ctx.fill(barX, barY, barX + filled, barY + barH,
                RenderUtils.withAlpha(0xFF6C63FF.toInt(), (220 * alphaMul).toInt()))
        }
    }

    override fun isPauseScreen() = false
}
