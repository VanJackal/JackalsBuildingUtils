package com.njackal;

import com.njackal.placement.Placement;
import com.njackal.placement.PlacementData;
import com.njackal.placement.Transform;
import com.njackal.render.pipeline.FilledThroughWalls;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class BuildingUtilsClient implements ClientModInitializer {

    private static BuildingUtilsClient instance;
    // mod-specific file directories
    public static final String BASE_PATH = "BuildingUtils";
    public static final String MODEL_PATH = BASE_PATH +"/models";


    public static BuildingUtilsClient getInstance() {
        return instance;
    }

    @Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		BuildingUtils.LOGGER.info("Client Initialized");
        instance = this;
        FilledThroughWalls fill = FilledThroughWalls.getInstance();
        List<PlacementData> placementData = List.of(
                new PlacementData(
                        new Transform(
                            new Vector3f(0f, 0f, 0f),
                            new Vector3f(0f, 0f, 0f),
                            new Vector3f(1f, 1f, 1f)
                        ),
                        MODEL_PATH + "/testMonkey.obj"
                ),
                new PlacementData(
                        new Transform(
                                new Vector3f(0f, 5f, 0f),
                                new Vector3f(0f, 0.7853982f, 0f),
                                new Vector3f(5f, 1f, 1f)
                        ),
                        MODEL_PATH + "/testMonkey.obj"
                )
        );

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





















