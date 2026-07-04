package com.gan.client.resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public final class PreviewSkin {

    private static File cachedFile;
    private static long cachedLastModified = -1L;
    private static BufferedImage image;

    private PreviewSkin() {
    }

    public static void load(File file) {
        if (file == null || !file.exists()) {
            clear();
            return;
        }

        long lm = file.lastModified();
        if (file.equals(cachedFile) && lm == cachedLastModified && image != null) {
            return;
        }

        try {
            BufferedImage read = ImageIO.read(file);
            if (read == null) {
                clear();
                TextureLoader.STATUS = "Preview failed: unreadable PNG";
                return;
            }

            cachedFile = file;
            cachedLastModified = lm;
            image = read;
            TextureLoader.STATUS = "Preview ready: " + file.getName() + " (" + read.getWidth() + "x" + read.getHeight() + ")";
        } catch (Exception e) {
            clear();
            TextureLoader.STATUS = "Preview failed: " + e.getClass().getSimpleName();
        }
    }

    public static void clear() {
        cachedFile = null;
        cachedLastModified = -1L;
        image = null;
    }

    public static boolean hasImage() {
        return image != null;
    }

    public static int width() {
        return image == null ? 0 : image.getWidth();
    }

    public static int height() {
        return image == null ? 0 : image.getHeight();
    }

    public static int pixel(int x, int y) {
        if (image == null) {
            return 0;
        }

        int w = image.getWidth();
        int h = image.getHeight();

        if (w <= 0 || h <= 0) {
            return 0;
        }

        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x >= w) x = w - 1;
        if (y >= h) y = h - 1;

        return image.getRGB(x, y);
    }
}