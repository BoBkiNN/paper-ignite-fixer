package xyz.bobkinn.ignite_fixer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bobkinn.ignite_fixer.RemapUtil;

import java.nio.file.Path;

@Mixin(targets = "io.papermc.paper.pluginremap.PluginRemapper")
public class MixinPluginRemapper {

    @Inject(method = "lambda$remap$7", at = @At(value = "INVOKE", target = "Ljava/lang/System;currentTimeMillis()J", ordinal = 0), cancellable = true)
    public void onRemap(boolean library, Path inputFile, Path destination, Path reobfServer, CallbackInfoReturnable<Path> cir){
        final long start = System.currentTimeMillis();
        if (!RemapUtil.ENABLE_CUSTOM_REMAP) {
            return;
        }
        try {
            RemapUtil.remap(inputFile, destination, reobfServer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to remap "+(library ? "library" : "plugin")+" jar '" + inputFile + "'", e);
        }
        RemapUtil.LOGGER.info("Done remapping {} '{}' in {}ms.", library ? "library" : "plugin", inputFile, System.currentTimeMillis() - start);
        cir.setReturnValue(destination);
        cir.cancel();
    }
}
