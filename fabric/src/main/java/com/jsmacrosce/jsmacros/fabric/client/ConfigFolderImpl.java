package com.jsmacrosce.jsmacros.fabric.client;

import net.fabricmc.loader.api.FabricLoader;
import com.jsmacrosce.jsmacros.client.ConfigFolder;
import com.jsmacrosce.jsmacros.client.JsMacros;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

/**
 * McMacros keeps its macros and settings in {@code config/McMacros}.
 *
 * <p>People switching from JsMacros CE already have their macros in {@code config/jsMacros}.
 * The first time McMacros starts, and only if {@code config/McMacros} does not exist yet, that
 * folder is copied over so their macros and triggers keep working without any manual steps.
 * The old folder is left untouched.</p>
 */
public class ConfigFolderImpl implements ConfigFolder {
    private static final String FOLDER_NAME = "McMacros";
    private static final String LEGACY_FOLDER_NAME = "jsMacros";

    private static File folder;

    @Override
    public File getFolder() {
        if (folder == null) {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Path target = configDir.resolve(FOLDER_NAME);
            Path legacy = configDir.resolve(LEGACY_FOLDER_NAME);
            if (!Files.exists(target) && Files.isDirectory(legacy)) {
                migrate(legacy, target);
            }
            folder = target.toFile();
        }
        return folder;
    }

    private static void migrate(Path from, Path to) {
        try (Stream<Path> paths = Files.walk(from)) {
            for (Path src : (Iterable<Path>) paths::iterator) {
                Path dest = to.resolve(from.relativize(src).toString());
                if (Files.isDirectory(src)) {
                    Files.createDirectories(dest);
                } else {
                    Files.copy(src, dest, StandardCopyOption.COPY_ATTRIBUTES);
                }
            }
            JsMacros.LOGGER.info("Copied macros from config/{} to config/{}", LEGACY_FOLDER_NAME, FOLDER_NAME);
        } catch (IOException e) {
            JsMacros.LOGGER.error("Could not copy macros from config/{}", LEGACY_FOLDER_NAME, e);
        }
    }

}
