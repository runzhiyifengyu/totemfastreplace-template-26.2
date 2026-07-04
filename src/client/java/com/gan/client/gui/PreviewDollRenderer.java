package com.gan.client.gui;

import com.gan.client.resource.PreviewSkin;
import com.gan.client.resource.TextureLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class PreviewDollRenderer {

    private PreviewDollRenderer() {
    }

    private static final class Part {
        final int x;
        final int y;
        final int z;
        final int w;
        final int h;
        final int srcX;
        final int srcY;
        final int srcW;
        final int srcH;
        final int scale;
        final float brightness;

        Part(int x, int y, int z, int w, int h, int srcX, int srcY, int srcW, int srcH, int scale, float brightness) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
            this.h = h;
            this.srcX = srcX;
            this.srcY = srcY;
            this.srcW = srcW;
            this.srcH = srcH;
            this.scale = scale;
            this.brightness = brightness;
        }
    }

    // 按你给的 JSON 结构写出来的部件
    private static final Part[] PARTS = new Part[]{
            // head
            new Part(4, 8, 6, 8, 8, 0, 0, 8, 8, 4, 1.00f),
            // head overlay
            new Part(4, 8, 6, 8, 8, 32, 8, 8, 8, 4, 0.86f),

            // body
            new Part(4, 0, 6, 8, 12, 20, 20, 8, 12, 4, 1.00f),
            // body overlay
            new Part(4, 0, 6, 8, 12, 20, 36, 8, 12, 4, 0.88f),

            // left arm
            new Part(1, 1, 9, 3, 12, 36, 52, 4, 12, 4, 0.96f),
            // right arm
            new Part(12, 1, 9, 3, 12, 44, 20, 4, 12, 4, 0.96f),

            // left leg
            new Part(3, 8, 6, 4, 8, 20, 52, 4, 12, 4, 0.96f),
            // right leg
            new Part(9, 8, 6, 4, 8, 4, 20, 4, 12, 4, 0.96f),
    };

    public static void render(GuiGraphicsExtractor graphics, int x, int y, int width, int height, float yawDeg, float pitchDeg) {
        PreviewSkin.load(TextureLoader.SELECTED);

        if (!PreviewSkin.hasImage()) {
            graphics.text(null, "No preview", x + width / 2 - 28, y + height / 2, 0xFFFFFFFF, true);
            return;
        }

        // 模型中心点：按你 JSON 的 16x16x16 体积大致取中间
        float pivotX = 8.0f;
        float pivotY = 8.0f;
        float pivotZ = 8.0f;

        // 预览放大倍数
        int modelScale = 5;

        // 预览中心位置
        int centerX = x + width / 2;
        int centerY = y + height / 2 + 10;

        // 鼠标旋转带来的整体偏移
        double yaw = Math.toRadians(yawDeg);
        double pitch = Math.toRadians(pitchDeg);

        // 先画一个轻微阴影，不是相框
        drawShadow(graphics, centerX, centerY + 72);

        // 按照同一个 pivot 整体旋转
        for (Part p : PARTS) {
            drawRotatedPart(
                    graphics,
                    centerX,
                    centerY,
                    p,
                    pivotX,
                    pivotY,
                    pivotZ,
                    yaw,
                    pitch,
                    modelScale
            );
        }
    }

    private static void drawRotatedPart(
            GuiGraphicsExtractor graphics,
            int screenCenterX,
            int screenCenterY,
            Part part,
            float pivotX,
            float pivotY,
            float pivotZ,
            double yaw,
            double pitch,
            int modelScale
    ) {
        // 部件中心点
        float cx = part.x + part.w / 2.0f;
        float cy = part.y + part.h / 2.0f;
        float cz = part.z + part.w / 2.0f;

        // 相对 pivot
        float rx = cx - pivotX;
        float ry = cy - pivotY;
        float rz = cz - pivotZ;

        // Y 轴旋转（鼠标左右拖动）
        float x1 = (float) (rx * Math.cos(yaw) - rz * Math.sin(yaw));
        float z1 = (float) (rx * Math.sin(yaw) + rz * Math.cos(yaw));

        // X 轴旋转（鼠标上下拖动）
        float y1 = (float) (ry * Math.cos(pitch) - z1 * Math.sin(pitch));
        float z2 = (float) (ry * Math.sin(pitch) + z1 * Math.cos(pitch));

        // 投影到屏幕
        int sx = screenCenterX + Math.round(x1 * modelScale);
        int sy = screenCenterY + Math.round(y1 * modelScale - z2 * 0.35f * modelScale);

        // 立方体厚度感
        int drawW = Math.max(1, part.w * part.scale);
        int drawH = Math.max(1, part.h * part.scale);

        // 轻微透视：越靠前越亮
        float depth = 1.0f - Math.min(0.25f, Math.max(-0.15f, z2 * 0.02f));
        float b = part.brightness * depth;

        // 把每个 part 当成一个局部块画出来
        drawBlock(graphics, sx, sy, drawW, drawH, part.srcX, part.srcY, part.srcW, part.srcH, part.scale, b, yaw);
    }

    private static void drawBlock(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int w,
            int h,
            int srcX,
            int srcY,
            int srcW,
            int srcH,
            int scale,
            float brightness,
            double yaw
    ) {
        // 统一朝向，不再对每个部件单独“乱偏”
        int sideShift = (int) Math.round(Math.sin(yaw) * 4.0);
        int topLift = 2;

        // 侧面暗层
        drawShearedRegion(graphics, x + w / 2 + sideShift, y + 1, srcX, srcY, srcW, srcH, scale, 3, brightness * 0.72f);

        // 顶部亮层
        drawShearedRegion(graphics, x + 1, y - topLift, srcX, srcY, srcW, srcH, scale, -1, brightness * 1.08f);

        // 正面
        drawScaledRegion(graphics, x, y, srcX, srcY, srcW, srcH, scale, brightness);
    }

    private static void drawScaledRegion(
            GuiGraphicsExtractor graphics,
            int dstX,
            int dstY,
            int srcX,
            int srcY,
            int srcW,
            int srcH,
            int scale,
            float brightness
    ) {
        for (int sy = 0; sy < srcH; sy++) {
            for (int sx = 0; sx < srcW; sx++) {
                int argb = PreviewSkin.pixel(srcX + sx, srcY + sy);
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) {
                    continue;
                }

                int tinted = tint(argb, brightness);
                int px = dstX + sx * scale;
                int py = dstY + sy * scale;
                graphics.fill(px, py, px + scale, py + scale, tinted);
            }
        }
    }

    private static void drawShearedRegion(
            GuiGraphicsExtractor graphics,
            int dstX,
            int dstY,
            int srcX,
            int srcY,
            int srcW,
            int srcH,
            int scale,
            int shear,
            float brightness
    ) {
        for (int sy = 0; sy < srcH; sy++) {
            int rowShift = (int) ((float) sy / Math.max(1, srcH - 1) * shear);

            for (int sx = 0; sx < srcW; sx++) {
                int argb = PreviewSkin.pixel(srcX + sx, srcY + sy);
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) {
                    continue;
                }

                int tinted = tint(argb, brightness);
                int px = dstX + sx * scale + rowShift;
                int py = dstY + sy * scale;
                graphics.fill(px, py, px + scale, py + scale, tinted);
            }
        }
    }

    private static void drawShadow(GuiGraphicsExtractor graphics, int centerX, int y) {
        graphics.fill(centerX - 54, y, centerX + 54, y + 8, 0x22000000);
        graphics.fill(centerX - 42, y - 2, centerX + 42, y + 5, 0x16000000);
    }

    private static int tint(int argb, float brightness) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        r = clamp((int) (r * brightness));
        g = clamp((int) (g * brightness));
        b = clamp((int) (b * brightness));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}