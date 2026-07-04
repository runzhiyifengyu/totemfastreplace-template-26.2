package com.gan.client.resource;

import net.minecraft.client.Minecraft;

import java.lang.reflect.Method;

public final class ResourceReloader {

    private ResourceReloader() {
    }

    public static boolean reload() {
        Object mc = Minecraft.getInstance();
        if (mc == null) {
            TextureLoader.STATUS = "Reload failed: no client";
            return false;
        }

        String[] names = {
                "reloadResourcePacks",
                "reloadResources",
                "refreshResources"
        };

        for (String name : names) {
            if (invokeZeroArg(mc, name)) {
                return true;
            }
        }

        TextureLoader.STATUS = TextureLoader.STATUS + " (apply done; press F3+T)";
        return false;
    }

    private static boolean invokeZeroArg(Object target, String methodName) {
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }

            if (method.getParameterCount() != 0) {
                continue;
            }

            try {
                method.invoke(target);
                TextureLoader.STATUS = TextureLoader.STATUS + " (reloaded)";
                return true;
            } catch (Throwable ignored) {
            }
        }

        return false;
    }
}