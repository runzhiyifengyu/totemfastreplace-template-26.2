package com.gan.client.render;

import com.gan.client.gui.PreviewDollRenderer;
import com.gan.client.model.BlockbenchLoader;
import com.gan.client.model.BlockbenchModel;
import com.gan.client.resource.PreviewSkin;
import com.gan.client.resource.TextureLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.io.File;

public final class PreviewRenderer {

    private static BlockbenchModel model;
    private static float yawDeg;
    private static float pitchDeg;
    private static boolean initialized;

    private PreviewRenderer() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        model = BlockbenchLoader.loadDefault();
        if (model == null) {
            model = new BlockbenchModel();
        }

        if (TextureLoader.SELECTED != null) {
            PreviewSkin.load(TextureLoader.SELECTED);
        }
    }

    public static void setSkin(File file) {
        TextureLoader.SELECTED = file;
        PreviewSkin.load(file);
    }

    public static void setModel(BlockbenchModel newModel) {
        if (newModel != null) {
            model = newModel;
        }
    }

    public static void mouseDragged(double deltaX, double deltaY) {
        yawDeg += (float) deltaX * 0.8f;
        pitchDeg -= (float) deltaY * 0.6f;

        if (pitchDeg > 45.0f) {
            pitchDeg = 45.0f;
        }
        if (pitchDeg < -45.0f) {
            pitchDeg = -45.0f;
        }
    }

    public static void resetRotation() {
        yawDeg = 0.0f;
        pitchDeg = 0.0f;
    }

    public static void render(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        if (!initialized) {
            init();
        }

        PreviewDollRenderer.render(graphics, x, y, width, height, yawDeg, pitchDeg, model);
    }
}