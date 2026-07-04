package com.gan.client.resource;

import com.gan.TotemFastReplace;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TextureLoader {

    public static final File CONFIG_DIR = new File("config/totemfastreplace/custom-totem-plush");
    public static final File RESOURCE_PACK_ZIP = new File("resourcepacks/custom-totem-plush.zip");

    public static final List<File> TEXTURES = new ArrayList<>();
    public static File SELECTED = null;
    public static String STATUS = "Ready";

    private TextureLoader() {
    }

    public static void load() {
        ensureDirectories();
        ensureBundledResourcePack();

        TEXTURES.clear();

        File[] files = CONFIG_DIR.listFiles();
        if (files == null) {
            STATUS = "No files found";
            return;
        }

        Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        for (File f : files) {
            if (f.isFile() && f.getName().toLowerCase().endsWith(".png")) {
                TEXTURES.add(f);
            }
        }

        STATUS = TEXTURES.isEmpty()
                ? "No PNG found"
                : "Loaded " + TEXTURES.size() + " PNG(s)";
    }

    private static void ensureDirectories() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }

        File parent = RESOURCE_PACK_ZIP.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    private static void ensureBundledResourcePack() {
        if (RESOURCE_PACK_ZIP.exists()) {
            return;
        }

        try (InputStream in = TotemFastReplace.class.getResourceAsStream("/defaults/custom-totem-plush.zip")) {
            if (in == null) {
                STATUS = "Missing bundled resource pack";
                return;
            }
            Files.copy(in, RESOURCE_PACK_ZIP.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            STATUS = "Cannot create zip: " + e.getClass().getSimpleName();
        }
    }
}