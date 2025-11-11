package com.njackal;

import com.njackal.render.pipeline.FilledThroughWalls;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import org.joml.Vector3f;

import java.util.List;

public class BuildingUtilsClient implements ClientModInitializer {

    private static BuildingUtilsClient instance;

    public static BuildingUtilsClient getInstance() {
        return instance;
    }

    @Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		BuildingUtils.LOGGER.info("Client Initialized");
        instance = this;
        FilledThroughWalls fill = FilledThroughWalls.getInstance();
        
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            fill.draw(context, List.of(
                    new Vector3f(0f, 75f, 0f),
                    new Vector3f(0f, 77f, 0f),
                    new Vector3f(0f, 75f, 3f),
                    new Vector3f(0f, 77f, 3f)
            ));
        });
        
	}
    

}





















