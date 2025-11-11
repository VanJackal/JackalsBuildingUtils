package com.njackal;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public class BuildingUtilsClient implements ClientModInitializer {
    
    private static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                    .withLocation(Identifier.of(BuildingUtils.MOD_ID, "pipeline/debug_filled_box_through_walls"))
                    .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_STRIP)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .build()
    );
    private static final BufferAllocator ALLOCATOR = new BufferAllocator(RenderLayer.CUTOUT_BUFFER_SIZE);
    private static BuildingUtilsClient instance;
    private BufferBuilder buffer;

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private MappableRingBuffer vertexBuffer;

    public static BuildingUtilsClient getInstance() {
        return instance;
    }

    @Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		BuildingUtils.LOGGER.info("Client Initialized");
        instance = this;
        
        WorldRenderEvents.AFTER_ENTITIES.register(this::extractAndDraw);
        
	}
    
    private void extractAndDraw(WorldRenderContext context) {
        renderWaypoint(context);
        drawFilledThroughWalls(MinecraftClient.getInstance(), FILLED_THROUGH_WALLS);
    }
    
    private void renderWaypoint(WorldRenderContext context) {
        MatrixStack matrices = context.matrices();
        Vec3d camera = context.worldState().cameraRenderState.pos;
        
        assert matrices != null;
        
        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (buffer == null) {
            buffer = new BufferBuilder(ALLOCATOR, FILLED_THROUGH_WALLS.getVertexFormatMode(), FILLED_THROUGH_WALLS.getVertexFormat());
        }

        //VertexRendering.drawFilledBox(matrices, buffer, 0f, 75, 0f, 1f, 76f, 1f, 0f, 1f, 0f, 0.5f);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.vertex(matrix, 0, 75, 0).color(0,0,1f,1f);
        buffer.vertex(matrix, 0, 77, 0).color(0,0,1f,1f);
        buffer.vertex(matrix, 0, 75, 3).color(0,0,1f,1f);

        matrices.pop();
    }
    
    private void simpleDebug(){
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

    private void drawFilledThroughWalls(MinecraftClient client, @SuppressWarnings("SameParameterValue") RenderPipeline pipeline) {
        // Build the buffer
        BuiltBuffer builtBuffer = buffer.end();
        BuiltBuffer.DrawParameters drawParameters = builtBuffer.getDrawParameters();
        VertexFormat format = drawParameters.format();

        GpuBuffer vertices = upload(drawParameters, format, builtBuffer);

        draw(client, pipeline, builtBuffer, drawParameters, vertices, format);

        // Rotate the vertex buffer so we are less likely to use buffers that the GPU is using
        vertexBuffer.rotate();
        buffer = null;
    }

    private GpuBuffer upload(BuiltBuffer.DrawParameters drawParameters, VertexFormat format, BuiltBuffer builtBuffer) {
        // Calculate the size needed for the vertex buffer
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        // Initialize or resize the vertex buffer as needed
        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            vertexBuffer = new MappableRingBuffer(() -> BuildingUtils.MOD_ID + " example render pipeline", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        }

        // Copy vertex data into the vertex buffer
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.getBlocking().slice(0, builtBuffer.getBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.getBuffer(), mappedView.data());
        }

        return vertexBuffer.getBlocking();
    }

    private static void draw(MinecraftClient client, RenderPipeline pipeline, BuiltBuffer builtBuffer, BuiltBuffer.DrawParameters drawParameters, GpuBuffer vertices, VertexFormat format) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        if (pipeline.getVertexFormatMode() == VertexFormat.DrawMode.QUADS) {
            // Sort the quads if there is translucency
            builtBuffer.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().getVertexSorter());
            // Upload the index buffer
            indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.getSortedBuffer());
            indexType = builtBuffer.getDrawParameters().indexType();
        } else {
            // Use the general shape index buffer for non-quad draw modes
            RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = shapeIndexBuffer.getIndexBuffer(drawParameters.indexCount());
            indexType = shapeIndexBuffer.getIndexType();
        }

        // Actually execute the draw
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .write(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, new Vector3f(), RenderSystem.getTextureMatrix(), 1f);
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> BuildingUtils.MOD_ID + " example render pipeline rendering", client.getFramebuffer().getColorAttachmentView(), OptionalInt.empty(), client.getFramebuffer().getDepthAttachmentView(), OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            // Bind texture if applicable:
            // Sampler0 is used for texture inputs in vertices
            // renderPass.bindSampler("Sampler0", textureView);

            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);

            // The base vertex is the starting index when we copied the data into the vertex buffer divided by vertex size
            //noinspection ConstantValue
            renderPass.drawIndexed(0 / format.getVertexSize(), 0, drawParameters.indexCount(), 1);
        }

        builtBuffer.close();
    }

    public void close() {
        ALLOCATOR.close();

        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }
    
}





















