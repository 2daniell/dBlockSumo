package com.daniel.blocksumo.world;

import com.daniel.blocksumo.converter.BlockData;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.w3c.dom.ls.LSOutput;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class WorldGenerator {

    private int startX, startY, startZ;
    private int endX, endY, endZ;
    private final HashMap<Block, BlockState> blocks = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File datafolder = new File("plugins/BlockSumo/arenas");

    /*private static void save() {
        if (!(datafolder.exists())) {
            datafolder.mkdirs();
        }

        try(FileWriter writer = new FileWriter(file)) {
            gson.toJson(blocks, writer);
        } catch (IOException e) {
            System.out.println("Erro ao salvar arenas: " + e);
        }
    }*/

    private void save(String fileName) {
        if (!datafolder.exists()) {
            datafolder.mkdirs();
        }
        File file = new File(datafolder, fileName+".json");
        List<BlockData> blockDataList = new ArrayList<>();
        for (Map.Entry<Block, BlockState> entry : blocks.entrySet()) {
            Block block = entry.getKey();
            BlockState state = entry.getValue();
            Location location = block.getLocation();
            String worldName = location.getWorld().getName();
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            String blockType = state.getType().toString();
            byte blockData = state.getData().getData();
            BlockData blockDataObj = new BlockData(worldName, x, y, z, blockType, blockData);
            blockDataList.add(blockDataObj);
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(blockDataList, writer);
        } catch (IOException e) {
            System.out.println("Erro ao salvar arenas: " + e);
        }
    }

    public void loadAllArenas() {
        if (!datafolder.exists() || !datafolder.isDirectory()) {
            System.out.println("A pasta de arenas não existe ou não é um diretório.");
            return;
        }

        File[] arenaFiles = datafolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (arenaFiles == null) {
            System.out.println("Nenhum arquivo JSON encontrado na pasta de arenas.");
            return;
        }

        for (File arenaFile : arenaFiles) {
            loadArenaFromFile(arenaFile);
        }
    }

    private void loadArenaFromFile(File arenaFile) {
        try (FileReader reader = new FileReader(arenaFile)) {
            Type listType = new TypeToken<List<BlockData>>() {}.getType();
            List<BlockData> blockDataList = gson.fromJson(reader, listType);

            for (BlockData blockData : blockDataList) {
                World world = Bukkit.getWorld(blockData.getWorldName());
                if (world != null) {
                    Location location = new Location(world, blockData.getX(), blockData.getY(), blockData.getZ());
                    Block block = location.getBlock();
                    Material blockType = Material.valueOf(blockData.getBlockType());
                    byte blockDataValue = blockData.getBlockData();
                    block.setType(blockType);
                    block.setData(blockDataValue);
                    BlockState state = block.getState();
                    blocks.put(block, state);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar a arena do arquivo " + arenaFile.getName() + ": " + e);
        }
    }


    /*public static void saveWorld(World world, int startX, int startY, int startZ, int endX, int endY, int endZ) {
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
    }*/

    public void saveWorld(World world, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!block.getType().equals(Material.AIR) && block.getType() != null) {
                        BlockState state = block.getState();
                        blocks.put(block, state);
                        System.out.println("Bloco inserido em " + block.getLocation());
                    }
                }
            }
        }
        System.out.println("Total de blocos inseridos: " + blocks.size());
        save(world.getName());
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

    public void resetWorld(World world) {
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(Material.AIR);
                }
            }
        }
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
