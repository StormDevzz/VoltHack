package volthack.mixin

import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiGraphics
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import volthack.hud.HUDManager

@Mixin(Gui::class)
abstract class MixinInGameHUD {
    @Inject(method = ["render"], at = [At("TAIL")])
    private fun onRender(context: GuiGraphics, tickCounter: DeltaTracker, ci: CallbackInfo) {
        HUDManager.render(context)
    }
}
