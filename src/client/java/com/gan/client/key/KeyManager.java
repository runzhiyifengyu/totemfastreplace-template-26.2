package com.gan.client.key;

import com.gan.TotemFastReplace;
import com.gan.client.gui.TotemScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class KeyManager {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(TotemFastReplace.MOD_ID, "ui")
    );

    private static final KeyMapping OPEN_KEY = new KeyMapping(
            "key.totemfastreplace.open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            CATEGORY
    );

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }

            while (OPEN_KEY.consumeClick()) {
                net.minecraft.client.Minecraft.getInstance().setScreenAndShow(new TotemScreen());
            }
        });
    }
}