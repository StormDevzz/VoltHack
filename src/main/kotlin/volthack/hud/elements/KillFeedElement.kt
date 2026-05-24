package volthack.hud.elements

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.LivingEntity
import volthack.gui.font.GUIFontRenderer
import volthack.gui.theme.VoltHackTheme
import volthack.hud.HUDElement
import java.util.concurrent.CopyOnWriteArrayList

class KillFeedElement : HUDElement("KillFeed") {
    private val customColor by color("Color", 0xFF6C63FF.toInt())
    private val showMockKills by boolean("Show Mock Kills", true)

    private val skullLoc = Identifier.tryParse("volthack:textures/skull.png") ?: Identifier.tryParse("volthack:textures/volthack.png")!!

    data class KillEntry(val name: String, val timestamp: Long)

    private val killsList = CopyOnWriteArrayList<KillEntry>()
    private val recordedDeathIds = mutableSetOf<Int>()

    init {
        x = 4
        y = 120
        cachedWidth = 120
        cachedHeight = 50
        enabled = false
    }

    override fun draw(ctx: GuiGraphics) {
        val mc = Minecraft.getInstance()
        val player = mc.player
        val world = mc.level

        // Local tracking of deaths when active in a world
        if (player != null && world != null) {
            for (entity in world.entitiesForRendering()) {
                if (entity is LivingEntity && entity != player) {
                    if ((entity.isDeadOrDying || entity.health <= 0) && entity.lastHurtByMob == player) {
                        val entityId = entity.id
                        if (entityId !in recordedDeathIds) {
                            recordedDeathIds.add(entityId)
                            val name = entity.name.string
                            // Add new kill entry (limit list to last 5 kills for neat layout)
                            killsList.add(0, KillEntry(name, System.currentTimeMillis()))
                            if (killsList.size > 5) {
                                killsList.removeAt(killsList.size - 1)
                            }
                        }
                    }
                }
            }
        }

        // Get actual or mock kills
        val displayKills = if (killsList.isEmpty() && showMockKills) {
            listOf(
                KillEntry("StormDevzz", System.currentTimeMillis()),
                KillEntry("Gamer123", System.currentTimeMillis()),
                KillEntry("NoobSlayer", System.currentTimeMillis())
            )
        } else {
            killsList
        }

        val totalKillsText = "Kills: ${if (killsList.isEmpty()) displayKills.size else killsList.size}"
        
        var maxW = GUIFontRenderer.width(totalKillsText)
        for (k in displayKills) {
            val entryW = 16 + GUIFontRenderer.width(k.name) // 12px skull + 4px gap
            if (entryW > maxW) maxW = entryW
        }

        cachedWidth = maxW + 16
        cachedHeight = 8 + GUIFontRenderer.height + 6 + (displayKills.size * (GUIFontRenderer.height + 4))

        // Draw card background
        ctx.fill(x, y, x + cachedWidth, y + cachedHeight, VoltHackTheme.surface)
        ctx.fill(x, y, x + cachedWidth, y + 1, customColor)

        // Draw total kills count
        val tx = x + 8f
        var cy = y + 6f
        GUIFontRenderer.draw(ctx, totalKillsText, tx, cy, customColor)
        cy += GUIFontRenderer.height + 6f

        // Draw each kill with the skull icon
        for (k in displayKills) {
            val skullX = x + 8
            val skullY = cy.toInt() + (GUIFontRenderer.height - 12) / 2
            
            // Draw skull icon (using transparent PNG)
            ctx.blit(
                skullLoc,
                skullX,
                skullY,
                0,
                0,
                12f,
                12f,
                1024f,
                1024f
            )

            // Draw player name next to it
            val nameX = skullX + 16f
            GUIFontRenderer.draw(ctx, k.name, nameX, cy, VoltHackTheme.textPrimary)
            cy += GUIFontRenderer.height + 4f
        }
    }
}
