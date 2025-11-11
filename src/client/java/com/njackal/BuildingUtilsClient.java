package com.njackal;

import com.njackal.render.pipeline.FilledThroughWalls;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

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
        
        WorldRenderEvents.AFTER_ENTITIES.register(fill::extractAndDraw);
        
	}
    

}





















