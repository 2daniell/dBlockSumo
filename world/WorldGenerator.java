package com.daniel.blocksumo.world;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.w3c.dom.ls.LSOutput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WorldGenerator {

    private static final HashMap<Block, BlockState> blocks = new HashMap<>();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File datafolder = new File("plugins/BlockSumo/arenas");
    private static final File file = new File(datafolder, "arenas.json");

    private static void save() {
        if (!(datafolder.exists())) {
            datafolder.mkdirs();
        }

        try(FileWriter writer = new FileWriter(file)) {
            gson.toJson(blocks, writer);
        } catch (IOException e) {
            System.out.println("Erro ao salvar arenas: " + e);
        }
    }


    public static void saveWorld(World world, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    BlockState state = block.getState();
                    blocks.put(block, state);
                    System.out.println("Bloco inserido em " + block.getLocation());
                }
            }
        }
        System.out.println("Total de blocos inseridos: " + blocks.size());
        save();
    }

    /*public void resetWorld(World world) {
        System.out.println("HashMap vazio: " + blocks.isEmpty());
        System.out.println("Total de blocos no HashMap: " + blocks.size());

        for (Map.Entry<Block, BlockState> entry : blocks.entrySet()) {
            Block block = entry.getKey();
            BlockState originalState = entry.getValue();
            block.setType(originalState.getType());
            block.setData(originalState.getData().getData());
            originalState.update(true, false);
        }

        blocks.clear();
        System.out.println("HashMap limpo: " + blocks.isEmpty());
    }*/

    public static void resetWorld(World world) {
        System.out.println("HashMap vazio: " + blocks.isEmpty());
        System.out.println("Total de blocos no HashMap: " + blocks.size());

        Iterator<Map.Entry<Block, BlockState>> iterator = blocks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Block, BlockState> entry = iterator.next();
            Block block = entry.getKey();
            World blockWorld = block.getWorld();

            if (blockWorld.equals(world)) {
                BlockState originalState = entry.getValue();
                block.setType(originalState.getType());
                block.setData(originalState.getData().getData());
                originalState.update(true, false);
                iterator.remove();
            }
        }

        System.out.println("HashMap limpo: " + blocks.isEmpty());
    }

}
