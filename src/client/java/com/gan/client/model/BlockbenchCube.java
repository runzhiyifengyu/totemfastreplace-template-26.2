package com.gan.client.model;

import java.util.HashMap;
import java.util.Map;

public class BlockbenchCube {

    public float fromX;
    public float fromY;
    public float fromZ;

    public float toX;
    public float toY;
    public float toZ;

    public String name = "";

    public BlockbenchRotation rotation;

    public final Map<String, BlockbenchFace> faces = new HashMap<>();

    public BlockbenchFace face(String side) {
        return faces.get(side);
    }
}