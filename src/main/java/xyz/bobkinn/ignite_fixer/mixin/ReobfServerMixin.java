package xyz.bobkinn.ignite_fixer.mixin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;

@Mixin(targets = "io.papermc.paper.pluginremap.ReobfServer")
public class ReobfServerMixin {

    @Unique
    private static @NotNull Path ignite_fixer$fixPath(String file){
        return Path.of(file).toAbsolutePath();
    }

    @Unique
    private static @NotNull Path ignite_fixer$findPath(){
        var pJar = System.getProperty("ignite.paper.jar");
        var jar = System.getProperty("ignite.jar");
        if (pJar != null){
            return ignite_fixer$fixPath(pJar);
        }
        if (jar != null){
            return ignite_fixer$fixPath(jar);
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
