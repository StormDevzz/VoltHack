package volthack.gui.menu

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.options.OptionsScreen
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import volthack.VoltHack
import volthack.gui.theme.VoltHackTheme
import volthack.util.render.FontUtils
import volthack.util.render.RenderUtils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class VoltMainMenu : Screen(Component.literal("VoltHack Main Menu")) {

    private data class Particle(
        var x: Double, var y: Double,
        var speedX: Double, var speedY: Double,
        val size: Int, val baseAlpha: Double, val phase: Double,
        val colorHue: Float
    )

    private data class MenuButton(
        val x: Float, val y: Float, val w: Float, val h: Float,
        val label: Component, val action: () -> Unit
    ) {
        var hover: Float = 0f
        fun contains(mx: Double, my: Double) = mx >= x && mx <= x + w && my >= y && my <= y + h
    }

    private val particles = mutableListOf<Particle>()
    private var startTime = 0L
    private var buttons = listOf<MenuButton>()
    private var initialized = false

    private val neonColors = intArrayOf(
        0xFF6C63FF.toInt(),
        0xFF00D4FF.toInt(),
        0xFFFF00E5.toInt(),
        0xFF00FF88.toInt()
    )

    override fun init() {
        startTime = System.currentTimeMillis()
        particles.clear()
        repeat(60) {
            particles.add(
                Particle(
                    x = Random.nextDouble() * width,
                    y = Random.nextDouble() * height,
                    speedX = (Random.nextDouble() - 0.5) * 0.25,
                    speedY = -(Random.nextDouble() * 0.3 + 0.05),
                    size = Random.nextInt(3) + 1,
                    baseAlpha = Random.nextDouble() * 0.5 + 0.15,
                    phase = Random.nextDouble() * PI * 2,
                    colorHue = Random.nextFloat()
                )
            )
        }

        val cx = width / 2f
        val bw = 220f; val bh = 38f
        val gap = 10f
        val startY = height / 2f - 25f

        buttons = listOf(
            MenuButton(cx - bw / 2, startY, bw, bh,
                Component.literal("VoltHack ClickGUI")) {
                Minecraft.getInstance().setScreen(volthack.gui.ClickGUI())
            },
            MenuButton(cx - bw / 2, startY + (bh + gap) * 1, bw, bh,
                Component.translatable("menu.singleplayer")) {
                Minecraft.getInstance().setScreen(SelectWorldScreen(this))
            },
            MenuButton(cx - bw / 2, startY + (bh + gap) * 2, bw, bh,
                Component.translatable("menu.multiplayer")) {
                Minecraft.getInstance().setScreen(JoinMultiplayerScreen(this))
            },
            MenuButton(cx - bw / 2, startY + (bh + gap) * 3, bw, bh,
                Component.translatable("menu.options")) {
                Minecraft.getInstance().setScreen(OptionsScreen(this, Minecraft.getInstance().options))
            },
            MenuButton(cx - bw / 2, startY + (bh + gap) * 4, bw, bh,
                Component.translatable("menu.quit")) {
                Minecraft.getInstance().stop()
            }
        )
        initialized = true
    }

    private fun getAnimatedColor(elapsed: Long, offset: Float = 0f, speed: Float = 0.0003f): Int {
        val hue = ((elapsed * speed + offset) % 1.0f)
        return java.awt.Color.HSBtoRGB(hue, 0.7f, 1.0f)
    }

    override fun render(ctx: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val elapsed = System.currentTimeMillis() - startTime
        val et = elapsed / 1000f
        val w = width; val h = height

        val bgShift = (sin(et * 0.15) * 0.03 + 0.80).toFloat()
        val bgTop = RenderUtils.lerpColor(0xFF0D0D1A.toInt(), 0xFF0A0A2E.toInt(), bgShift)
        val bgBot = RenderUtils.lerpColor(0xFF070712.toInt(), 0xFF0D0D2A.toInt(), bgShift)
        RenderUtils.gradientVertical(ctx, 0, 0, w, h, bgTop, bgBot)

        if (initialized) {
            for (p in particles) {
                p.x += p.speedX; p.y += p.speedY
                if (p.y < -5) { p.y = h + 5.0; p.x = Random.nextDouble() * w }
                if (p.x < -5) p.x = w + 5.0
                if (p.x > w + 5) p.x = -5.0

                val twinkle = (sin(elapsed * 0.003 + p.phase) * 0.3 + 0.7).toFloat()
                val pa = (p.baseAlpha * twinkle * 255).toInt().coerceIn(0, 255)
                if (pa < 3) continue

                val particleColor = java.awt.Color.HSBtoRGB(
                    ((elapsed * 0.0001f + p.colorHue) % 1.0f), 0.8f, 1.0f
                )
                val pc = (pa shl 24) or (particleColor and 0x00FFFFFF)

                if (p.size > 2) {
                    ctx.fill(p.x.toInt(), p.y.toInt(), p.x.toInt() + p.size, p.y.toInt() + p.size, pc)
                    ctx.fill((p.x - 1).toInt(), (p.y - 1).toInt(), (p.x + p.size + 1).toInt(), (p.y + p.size + 1).toInt(),
                        (pa / 3 shl 24) or (particleColor and 0x00FFFFFF))
                } else {
                    ctx.fill(p.x.toInt(), p.y.toInt(), p.x.toInt() + 1, p.y.toInt() + 1, pc)
                }
            }

            for (btn in buttons) {
                btn.hover += (if (btn.contains(mouseX.toDouble(), mouseY.toDouble())) 0.08f else -0.08f) * delta
                btn.hover = btn.hover.coerceIn(0f, 1f)
            }
        }

        val cx = w / 2f
        val pulse = (sin(et * 2.0) * 0.12 + 0.88).toFloat()

        val titleStr = "VoltHack"
        val titleW = FontUtils.width(titleStr)
        val titleY = h / 4f - 10
        val titleX = cx - titleW / 2f

        val glowExpand = (25 + 12 * sin(et * 2.0)).toInt()
        val glowColor = getAnimatedColor(elapsed, 0.0f, 0.00015f)
        ctx.fill((titleX - glowExpand).toInt(), (titleY - glowExpand / 2).toInt(),
            (titleX + titleW + glowExpand).toInt(), (titleY + FontUtils.height + glowExpand / 2).toInt(),
            ((25 * pulse).toInt() shl 24) or (glowColor and 0x00FFFFFF))

        val gradStart = getAnimatedColor(elapsed, 0.0f, 0.0001f)
        val gradEnd = getAnimatedColor(elapsed, 0.5f, 0.0001f)
        FontUtils.drawGradientText(ctx, titleStr, titleX, titleY, gradStart, gradEnd)

        val lineW = (100 + 30 * sin(et * 1.2).toFloat()).toInt()
        val lineColor = getAnimatedColor(elapsed, 0.2f, 0.0002f)
        ctx.fill((cx - lineW / 2).toInt(), (titleY + FontUtils.height + 8).toInt(),
            (cx + lineW / 2).toInt(), (titleY + FontUtils.height + 10).toInt(), lineColor)

        FontUtils.drawCentered(ctx, "v${VoltHack.version}", cx, titleY + FontUtils.height + 18,
            VoltHackTheme.textSecondary)

        for (btn in buttons) {
            val bh = btn.h
            val bw = btn.w
            val bx = btn.x; val by = btn.y

            val bgColor = RenderUtils.lerpColor(VoltHackTheme.surface, VoltHackTheme.surfaceHover, btn.hover)
            val borderColor = RenderUtils.lerpColor(VoltHackTheme.border, 0xFF6C63FF.toInt(), btn.hover)
            val textColor = RenderUtils.lerpColor(VoltHackTheme.textSecondary, VoltHackTheme.textPrimary, btn.hover)

            RenderUtils.roundedRect(ctx, bx.toInt(), by.toInt(), bw.toInt(), bh.toInt(), 4, bgColor)

            if (btn.hover > 0.01f) {
                val neonGlow = getAnimatedColor(elapsed, btn.y * 0.01f, 0.0003f)
                RenderUtils.roundedBorder(ctx, bx.toInt() - 1, by.toInt() - 1, bw.toInt() + 2, bh.toInt() + 2, 4,
                    RenderUtils.withAlpha(neonGlow, (60 * btn.hover).toInt()), 2)
            }

            RenderUtils.roundedBorder(ctx, bx.toInt(), by.toInt(), bw.toInt(), bh.toInt(), 4, borderColor, 1)

            if (btn.hover > 0.01f) {
                val lineH = 2
                val lineAlpha = (btn.hover * 255).toInt()
                val lineColor = getAnimatedColor(elapsed, btn.y * 0.01f + 0.3f, 0.0003f)
                ctx.fill(bx.toInt() + 10, (by + bh - lineH).toInt(), (bx + bw - 10).toInt(), (by + bh).toInt(),
                    (lineAlpha shl 24) or (lineColor and 0x00FFFFFF))
            }

            FontUtils.drawCentered(ctx, btn.label, bx + bw / 2f, by + (bh - FontUtils.height) / 2f, textColor)
        }

        val footColor = RenderUtils.withAlpha(VoltHackTheme.textDisabled, (120 + 60 * sin(et * 1.5)).toInt().coerceIn(40, 180))
        FontUtils.drawCentered(ctx, "VoltHack Client", cx, h - 20f, footColor)
    }

    override fun mouseClicked(event: MouseButtonEvent, isInside: Boolean): Boolean {
        val mx = event.x(); val my = event.y()
        if (event.button() == 0) {
            for (btn in buttons) {
                if (btn.contains(mx, my)) {
                    btn.action()
                    return true
                }
            }
        }
        return super.mouseClicked(event, isInside)
    }

    override fun isPauseScreen() = false
}
