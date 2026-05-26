package volthack.mixin.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinNoWeb {
    /**
     * makeStuckInBlock is called every tick when an entity overlaps a block that
     * slows movement (cobweb, sweet berry bush, powder snow, etc.).
     * Cancelling the call prevents the speed reduction from being applied.
     */
    @Inject(method = "makeStuckInBlock", at = @At("HEAD"), cancellable = true)
    private void onMakeStuckInBlock(BlockState state, Vec3 motionMultiplier, CallbackInfo ci) {
        if (volthack.modules.movement.NoWeb.INSTANCE.getEnabled()) {
            // Only cancel for cobweb (spider web) blocks
            net.minecraft.resources.Identifier blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (blockId != null && blockId.getPath().contains("cobweb")) {
                ci.cancel();
            }
        }
    }
}
