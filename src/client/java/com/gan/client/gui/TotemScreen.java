package com.gan.client.gui;

import com.gan.client.resource.TextureLoader;
import com.gan.client.resource.ZipManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TotemScreen extends Screen {

    private static final int PAGE_SIZE = 10;
    private final int requestedPage;
    private int currentPage;

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
            var file = TextureLoader.TEXTURES.get(i);
            String name = file.getName();

            this.addRenderableWidget(
                    Button.builder(Component.literal(name), btn -> {
                        TextureLoader.SELECTED = file;
                        TextureLoader.STATUS = "Selected: " + name;
                    }).bounds(x, currentY, buttonWidth, buttonHeight).build()
            );

            currentY += buttonHeight + gap;
        }
    }

    private void openPage(int page) {
        Minecraft.getInstance().setScreenAndShow(new TotemScreen(page));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int totalPages = Math.max(1, (TextureLoader.TEXTURES.size() + PAGE_SIZE - 1) / PAGE_SIZE);

        graphics.text(this.font, "PNG Files", 20, 18, 0xFFFFFF, true);
        graphics.text(this.font, "Page: " + (this.currentPage + 1) + " / " + totalPages, 220, 18, 0xFFFFFF, true);

        String selected = TextureLoader.SELECTED == null
                ? "Selected: none"
                : "Selected: " + TextureLoader.SELECTED.getName();

        graphics.text(this.font, selected, 220, 34, 0xFFFFFF, true);
        graphics.text(this.font, "Status: " + TextureLoader.STATUS, 220, 50, 0xFFFFFF, true);
        graphics.text(this.font, "Zip: resourcepacks/custom-totem-plush.zip", 220, 66, 0xFFFFFF, true);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}