package com.daniel.blocksumo.world;

import com.daniel.blocksumo.converter.ArenaData;
import com.daniel.blocksumo.converter.BlockData;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
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

        // Crie um objeto ArenaData e preencha com as coordenadas da arena
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

        // Crie um objeto JSON contendo os dados da arena e os blocos
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
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            // Carregar informações da arena
            JsonObject arenaDataObject = jsonObject.getAsJsonObject("arenaData");
            this.startX = arenaDataObject.get("startX").getAsInt();
            this.startY = arenaDataObject.get("startY").getAsInt();
            this.startZ = arenaDataObject.get("startZ").getAsInt();
            this.endX = arenaDataObject.get("endX").getAsInt();
            this.endY = arenaDataObject.get("endY").getAsInt();
            this.endZ = arenaDataObject.get("endZ").getAsInt();

            // Restaurar blocos da arena
            JsonArray blocksArray = jsonObject.getAsJsonArray("blocks");
            for (JsonElement element : blocksArray) {
                JsonObject blockObject = element.getAsJsonObject();
                World world = Bukkit.getWorld(blockObject.get("worldName").getAsString());
                if (world != null) {
                    int x = blockObject.get("x").getAsInt();
                    int y = blockObject.get("y").getAsInt();
                    int z = blockObject.get("z").getAsInt();
                    Material blockType = Material.valueOf(blockObject.get("blockType").getAsString());
                    byte blockDataValue = blockObject.get("blockData").getAsByte();
                    Block block = world.getBlockAt(x, y, z);
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
        // Verificar se o mundo tem blocos a serem resetados
        boolean worldHasBlocks = blocks.entrySet().stream()
                .anyMatch(entry -> entry.getKey().getWorld().equals(world));

        if (worldHasBlocks) {
            // Remover os blocos marcados para resetar
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    for (int z = startZ; z <= endZ; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        block.setType(Material.AIR);
                    }
                }
            }

            // Restaurar os blocos do hashmap apenas para o mundo especificado
            Iterator<Map.Entry<Block, BlockState>> iterator = blocks.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Block, BlockState> entry = iterator.next();
                Block block = entry.getKey();
                if (block.getWorld().equals(world)) {
                    BlockState originalState = entry.getValue();
                    block.setType(originalState.getType());
                    block.setData(originalState.getData().getData());
                    originalState.update(true, false);
                    iterator.remove();
                }
            }

            System.out.println("HashMap limpo: " + blocks.isEmpty());
        } else {
            System.out.println("Não há blocos para resetar no mundo especificado.");
        }
    }

}
