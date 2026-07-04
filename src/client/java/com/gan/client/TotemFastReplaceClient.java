package com.gan.client;

import com.gan.client.key.KeyManager;
import net.fabricmc.api.ClientModInitializer;

public class TotemFastReplaceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyManager.init();
        System.out.println(">>> CLIENT INIT CALLED               <<<");
    }
}