package com.daniel.blocksumo.world;

import com.daniel.blocksumo.Main;
import com.daniel.blocksumo.converter.ArenaData;
import com.daniel.blocksumo.converter.BlockData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldGenerator {

    private int startX, startY, startZ;
    private int endX, endY, endZ;
    private String arena;
    private HashMap<Block, BlockState> blocks = new HashMap<>();
    private final File datafolder = new File("plugins/dBlockSumo/arenas");

    public WorldGenerator(int startX, int startY, int startZ, int endX, int endY, int endZ, String arena) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
        this.arena = arena;
    }

    private void save(String fileName) {
        if (!datafolder.exists()) {
            datafolder.mkdirs();
        }
        File file = new File(datafolder, fileName + ".json");
        Gson gson = new Gson();

        ArenaData arenaData = new ArenaData();
        arenaData.setStartX(startX);
        arenaData.setStartY(startY);
        arenaData.setStartZ(startZ);
        arenaData.setEndX(endX);
        arenaData.setEndY(endY);
        arenaData.setEndZ(endZ);

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

            // Crie um objeto BlockData apenas com os dados do bloco
            BlockData blockDataObj = new BlockData(worldName, x, y, z, blockType, blockData);
            blockDataList.add(blockDataObj);
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", fileName); // Adicione o nome da arena, se necessário
        jsonObject.add("arenaData", gson.toJsonTree(arenaData)); // Adicione os dados da arena
        jsonObject.add("blocks", gson.toJsonTree(blockDataList)); // Adicione os blocos

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            System.out.println("Erro ao salvar arenas: " + e);
        }
    }

    public void saveBlocksInArena(World world) {
        int chunkSize = 16;
        int areaSize = (Math.abs(endX - startX) + 1) * (Math.abs(endZ - startZ) + 1);

        if (areaSize > chunkSize * chunkSize * 4) { // Se a área for maior que 4 chunks
            int startChunkX = startX / chunkSize;
            int startChunkZ = startZ / chunkSize;
            int endChunkX = endX / chunkSize;
            int endChunkZ = endZ / chunkSize;

            List<BlockState> blockStates = new ArrayList<>();

            for (int chunkX = startChunkX; chunkX <= endChunkX; chunkX++) {
                for (int chunkZ = startChunkZ; chunkZ <= endChunkZ; chunkZ++) {
                    Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                    for (int x = 0; x < 16; x++) {
                        for (int y = startY; y <= endY; y++) {
                            for (int z = 0; z < 16; z++) {
                                Block block = chunk.getBlock(x, y, z);
                                if (!block.getType().equals(Material.AIR)) {
                                    blockStates.add(block.getState());
                                }
                            }
                        }
                    }
                }
            }

            // Adicione todos os blocos coletados ao HashMap
            for (BlockState state : blockStates) {
                blocks.put(state.getBlock(), state);
            }
        } else { // Se a área for menor ou igual a 4 chunks
            for (int x = Math.min(startX, endX); x <= Math.max(startX, endX); x++) {
                for (int y = Math.min(startY, endY); y <= Math.max(startY, endY); y++) {
                    for (int z = Math.min(startZ, endZ); z <= Math.max(startZ, endZ); z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (!block.getType().equals(Material.AIR)) {
                            blocks.put(block, block.getState());
                        }
                    }
                }
            }
        }
        save(arena);
        System.out.println(blocks.size() + " blocos");
    }



    public void resetWorld(World world) {
        boolean worldHasBlocks = blocks.entrySet().stream()
                .anyMatch(entry -> entry.getKey().getWorld().equals(world));

        if (worldHasBlocks) {
            final int chunkSize = 16;
            final int totalChunksX = (endX - startX + 1) / chunkSize;
            final int totalChunksY = (endY - startY + 1) / chunkSize;
            final int totalChunksZ = (endZ - startZ + 1) / chunkSize;

            for (int chunkX = 0; chunkX < totalChunksX; chunkX++) {
                for (int chunkY = 0; chunkY < totalChunksY; chunkY++) {
                    for (int chunkZ = 0; chunkZ < totalChunksZ; chunkZ++) {
                        final int startXChunk = startX + chunkX * chunkSize;
                        final int startYChunk = startY + chunkY * chunkSize;
                        final int startZChunk = startZ + chunkZ * chunkSize;
                        final int endXChunk = Math.min(startXChunk + chunkSize - 1, endX);
                        final int endYChunk = Math.min(startYChunk + chunkSize - 1, endY);
                        final int endZChunk = Math.min(startZChunk + chunkSize - 1, endZ);

                        Bukkit.getScheduler().runTask(Main.getPlugin(Main.class), () -> {
                            for (int x = startXChunk; x <= endXChunk; x++) {
                                for (int y = startYChunk; y <= endYChunk; y++) {
                                    for (int z = startZChunk; z <= endZChunk; z++) {
                                        Block block = world.getBlockAt(x, y, z);
                                        BlockState originalState = blocks.get(block);
                                        if (originalState != null) {
                                            block.setType(originalState.getType());
                                            block.setData(originalState.getData().getData());
                                            originalState.update(true, false);
                                        } else {
                                            block.setType(Material.AIR);
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }

            System.out.println("Reset do mundo completo.");
        } else {
            System.out.println("Não há blocos para resetar no mundo especificado.");
        }
    }

    public boolean hasBlocksForWorld(World world) {
        return blocks.entrySet().stream()
                .anyMatch(entry -> entry.getKey().getWorld().equals(world));
    }
}
