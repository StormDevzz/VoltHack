package volthack.modules.render

import volthack.gui.font.GUIFontRenderer
import volthack.setting.Category
import volthack.setting.Module

object FontModule : Module("Font", "Changes the font of the client UI using local TTF files", Category.RENDER) {
    val selectedFont by mode("Font Name", listOf(
        "InterDisplay-Black",
        "Inter-BoldItalic",
        "Inter-ExtraLightItalic",
        "Inter-Thin",
        "InterDisplay-Thin"
    ), "InterDisplay-Black", "Select which custom TTF font to use", onChanged = {
        if (enabled) {
            GUIFontRenderer.reloadFont()
        }
    })
    
    val size by float("Font Size", 16.0f, 10.0f, 24.0f, 1.0f, "The size of the rendered text", onChanged = {
        if (enabled) {
            GUIFontRenderer.reloadFont()
        }
    })

    override fun onEnable() {
        GUIFontRenderer.reloadFont()
    }
}
