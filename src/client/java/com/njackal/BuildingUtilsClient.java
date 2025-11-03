package com.njackal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.util.math.Vec3d;

public class BuildingUtilsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		BuildingUtils.LOGGER.info("Client Initialized");

		WorldRenderEvents.AFTER_ENTITIES.register(world -> {
			Vec3d camera = world.worldState().cameraRenderState.pos;
			world.matrices().push();
			world.matrices().translate(-camera.x, -camera.y, -camera.z);
			world.matrices().translate(0,75,0);

			VertexRendering.drawFilledBox(world.matrices(),world.consumers().getBuffer(RenderLayer.getDebugFilledBox()),
					0.25f,0.25f, 0.25f, .75f,.75f,.75f,1f,0,0,1f);
			world.matrices().pop();
		});
	}
}