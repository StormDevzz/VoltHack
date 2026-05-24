package volthack.util.render

import net.minecraft.client.gui.GuiGraphics
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme
import volthack.modules.render.Notifications

enum class NotificationType(val color: Int) {
    INFO(0xFF6C63FF.toInt()),
    WARNING(0xFFFFB300.toInt()),
    SUCCESS(0xFF00FF88.toInt()),
    ERROR(0xFFFF3333.toInt())
}

class Notification(
    val title: String,
    val message: String,
    val type: NotificationType,
    val duration: Long,
    val createdAt: Long = System.currentTimeMillis()
) {
    var animX = 1.0f
}

object NotificationManager {
    private val activeNotifications = mutableListOf<Notification>()

    fun add(title: String, message: String, type: NotificationType, durationMs: Long = 3000L) {
        if (!Notifications.enabled) return
        
        if (Notifications.playSound) {
            val mc = net.minecraft.client.Minecraft.getInstance()
            mc.execute {
                mc.player?.playSound(
                    net.minecraft.sounds.SoundEvents.NOTE_BLOCK_CHIME.value(),
                    1.0f,
                    1.0f
                )
            }
        }
        
        synchronized(activeNotifications) {
            activeNotifications.add(Notification(title, message, type, durationMs))
        }
    }

    fun render(ctx: GuiGraphics) {
        if (!Notifications.enabled) return

        val mc = net.minecraft.client.Minecraft.getInstance()
        val window = mc.window
        val screenWidth = window.guiScaledWidth
        
        val time = System.currentTimeMillis()
        
        synchronized(activeNotifications) {
            val iterator = activeNotifications.iterator()
            while (iterator.hasNext()) {
                val notif = iterator.next()
                val elapsed = time - notif.createdAt
                if (elapsed >= notif.duration) {
                    iterator.remove()
                }
            }
            
            var currentY = 10
            for (notif in activeNotifications) {
                val elapsed = time - notif.createdAt
                val duration = notif.duration
                
                val slideTime = 250f
                val targetAnimX = when {
                    elapsed < slideTime -> 1.0f - (elapsed / slideTime)
                    elapsed > (duration - slideTime) -> (elapsed - (duration - slideTime)) / slideTime
                    else -> 0.0f
                }
                
                notif.animX += (targetAnimX - notif.animX) * 0.2f
                
                val cardW = 180
                val cardH = 32
                
                val startX = screenWidth - cardW - 10
                val renderX = startX + (notif.animX * (cardW + 20))
                
                RenderUtils.roundedRect(ctx, renderX.toInt(), currentY, cardW, cardH, 4, 0xD00A0A14.toInt())
                RenderUtils.roundedBorder(ctx, renderX.toInt(), currentY, cardW, cardH, 4, VoltHackTheme.border, 1)
                
                ctx.fill(renderX.toInt(), currentY, renderX.toInt() + 3, currentY + cardH, notif.type.color)
                
                GUIFontRenderer.draw(
                    ctx,
                    notif.title,
                    renderX + 8f,
                    currentY + 4f,
                    VoltHackTheme.textPrimary
                )
                
                GUIFontRenderer.draw(
                    ctx,
                    notif.message,
                    renderX + 8f,
                    currentY + 16f,
                    VoltHackTheme.textSecondary
                )
                
                currentY += cardH + 6
            }
        }
    }
}
