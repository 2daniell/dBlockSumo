package com.daniel.blocksumo.converter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockData {
    private String worldName;
    private int x;
    private int y;
    private int z;
    private String blockType;
    private byte blockData;

    public BlockData(String worldName, int x, int y, int z, String blockType, byte blockData) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockType = blockType;
        this.blockData = blockData;
    }

}
