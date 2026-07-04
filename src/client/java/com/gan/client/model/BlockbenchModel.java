package com.gan.client.model;

import java.util.ArrayList;
import java.util.List;

public class BlockbenchModel {

    private int textureWidth;
    private int textureHeight;

    private final List<BlockbenchCube> cubes = new ArrayList<>();

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

    public List<BlockbenchCube> getCubes() {
        return cubes;
    }

    public void addCube(BlockbenchCube cube) {
        cubes.add(cube);
    }

    public void clear() {
        cubes.clear();
    }
}