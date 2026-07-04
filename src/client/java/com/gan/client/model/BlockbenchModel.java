package com.gan.client.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BlockbenchModel {

    public String credit;
    public int textureWidth = 64;
    public int textureHeight = 64;
    public Map<String, String> textures = new LinkedHashMap<>();
    public List<BlockbenchCube> cubes = new ArrayList<>();

    public BlockbenchModel() {
    }

    public List<BlockbenchCube> getCubes() {
        return cubes;
    }

    public void setCubes(List<BlockbenchCube> cubes) {
        this.cubes = (cubes == null) ? new ArrayList<>() : cubes;
    }

    public void addCube(BlockbenchCube cube) {
        if (cube != null) {
            this.cubes.add(cube);
        }
    }

    public int getTextureWidth() {
        return textureWidth;
    }

    public void setTextureWidth(int textureWidth) {
        this.textureWidth = textureWidth;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    public void setTextureHeight(int textureHeight) {
        this.textureHeight = textureHeight;
    }

    public Map<String, String> getTextures() {
        return textures;
    }

    public void setTextures(Map<String, String> textures) {
        this.textures = (textures == null) ? new LinkedHashMap<>() : textures;
    }
}