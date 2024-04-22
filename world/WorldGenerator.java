package com.daniel.blocksumo.world;

import com.daniel.blocksumo.Main;
import com.daniel.blocksumo.converter.ArenaData;
import com.daniel.blocksumo.converter.BlockData;
import com.google.gson.*;
import lombok.SneakyThrows;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldGenerator {

    private int startX, startY, startZ, endX, endY, endZ;
    private final String arena;
    private final HashMap<Block, BlockState> blocks = new HashMap<>();
    private final File datafolder = new File("plugins/dBlockSumo/arenas");

    public WorldGenerator(String arena) {
        this.arena = arena;
    }

    public WorldGenerator(Location pos1, Location pos2, String arena) {
        this(arena);
        startX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        startY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        startZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        endX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        endY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        endZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
    }

    private void save() { //salva em json
        if (!datafolder.exists()) {
            datafolder.mkdirs();
        }
        File file = new File(datafolder, arena + ".json");
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

            BlockData blockDataObj = new BlockData(worldName, x, y, z, blockType, blockData);
            blockDataList.add(blockDataObj);
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", arena);
        jsonObject.add("arenaData", gson.toJsonTree(arenaData));
        jsonObject.add("blocks", gson.toJsonTree(blockDataList));

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            System.out.println("Erro ao salvar arenas: " + e);
        }
    }

    public boolean isOriginalBlock(Block block) {
        return blocks.containsKey(block);
    }

    public void loadArena() { //carrega todos os json pro hashmap
        File file = new File(datafolder, arena + ".json");

        if (file.exists()) {
            Gson gson = new Gson();

            try (FileReader reader = new FileReader(file)) {
                JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

                if (jsonObject.has("arenaData") && jsonObject.has("blocks")) {
                    JsonObject arenaDataJson = jsonObject.getAsJsonObject("arenaData");
                    JsonArray blocksJson = jsonObject.getAsJsonArray("blocks");

                     startX = arenaDataJson.get("startX").getAsInt();
                     startY = arenaDataJson.get("startY").getAsInt();
                     startZ = arenaDataJson.get("startZ").getAsInt();
                     endX = arenaDataJson.get("endX").getAsInt();
                     endY = arenaDataJson.get("endY").getAsInt();
                     endZ = arenaDataJson.get("endZ").getAsInt();

                    for (JsonElement blockElement : blocksJson) {
                        BlockData blockData = gson.fromJson(blockElement, BlockData.class);
                        World world = Bukkit.getWorld(blockData.getWorldName());
                        Location location = new Location(world, blockData.getX(), blockData.getY(), blockData.getZ());
                        Block block = location.getBlock();
                        BlockState state = block.getState();
                        state.setType(Material.valueOf(blockData.getBlockType()));
                        state.setData(new MaterialData(Material.valueOf(blockData.getBlockType()), blockData.getBlockData()));

                        blocks.put(block, state);
                    }
                }
            } catch (IOException e) {
                System.err.println("Erro ao carregar arquivo: " + file.getName());
                e.printStackTrace();
            }
        } else {
            System.err.println("O arquivo da arena " + arena + " não foi encontrado.");
        }
    }

    /*public void loadArenas() {
        File[] files = datafolder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files != null) {
            Gson gson = new Gson();

            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

                    // Verifique se o objeto JSON contém arenaData e blocks
                    if (jsonObject.has("arenaData") && jsonObject.has("blocks")) {
                        JsonObject arenaDataJson = jsonObject.getAsJsonObject("arenaData");
                        JsonArray blocksJson = jsonObject.getAsJsonArray("blocks");

                        // Atualize os atributos da classe com base nos dados do JSON
                        int startX = arenaDataJson.get("startX").getAsInt();
                        int startY = arenaDataJson.get("startY").getAsInt();
                        int startZ = arenaDataJson.get("startZ").getAsInt();
                        int endX = arenaDataJson.get("endX").getAsInt();
                        int endY = arenaDataJson.get("endY").getAsInt();
                        int endZ = arenaDataJson.get("endZ").getAsInt();

                        // Processar os dados dos blocos, se necessário
                        for (JsonElement blockElement : blocksJson) {
                            BlockData blockData = gson.fromJson(blockElement, BlockData.class);
                            // Criar instância de Block e BlockState com os dados do bloco
                            World world = Bukkit.getWorld(blockData.getWorldName());
                            Location location = new Location(world, blockData.getX(), blockData.getY(), blockData.getZ());
                            Block block = location.getBlock();
                            BlockState state = block.getState();
                            state.setType(Material.valueOf(blockData.getBlockType()));
                            state.setData(new MaterialData(Material.valueOf(blockData.getBlockType()), blockData.getBlockData()));
                            // Adicionar ao HashMap blocks
                            blocks.put(block, state);
                        }

                    }
                } catch (IOException e) {
                    System.err.println("Erro ao carregar arquivo: " + file.getName());
                    e.printStackTrace();
                }
            }
        }
    }*/

    public void saveBlocksInArena(World world) {
        int chunkSize = 16;
        int areaSize = (Math.abs(endX - startX) + 1) * (Math.abs(endZ - startZ) + 1);

        if (areaSize > chunkSize * chunkSize * 4) {
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


            for (BlockState state : blockStates) {
                blocks.put(state.getBlock(), state);
            }
        } else {
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
        save();
        System.out.println(blocks.size() + " blocos");
    }



   /* public void resetWorld(World world) {
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
    }*/

    public void resetWorld(World world) {
        boolean worldHasBlocks = blocks.entrySet().stream()
                .anyMatch(entry -> entry.getKey().getWorld().equals(world));

        if (worldHasBlocks) {
            final int chunkSize = 16;
            final int totalChunksX = (endX - startX + 1) / chunkSize;
            final int totalChunksY = (endY - startY + 1) / chunkSize;
            final int totalChunksZ = (endZ - startZ + 1) / chunkSize;

            if (totalChunksX < 2 && totalChunksY < 2 && totalChunksZ < 2) { // Se a área for menor que 3 chunks em cada dimensão
                for (int x = startX; x <= endX; x++) {
                    for (int y = startY; y <= endY; y++) {
                        for (int z = startZ; z <= endZ; z++) {
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
            } else {
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
