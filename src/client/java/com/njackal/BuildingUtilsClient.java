package com.njackal;

import com.njackal.render.model.Model;
import com.njackal.render.model.loader.ModelLoader;
import com.njackal.render.model.loader.ObjLoader;
import com.njackal.render.pipeline.FilledThroughWalls;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Paths;
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
        ModelLoader loader = ObjLoader.getInstance();
        Model model;
        try {
            model = loader.load(Paths.get(MODEL_PATH + "/testMonkey.obj"));
            WorldRenderEvents.AFTER_ENTITIES.register(context -> {
                fill.draw(context, model.vertices());
            });
        } catch (IOException e) {
            BuildingUtils.LOGGER.error("Failed to load model");
            e.printStackTrace();
        }

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            fill.draw(context, List.of(
                    new Vector3f(0f, 75f, 0f),
                    new Vector3f(0f, 77f, 0f),
                    new Vector3f(0f, 75f, 3f)
            ));
        });
	}
    

}





















