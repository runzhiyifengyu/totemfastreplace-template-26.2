package com.gan.client.gui;

import com.gan.client.render.PreviewRenderer;
import com.gan.client.resource.TextureLoader;
import com.gan.client.resource.ZipManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.io.File;

public class TotemScreen extends Screen {

    private static final int PAGE_SIZE = 10;

    private final int requestedPage;
    private int currentPage;

    private boolean draggingPreview = false;

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
        PreviewRenderer.init();

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
                        PreviewRenderer.setSkin(file);
                    }).bounds(x, currentY, buttonWidth, buttonHeight).build()
            );

            currentY += buttonHeight + gap;
        }

        if (TextureLoader.SELECTED != null) {
            PreviewRenderer.setSkin(TextureLoader.SELECTED);
        } else if (!TextureLoader.TEXTURES.isEmpty()) {
            TextureLoader.SELECTED = TextureLoader.TEXTURES.get(start);
            PreviewRenderer.setSkin(TextureLoader.SELECTED);
        }
    }

    private void openPage(int page) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreenAndShow(new TotemScreen(page));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean handled) {
        if (event.button() == 0) {
            int previewX = 220;
            int previewY = 82;
            int previewW = 260;
            int previewH = 250;

            double mouseX = event.x();
            double mouseY = event.y();

            if (mouseX >= previewX && mouseX <= previewX + previewW
                    && mouseY >= previewY && mouseY <= previewY + previewH) {
                this.draggingPreview = true;
                return true;
            }
        }

        return super.mouseClicked(event, handled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            this.draggingPreview = false;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (this.draggingPreview && event.button() == 0) {
            PreviewRenderer.mouseDragged(deltaX, deltaY);
            return true;
        }

        return super.mouseDragged(event, deltaX, deltaY);
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

        PreviewRenderer.render(graphics, 220, 82, 260, 250);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}