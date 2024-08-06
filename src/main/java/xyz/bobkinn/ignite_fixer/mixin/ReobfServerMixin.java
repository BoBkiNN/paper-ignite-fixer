package xyz.bobkinn.ignite_fixer.mixin;

import net.neoforged.art.internal.RenamerImpl;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.configurate.util.CheckedConsumer;
import xyz.bobkinn.ignite_fixer.RemapUtil;

import java.io.File;
import java.nio.file.Path;

@Mixin(targets = "io.papermc.paper.pluginremap.ReobfServer")
public class ReobfServerMixin {

    /**
     * injects in {@link io.papermc.paper.util.AtomicFiles#atomicWrite(Path, CheckedConsumer)}'s lambda argument
     * @param instance remapper
     * @param input input file
     * @param output output file
     * @param remappingSelf true if remapping self
     */
    @Redirect(method = "lambda$remap$2", at= @At(value = "INVOKE", target = "Lnet/neoforged/art/internal/RenamerImpl;run(Ljava/io/File;Ljava/io/File;Z)V"))
    private static void onRemap(RenamerImpl instance, File input, File output, boolean remappingSelf){
        if (!RemapUtil.ENABLE_CUSTOM_SERVER_REMAP) {
            instance.run(input, output, remappingSelf);
        } else {
            RemapUtil.remapServer(input.toPath(), output.toPath());
        }
    }

    @Unique
    private static @NotNull Path ignite_fixer$findPath(){
        var pJar = System.getProperty("ignite.paper.jar");
        var jar = System.getProperty("ignite.jar");
        for (var name : new String[]{pJar, jar, "server.jar", "paper.jar"}){
            if (name != null){
                var f = Path.of(name).toAbsolutePath();
                if (f.toFile().isFile()){
                    return f;
                }
            }
        }
        throw new IllegalArgumentException("Failed to find server jar file");
    }

    @Inject(method = "serverJar", at=@At("HEAD"), cancellable = true)
    private static void injectServerJar(@NotNull CallbackInfoReturnable<Path> cir){
        var path = ignite_fixer$findPath();
        cir.setReturnValue(path);
        cir.cancel();
    }
}
