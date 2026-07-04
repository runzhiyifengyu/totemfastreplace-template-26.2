package com.gan.client.gui;

import com.gan.client.model.BlockbenchCube;
import com.gan.client.model.BlockbenchFace;
import com.gan.client.model.BlockbenchModel;
import com.gan.client.model.BlockbenchRotation;
import com.gan.client.resource.PreviewSkin;
import com.gan.client.resource.TextureLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class PreviewDollRenderer {

    private PreviewDollRenderer() {
    }

    private static final class Vec3 {
        final float x;
        final float y;
        final float z;

        Vec3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static final class FaceDraw {
        final String side;
        final BlockbenchCube cube;
        final Vec3[] world;
        final float depth;

        FaceDraw(String side, BlockbenchCube cube, Vec3[] world, float depth) {
            this.side = side;
            this.cube = cube;
            this.world = world;
            this.depth = depth;
        }
    }

    public static void render(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                              float yawDeg, float pitchDeg, BlockbenchModel model) {
        PreviewSkin.load(TextureLoader.SELECTED);

        if (!PreviewSkin.hasImage()) {
            graphics.text(null, "No preview", x + width / 2 - 28, y + height / 2, 0xFFFFFFFF, true);
            return;
        }

        if (model == null || model.getCubes().isEmpty()) {
            graphics.text(null, "No model", x + width / 2 - 24, y + height / 2, 0xFFFFFFFF, true);
            return;
        }

        int centerX = x + width / 2;
        int centerY = y + height / 2 + 8;

        drawShadow(graphics, centerX, centerY + 72);

        float[] bounds = modelBounds(model);
        float pivotX = (bounds[0] + bounds[3]) * 0.5f;
        float pivotY = (bounds[1] + bounds[4]) * 0.5f;
        float pivotZ = (bounds[2] + bounds[5]) * 0.5f;

        float yaw = (float) Math.toRadians(yawDeg);
        float pitch = (float) Math.toRadians(pitchDeg);

        int scale = Math.max(5, Math.min(width, height) / 32);
        float cameraDistance = 42.0f;

        List<FaceDraw> faces = new ArrayList<>();
        for (BlockbenchCube cube : model.getCubes()) {
            addCubeFaces(faces, cube, pivotX, pivotY, pivotZ, yaw, pitch, centerX, centerY, scale, cameraDistance);
        }

        // 远的先画，近的后画
        faces.sort(Comparator.comparingDouble((FaceDraw f) -> f.depth).reversed());

        for (FaceDraw face : faces) {
            drawFace(graphics, face);
        }
    }

    private static void addCubeFaces(
            List<FaceDraw> out,
            BlockbenchCube cube,
            float pivotX,
            float pivotY,
            float pivotZ,
            float yaw,
            float pitch,
            int centerX,
            int centerY,
            int scale,
            float cameraDistance
    ) {
        if (cube == null) {
            return;
        }

        float minX = Math.min(cube.fromX, cube.toX);
        float minY = Math.min(cube.fromY, cube.toY);
        float minZ = Math.min(cube.fromZ, cube.toZ);
        float maxX = Math.max(cube.fromX, cube.toX);
        float maxY = Math.max(cube.fromY, cube.toY);
        float maxZ = Math.max(cube.fromZ, cube.toZ);

        String[] sides = {"north", "south", "east", "west", "up", "down"};
        for (String side : sides) {
            BlockbenchFace face = cube.face(side);
            if (face == null) {
                continue;
            }

            Vec3[] corners = faceCorners(side, minX, minY, minZ, maxX, maxY, maxZ);
            if (corners == null) {
                continue;
            }

            Vec3[] world = new Vec3[corners.length];
            float depthSum = 0.0f;

            for (int i = 0; i < corners.length; i++) {
                Vec3 p = corners[i];
                Vec3 rotated = rotateElementPoint(p, cube.rotation);
                Vec3 local = new Vec3(rotated.x - pivotX, rotated.y - pivotY, rotated.z - pivotZ);
                Vec3 turned = rotateYawPitch(local, yaw, pitch);

                float zCam = turned.z + cameraDistance;
                float perspective = cameraDistance / Math.max(8.0f, zCam);

                float sx = centerX + turned.x * scale * perspective;
                float sy = centerY + turned.y * scale * perspective;

                world[i] = new Vec3(sx, sy, zCam);
                depthSum += zCam;
            }

            float avgDepth = depthSum / corners.length;
            out.add(new FaceDraw(side, cube, world, avgDepth));
        }
    }

    private static Vec3[] faceCorners(String side, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return switch (side.toLowerCase(Locale.ROOT)) {
            case "north" -> new Vec3[]{
                    new Vec3(minX, minY, minZ),
                    new Vec3(maxX, minY, minZ),
                    new Vec3(maxX, maxY, minZ),
                    new Vec3(minX, maxY, minZ)
            };
            case "south" -> new Vec3[]{
                    new Vec3(maxX, minY, maxZ),
                    new Vec3(minX, minY, maxZ),
                    new Vec3(minX, maxY, maxZ),
                    new Vec3(maxX, maxY, maxZ)
            };
            case "east" -> new Vec3[]{
                    new Vec3(maxX, minY, minZ),
                    new Vec3(maxX, minY, maxZ),
                    new Vec3(maxX, maxY, maxZ),
                    new Vec3(maxX, maxY, minZ)
            };
            case "west" -> new Vec3[]{
                    new Vec3(minX, minY, maxZ),
                    new Vec3(minX, minY, minZ),
                    new Vec3(minX, maxY, minZ),
                    new Vec3(minX, maxY, maxZ)
            };
            case "up" -> new Vec3[]{
                    new Vec3(minX, maxY, minZ),
                    new Vec3(maxX, maxY, minZ),
                    new Vec3(maxX, maxY, maxZ),
                    new Vec3(minX, maxY, maxZ)
            };
            case "down" -> new Vec3[]{
                    new Vec3(minX, minY, maxZ),
                    new Vec3(maxX, minY, maxZ),
                    new Vec3(maxX, minY, minZ),
                    new Vec3(minX, minY, minZ)
            };
            default -> null;
        };
    }

    private static Vec3 rotateElementPoint(Vec3 p, BlockbenchRotation rotation) {
        if (rotation == null) {
            return p;
        }

        float ox = rotation.originX;
        float oy = rotation.originY;
        float oz = rotation.originZ;
        float x = p.x - ox;
        float y = p.y - oy;
        float z = p.z - oz;
        double rad = Math.toRadians(rotation.angle);

        String axis = rotation.axis == null ? "y" : rotation.axis.toLowerCase(Locale.ROOT);
        return switch (axis) {
            case "x" -> {
                float ny = (float) (y * Math.cos(rad) - z * Math.sin(rad));
                float nz = (float) (y * Math.sin(rad) + z * Math.cos(rad));
                yield new Vec3(p.x, ny + oy, nz + oz);
            }
            case "y" -> {
                float nx = (float) (x * Math.cos(rad) + z * Math.sin(rad));
                float nz = (float) (-x * Math.sin(rad) + z * Math.cos(rad));
                yield new Vec3(nx + ox, p.y, nz + oz);
            }
            case "z" -> {
                float nx = (float) (x * Math.cos(rad) - y * Math.sin(rad));
                float ny = (float) (x * Math.sin(rad) + y * Math.cos(rad));
                yield new Vec3(nx + ox, ny + oy, p.z);
            }
            default -> p;
        };
    }

    private static Vec3 rotateYawPitch(Vec3 p, float yaw, float pitch) {
        float x1 = (float) (p.x * Math.cos(yaw) - p.z * Math.sin(yaw));
        float z1 = (float) (p.x * Math.sin(yaw) + p.z * Math.cos(yaw));
        float y1 = (float) (p.y * Math.cos(pitch) - z1 * Math.sin(pitch));
        float z2 = (float) (p.y * Math.sin(pitch) + z1 * Math.cos(pitch));
        return new Vec3(x1, y1, z2);
    }

    private static float[] modelBounds(BlockbenchModel model) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        for (BlockbenchCube cube : model.getCubes()) {
            minX = Math.min(minX, Math.min(cube.fromX, cube.toX));
            minY = Math.min(minY, Math.min(cube.fromY, cube.toY));
            minZ = Math.min(minZ, Math.min(cube.fromZ, cube.toZ));
            maxX = Math.max(maxX, Math.max(cube.fromX, cube.toX));
            maxY = Math.max(maxY, Math.max(cube.fromY, cube.toY));
            maxZ = Math.max(maxZ, Math.max(cube.fromZ, cube.toZ));
        }

        if (!Float.isFinite(minX)) {
            return new float[]{0, 0, 0, 16, 16, 16};
        }

        return new float[]{minX, minY, minZ, maxX, maxY, maxZ};
    }

    private static void drawFace(GuiGraphicsExtractor graphics, FaceDraw face) {
        BlockbenchFace meta = face.cube.face(face.side);
        if (meta == null) {
            return;
        }

        int u1 = Math.round(Math.min(meta.u1, meta.u2));
        int v1 = Math.round(Math.min(meta.v1, meta.v2));
        int u2 = Math.round(Math.max(meta.u1, meta.u2));
        int v2 = Math.round(Math.max(meta.v1, meta.v2));

        int avgColor = sampleAverageColor(u1, v1, u2, v2);
        int shaded = shade(avgColor, faceBrightness(face.side));

        fillQuad(graphics, face.world[0], face.world[1], face.world[2], face.world[3], shaded);
    }

    private static int sampleAverageColor(int u1, int v1, int u2, int v2) {
        int texW = Math.max(1, u2 - u1);
        int texH = Math.max(1, v2 - v1);

        int stepX = Math.max(1, texW / 6);
        int stepY = Math.max(1, texH / 6);

        long r = 0;
        long g = 0;
        long b = 0;
        long a = 0;
        long count = 0;

        for (int y = 0; y < texH; y += stepY) {
            for (int x = 0; x < texW; x += stepX) {
                int argb = PreviewSkin.pixel(u1 + x, v1 + y);
                int aa = (argb >>> 24) & 0xFF;
                if (aa == 0) {
                    continue;
                }

                a += aa;
                r += (argb >>> 16) & 0xFF;
                g += (argb >>> 8) & 0xFF;
                b += argb & 0xFF;
                count++;
            }
        }

        if (count <= 0) {
            return 0xFFFFFFFF;
        }

        int ia = (int) (a / count);
        int ir = (int) (r / count);
        int ig = (int) (g / count);
        int ib = (int) (b / count);

        return (ia << 24) | (ir << 16) | (ig << 8) | ib;
    }

    private static float faceBrightness(String side) {
        return switch (side.toLowerCase(Locale.ROOT)) {
            case "up" -> 1.12f;
            case "north" -> 0.96f;
            case "south" -> 0.92f;
            case "east" -> 0.86f;
            case "west" -> 0.88f;
            case "down" -> 0.75f;
            default -> 0.90f;
        };
    }

    private static int shade(int argb, float brightness) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        r = clamp((int) (r * brightness));
        g = clamp((int) (g * brightness));
        b = clamp((int) (b * brightness));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void fillQuad(GuiGraphicsExtractor graphics, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, int color) {
        float minY = Math.min(Math.min(p0.y, p1.y), Math.min(p2.y, p3.y));
        float maxY = Math.max(Math.max(p0.y, p1.y), Math.max(p2.y, p3.y));

        int yStart = (int) Math.floor(minY);
        int yEnd = (int) Math.ceil(maxY);

        for (int y = yStart; y <= yEnd; y++) {
            float[] xs = new float[4];
            int n = 0;

            n = addIntersection(xs, n, p0, p1, y);
            n = addIntersection(xs, n, p1, p2, y);
            n = addIntersection(xs, n, p2, p3, y);
            n = addIntersection(xs, n, p3, p0, y);

            if (n < 2) {
                continue;
            }

            Arrays.sort(xs, 0, n);

            for (int i = 0; i + 1 < n; i += 2) {
                int x1 = (int) Math.floor(xs[i]);
                int x2 = (int) Math.ceil(xs[i + 1]);

                if (x2 > x1) {
                    graphics.fill(x1, y, x2, y + 1, color);
                }
            }
        }
    }

    private static int addIntersection(float[] xs, int n, Vec3 a, Vec3 b, int y) {
        float y1 = a.y;
        float y2 = b.y;

        if (y1 == y2) {
            return n;
        }

        float min = Math.min(y1, y2);
        float max = Math.max(y1, y2);

        if (y < min || y >= max) {
            return n;
        }

        float t = (y - y1) / (y2 - y1);
        xs[n++] = a.x + (b.x - a.x) * t;
        return n;
    }

    private static void drawShadow(GuiGraphicsExtractor graphics, int centerX, int y) {
        graphics.fill(centerX - 54, y, centerX + 54, y + 8, 0x22000000);
        graphics.fill(centerX - 42, y - 2, centerX + 42, y + 5, 0x16000000);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}