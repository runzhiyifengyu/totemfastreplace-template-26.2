package com.gan.client.resource;

import java.io.File;
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
        TEXTURES.clear();

        if (!CONFIG_DIR.exists() && !CONFIG_DIR.mkdirs()) {
            STATUS = "Cannot create folder: " + CONFIG_DIR.getPath();
            return;
        }

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
}