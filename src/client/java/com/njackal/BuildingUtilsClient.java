package com.njackal;

import com.njackal.placement.*;
import com.njackal.render.pipeline.FilledThroughWalls;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class BuildingUtilsClient implements ClientModInitializer {

    private static BuildingUtilsClient instance;
    // mod-specific file directories
    public static final String BASE_PATH = "BuildingUtils";
    public static final String MODEL_PATH = BASE_PATH +"/models";
    public static final String PLACEMENT_PATH = BASE_PATH +"/placements";


    public static BuildingUtilsClient getInstance() {
        return instance;
    }

    @Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		BuildingUtils.LOGGER.info("Client Initialized");
        instance = this;
        FilledThroughWalls fill = FilledThroughWalls.getInstance();
        List<PlacementData> placementData = PlacementManager.getInstance().loadPlacementsFromDisk();

        //init placements
        List<Placement> placements = new LinkedList<>();
        for (PlacementData data : placementData ) {
            try {
                placements.add(Placement.fromData(data));
            } catch (IOException e) {
                BuildingUtils.LOGGER.error("Failed to load model: {}", data.model());
                e.printStackTrace();
            }
        }

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            for (Placement placement : placements) {
                fill.draw(context, placement.model().vertices(), placement.transform());
            }
        });
	}
    

}





















