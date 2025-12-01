package com.njackal.placement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.njackal.BuildingUtils;
import com.njackal.BuildingUtilsClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

public class PlacementManager {
    private static PlacementManager instance;
    private final Gson gson;

    private PlacementManager() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public static PlacementManager getInstance() {
        if (instance == null) {
            instance = new PlacementManager();
        }
        return instance;
    }

    public List<PlacementData> loadPlacementsFromDisk() {
        Path path = Path.of(BuildingUtilsClient.PLACEMENT_PATH + "/placements.json");
        List<PlacementData> out = new LinkedList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)){

            String line = reader.readLine();
            StringBuilder json = new StringBuilder();
            while (line != null){
                json.append(line);
                line = reader.readLine();
            }

            out.addAll(gson.fromJson(json.toString(), WorldPlacements.class).placements());

        } catch (IOException e) {
            BuildingUtils.LOGGER.error("Failed to read placement file: {}", path);
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            BuildingUtils.LOGGER.error("Invalid placement file (bad JSON) : {} ", path);
        }

        return out;
    }

    public void savePlacementsToDisk(List<PlacementData> placementData) {
        WorldPlacements worldPlacements = new WorldPlacements(placementData);
        String json = gson.toJson(worldPlacements);
        Path path = Path.of(BuildingUtilsClient.PLACEMENT_PATH + "/placements.json");
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)){
            writer.write(json);
        } catch (IOException e) {
            BuildingUtils.LOGGER.error("Failed to write placement file: {}", path);
            e.printStackTrace();
        }
    }
}
