package com.gan.client.gui;

import com.gan.client.resource.PreviewSkin;
import com.gan.client.resource.TextureLoader;
import com.gan.client.resource.ZipManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;

public class TotemScreen extends Screen {

    private static final int PAGE_SIZE = 10;

    private final int requestedPage;
    private int currentPage;

    private boolean draggingPreview = false;
    private double lastMouseX;
    private double lastMouseY;

    private float previewYaw = 0.0f;
    private float previewPitch = 0.0f;

    public TotemScreen() {
        this(0);
    }

    public TotemScreen(int page) {
        super(Component.literal("Totem Fast Replace"));
        this.requestedPage = page;
    }

    @Override
    protected void init() {
        TextureLoader.load();

        int totalPages = Math.max(1, (TextureLoader.TEXTURES.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        this.currentPage = Math.max(0, Math.min(this.requestedPage, totalPages - 1));

        int x = 20;
        int y = 34;
        int buttonWidth = 180;
        int buttonHeight = 20;
        int gap = 4;

        this.addRenderableWidget(
                Button.builder(Component.literal("Refresh"), btn -> openPage(this.currentPage))
                        .bounds(x, 10, 90, buttonHeight)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Apply Selected"), btn -> ZipManager.applySelected())
                        .bounds(x + 96, 10, 120, buttonHeight)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("< Prev"), btn -> openPage(this.currentPage - 1))
                        .bounds(x + 222, 10, 70, buttonHeight)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Next >"), btn -> openPage(this.currentPage + 1))
                        .bounds(x + 298, 10, 70, buttonHeight)
                        .build()
        );

        if (TextureLoader.TEXTURES.isEmpty()) {
            this.addRenderableWidget(
                    Button.builder(Component.literal("No PNG found"), btn -> {
                    }).bounds(x, y, buttonWidth, buttonHeight).build()
            );
            return;
        }

        int start = this.currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, TextureLoader.TEXTURES.size());
        int currentY = y;

        for (int i = start; i < end; i++) {
            final File file = TextureLoader.TEXTURES.get(i);
            final String name = file.getName();

            this.addRenderableWidget(
                    Button.builder(Component.literal(name), btn -> {
                        TextureLoader.SELECTED = file;
                        TextureLoader.STATUS = "Selected: " + name;
                        PreviewSkin.load(file);
                    }).bounds(x, currentY, buttonWidth, buttonHeight).build()
            );

            currentY += buttonHeight + gap;
        }

        if (TextureLoader.SELECTED != null) {
            PreviewSkin.load(TextureLoader.SELECTED);
        } else if (!TextureLoader.TEXTURES.isEmpty()) {
            TextureLoader.SELECTED = TextureLoader.TEXTURES.get(start);
            PreviewSkin.load(TextureLoader.SELECTED);
        }
    }

    private void openPage(int page) {
        Minecraft.getInstance().setScreenAndShow(new TotemScreen(page));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 只在右侧预览区域按住后才开始旋转
        if (button == 0) {
            int previewX = 220;
            int previewY = 82;
            int previewW = 260;
            int previewH = 250;

            if (mouseX >= previewX && mouseX <= previewX + previewW
                    && mouseY >= previewY && mouseY <= previewY + previewH) {
                this.draggingPreview = true;
                this.lastMouseX = mouseX;
                this.lastMouseY = mouseY;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.draggingPreview = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.draggingPreview && button == 0) {
            this.previewYaw += (float) deltaX * 0.8f;
            this.previewPitch -= (float) deltaY * 0.6f;

            if (this.previewPitch > 35.0f) this.previewPitch = 35.0f;
            if (this.previewPitch < -25.0f) this.previewPitch = -25.0f;

            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int totalPages = Math.max(1, (TextureLoader.TEXTURES.size() + PAGE_SIZE - 1) / PAGE_SIZE);

        graphics.text(this.font, "PNG Files", 20, 18, 0xFFFFFFFF, true);
        graphics.text(this.font, "Page: " + (this.currentPage + 1) + " / " + totalPages, 220, 18, 0xFFFFFFFF, true);

        String selected = TextureLoader.SELECTED == null
                ? "Selected: none"
                : "Selected: " + TextureLoader.SELECTED.getName();

        graphics.text(this.font, selected, 220, 34, 0xFFFFFFFF, true);
        graphics.text(this.font, "Status: " + TextureLoader.STATUS, 220, 50, 0xFFFFFFFF, true);

        // 右侧：纯图腾娃娃预览
        PreviewDollRenderer.render(graphics, 220, 82, 260, 250, this.previewYaw, this.previewPitch);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}