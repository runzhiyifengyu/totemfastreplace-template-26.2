package com.gan.client.model;

import com.gan.TotemFastReplace;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class BlockbenchLoader {

    private static final Gson GSON = new GsonBuilder().create();

    private BlockbenchLoader() {
    }

    public static BlockbenchModel loadDefault() {
        try (InputStream raw = TotemFastReplace.class.getResourceAsStream("/defaults/custom-totem-plush.zip")) {
            if (raw != null) {
                BlockbenchModel model = loadFromBundledZip(raw);
                if (model != null && !model.getCubes().isEmpty()) {
                    return model;
                }
            }
        } catch (Exception ignored) {
        }

        return new BlockbenchModel();
    }

    public static BlockbenchModel loadFromJson(InputStream in) {
        if (in == null) {
            return new BlockbenchModel();
        }

        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            return parseRoot(root);
        } catch (Exception ignored) {
            return new BlockbenchModel();
        }
    }

    private static BlockbenchModel loadFromBundledZip(InputStream zipStream) {
        try (ZipInputStream zis = new ZipInputStream(zipStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                if (entry.getName().endsWith("assets/minecraft/models/item/totem_of_undying.json")) {
                    byte[] data = readAllBytes(zis);
                    return loadFromJson(new ByteArrayInputStream(data));
                }
            }
        } catch (Exception ignored) {
        }

        return new BlockbenchModel();
    }

    private static BlockbenchModel parseRoot(JsonObject root) {
        BlockbenchModel model = new BlockbenchModel();

        if (root == null) {
            return model;
        }

        if (root.has("texture_size") && root.get("texture_size").isJsonArray()) {
            JsonArray textureSize = root.getAsJsonArray("texture_size");
            if (textureSize.size() >= 2) {
                model.setTextureWidth(safeInt(textureSize.get(0), 64));
                model.setTextureHeight(safeInt(textureSize.get(1), 64));
            }
        } else {
            model.setTextureWidth(64);
            model.setTextureHeight(64);
        }

        if (root.has("elements") && root.get("elements").isJsonArray()) {
            JsonArray elements = root.getAsJsonArray("elements");
            for (JsonElement elementEl : elements) {
                if (!elementEl.isJsonObject()) {
                    continue;
                }

                JsonObject elementObj = elementEl.getAsJsonObject();
                BlockbenchCube cube = new BlockbenchCube();
                cube.name = safeString(elementObj, "name", "");

                fillVec3(elementObj, "from", cube, true);
                fillVec3(elementObj, "to", cube, false);

                if (elementObj.has("rotation") && elementObj.get("rotation").isJsonObject()) {
                    JsonObject rotObj = elementObj.getAsJsonObject("rotation");
                    BlockbenchRotation rotation = new BlockbenchRotation();
                    rotation.angle = safeFloat(rotObj, "angle", 0.0f);
                    rotation.axis = safeString(rotObj, "axis", "y");

                    if (rotObj.has("origin") && rotObj.get("origin").isJsonArray()) {
                        JsonArray origin = rotObj.getAsJsonArray("origin");
                        if (origin.size() >= 3) {
                            rotation.originX = safeFloat(origin.get(0), 8.0f);
                            rotation.originY = safeFloat(origin.get(1), 8.0f);
                            rotation.originZ = safeFloat(origin.get(2), 8.0f);
                        }
                    }

                    cube.rotation = rotation;
                }

                if (elementObj.has("shade")) {
                    // 不强制使用，保留给后续扩展
                }

                if (elementObj.has("faces") && elementObj.get("faces").isJsonObject()) {
                    JsonObject faces = elementObj.getAsJsonObject("faces");
                    for (String side : faces.keySet()) {
                        JsonElement faceEl = faces.get(side);
                        if (!faceEl.isJsonObject()) {
                            continue;
                        }

                        JsonObject faceObj = faceEl.getAsJsonObject();
                        BlockbenchFace face = new BlockbenchFace();

                        if (faceObj.has("uv") && faceObj.get("uv").isJsonArray()) {
                            JsonArray uv = faceObj.getAsJsonArray("uv");
                            if (uv.size() >= 4) {
                                face.u1 = safeFloat(uv.get(0), 0.0f);
                                face.v1 = safeFloat(uv.get(1), 0.0f);
                                face.u2 = safeFloat(uv.get(2), 0.0f);
                                face.v2 = safeFloat(uv.get(3), 0.0f);
                            }
                        }

                        face.rotation = safeInt(faceObj, "rotation", 0);
                        face.texture = safeString(faceObj, "texture", "#plush");

                        cube.faces.put(side.toLowerCase(), face);
                    }
                }

                model.addCube(cube);
            }
        }

        return model;
    }

    private static void fillVec3(JsonObject obj, String key, BlockbenchCube cube, boolean from) {
        if (!obj.has(key) || !obj.get(key).isJsonArray()) {
            return;
        }

        JsonArray arr = obj.getAsJsonArray(key);
        if (arr.size() < 3) {
            return;
        }

        float x = safeFloat(arr.get(0), 0.0f);
        float y = safeFloat(arr.get(1), 0.0f);
        float z = safeFloat(arr.get(2), 0.0f);

        if (from) {
            cube.fromX = x;
            cube.fromY = y;
            cube.fromZ = z;
        } else {
            cube.toX = x;
            cube.toY = y;
            cube.toZ = z;
        }
    }

    private static int safeInt(JsonObject obj, String key, int fallback) {
        try {
            return obj.has(key) ? obj.get(key).getAsInt() : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static float safeFloat(JsonObject obj, String key, float fallback) {
        try {
            return obj.has(key) ? obj.get(key).getAsFloat() : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static int safeInt(JsonElement el, int fallback) {
        try {
            return el != null && !el.isJsonNull() ? el.getAsInt() : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static float safeFloat(JsonElement el, float fallback) {
        try {
            return el != null && !el.isJsonNull() ? el.getAsFloat() : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static String safeString(JsonObject obj, String key, String fallback) {
        try {
            return obj.has(key) ? obj.get(key).getAsString() : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static byte[] readAllBytes(InputStream in) throws java.io.IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) >= 0) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}