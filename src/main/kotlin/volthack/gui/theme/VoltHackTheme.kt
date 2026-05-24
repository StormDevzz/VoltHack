package volthack.gui.theme

import volthack.setting.Category

object VoltHackTheme {
    val background = 0xFF0D0D1A.toInt()
    val surface = 0xFF16162A.toInt()
    val surfaceLight = 0xFF1E1E3A.toInt()
    val surfaceHover = 0xFF28284A.toInt()
    val overlay = 0x80000000.toInt()
    val border = 0xFF2A2A4A.toInt()

    val accent = 0xFF6C63FF.toInt()
    val accentGlow = 0x406C63FF.toInt()
    val accentDim = 0x806C63FF.toInt()

    val textPrimary = 0xFFF0F0FF.toInt()
    val textSecondary = 0xFF9090B0.toInt()
    val textDisabled = 0xFF505068.toInt()

    val enabled = accent
    val enabledBg = 0x206C63FF.toInt()
    val disabledBg = surface

    val categoryColors = mapOf(
        Category.COMBAT to 0xFFFF4757.toInt(),
        Category.MOVEMENT to 0xFF2ED573.toInt(),
        Category.RENDER to 0xFF1E90FF.toInt(),
        Category.PLAYER to 0xFFFFA502.toInt(),
        Category.WORLD to 0xFFA855F7.toInt(),
        Category.CONFIGS to 0xFF00D4FF.toInt()
    )

    const val CARD_WIDTH = 130
    const val CARD_HEIGHT = 44
    const val CARD_GAP = 8
    const val TAB_HEIGHT = 44
    const val TAB_PADDING = 16
    const val PANEL_PADDING = 12
    const val BORDER_RADIUS = 6
    const val SCROLL_SPEED = 20
}
