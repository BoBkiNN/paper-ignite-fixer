package xyz.bobkinn.ignite_fixer;

import io.papermc.paper.util.MappingEnvironment;
import net.fabricmc.tinyremapper.*;
import net.fabricmc.tinyremapper.extension.mixin.MixinExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.Locale;
import java.util.Map;
import java.util.jar.Manifest;

public class RemapUtil {
    public static boolean ENABLE_CUSTOM_REMAP = Boolean.parseBoolean(
            System.getProperty("ignite.AlternateRemapping", "true"));

    public static boolean ENABLE_CUSTOM_SERVER_REMAP = Boolean.parseBoolean(
            System.getProperty("ignite.AlternateServerRemapping", "false")); // TODO fix, currently disabled

    public static final Logger LOGGER = LoggerFactory.getLogger("PluginRemapper");

    public static final String MOJANG_YARN = "mojang+yarn";
    public static final String SPIGOT = "spigot";

    public static BufferedReader getMappings(){
        // tiny	2	0	left    right
        var name = MappingEnvironment.mappingsHash().toUpperCase(Locale.ROOT)+".tiny";
        var f = new File("").toPath().resolve("plugins").resolve(".paper-remapped")
                .resolve("mappings")
                .resolve("reversed").resolve(name);
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(f.toFile())));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedReader getBundledMappings(){
        // tiny	2	0	mojang+yarn	spigot
        try {
            return new BufferedReader(new InputStreamReader(MappingEnvironment.mappingsStream()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to find bundled mappings", e);
        }
    }

    private static Manifest readManifest(FileSystem fs){
        var path = fs.getPath("META-INF", "MANIFEST.MF");
        try (var s = Files.newInputStream(path)) {
            return new Manifest(s);
        } catch (Exception e) {
            return new Manifest();
        }
    }

    private static void saveManifest(FileSystem fs, Manifest mf){
        var path = fs.getPath("META-INF", "MANIFEST.MF");
        try (var s = Files.newOutputStream(path)) {
            mf.write(s);
        } catch (NoSuchFileException ignored){
        } catch (Exception e) {
            throw new RuntimeException("Failed to save manifest", e);
        }
    }

    public static void addNamespaceManifestAttribute(Path output, String value){
        var uri = URI.create("jar:"+output.toUri());
        try (var fs = FileSystems.newFileSystem(uri, Map.of("create", "true"))) {
            var mf = readManifest(fs);
            var attr = mf.getMainAttributes();
            attr.putValue("paperweight-mappings-namespace", value);
            saveManifest(fs, mf);
        } catch (Exception e){
            throw new RuntimeException("Failed to add manifest entry", e);
        }
    }

    public static void remap(Path input, Path output, Path reobfServer){
        var fromNs = "left";
        var toNs = "right";
        var mappings = TinyUtils.createTinyMappingProvider(getMappings(), fromNs, toNs);
        var remapper = TinyRemapper.newRemapper()
                .withMappings(mappings)
                .threads(1)
                .extension(new MixinExtension())
                .ignoreConflicts(true)
                .build();
        try (OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(output).build()) {
            outputConsumer.addNonClassFiles(input, NonClassCopyMode.FIX_META_INF, remapper);

            remapper.readInputs(input);
            remapper.readClassPath(reobfServer);

            remapper.apply(outputConsumer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to remap", e);
        } finally {
            remapper.finish();
        }
        addNamespaceManifestAttribute(output, MOJANG_YARN);
    }

    public static void remapServer(Path input, Path output){
        var mappings = TinyUtils.createTinyMappingProvider(getBundledMappings(), MOJANG_YARN, SPIGOT);
        var remapper = TinyRemapper.newRemapper()
                .withMappings(mappings)
                .threads(1)
                .extension(new MixinExtension())
                .ignoreConflicts(true)
                .build();
        try (OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(output)
                .assumeArchive(true).build()) {
            outputConsumer.addNonClassFiles(input, NonClassCopyMode.UNCHANGED, remapper);

            remapper.readInputs(input);

            remapper.apply(outputConsumer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to remap", e);
        } finally {
            remapper.finish();
        }
        addNamespaceManifestAttribute(output, SPIGOT);
    }
}
