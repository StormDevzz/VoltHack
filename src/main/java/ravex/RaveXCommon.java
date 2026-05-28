package ravex;

import net.fabricmc.api.ModInitializer;
import ravex.utility.sound.SoundUtility;

/**
 * Common (server+client) initializer — runs BEFORE registries are frozen.
 * We register custom SoundEvents here so they appear in BuiltInRegistries.SOUND_EVENT
 * before the registry is locked. onInitializeClient() runs too late for this.
 */
public class RaveXCommon implements ModInitializer {
    @Override
    public void onInitialize() {
        SoundUtility.register();
    }
}
