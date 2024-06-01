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
