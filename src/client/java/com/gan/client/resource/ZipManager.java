package com.gan.client.resource;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ZipManager {

    private ZipManager() {
    }

    public static void applySelected() {
        File selected = TextureLoader.SELECTED;

        if (selected == null) {
            TextureLoader.STATUS = "Select a PNG first";
            return;
        }

        File zipFile = TextureLoader.RESOURCE_PACK_ZIP;

        if (!zipFile.exists()) {
            TextureLoader.STATUS = "Missing zip: " + zipFile.getPath();
            return;
        }

        try (FileSystem fs = FileSystems.newFileSystem(zipFile.toPath(), (ClassLoader) null)) {
            copyIntoZip(fs, selected.toPath(), "skin.png");

            TextureLoader.STATUS = "Applied: " + selected.getName();
            boolean reloaded = ResourceReloader.reload();
            if (!reloaded) {
                TextureLoader.STATUS = TextureLoader.STATUS + " (press F3+T)";
            }
        } catch (Exception e) {
            TextureLoader.STATUS = "Apply failed: " + e.getClass().getSimpleName();
        }
    }

    private static void copyIntoZip(FileSystem fs, Path source, String fileName) throws Exception {
        Path target = fs.getPath("assets", "minecraft", "textures", "item", fileName);

        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }

        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
}