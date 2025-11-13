package com.njackal.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.njackal.BuildingUtils;
import com.njackal.render.DrawHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class FilledThroughWalls {
    private static FilledThroughWalls INSTANCE;
    private static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                    .withLocation(Identifier.of(BuildingUtils.MOD_ID, "pipeline/debug_filled_box_through_walls"))
                    .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .build()
    );
    private static final BufferAllocator ALLOCATOR = new BufferAllocator(RenderLayer.CUTOUT_BUFFER_SIZE);
    private BufferBuilder buffer;
    private MappableRingBuffer vertexBuffer;

    private FilledThroughWalls(){}

    public static FilledThroughWalls getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FilledThroughWalls();
        }
        return INSTANCE;
    }

    public void draw(
            WorldRenderContext context,
            List<Vector3f> vertices
    ) {
        fillBuffer(context, vertices);
        drawFilledThroughWalls(MinecraftClient.getInstance(), FILLED_THROUGH_WALLS);
    }

    private void fillBuffer(WorldRenderContext context, List<Vector3f> vertices) {
        MatrixStack matrices = context.matrices();
        Vec3d camera = context.worldState().cameraRenderState.pos;

        assert matrices != null;

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (buffer == null) {
            buffer = new BufferBuilder(ALLOCATOR, FILLED_THROUGH_WALLS.getVertexFormatMode(), FILLED_THROUGH_WALLS.getVertexFormat());
        }

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        for (Vector3f v : vertices) {
            buffer.vertex(matrix, v.x, v.y, v.z).color(0f,0f,1f,1f);
        }

        matrices.pop();
    }

    private void drawFilledThroughWalls(MinecraftClient client,
                                        @SuppressWarnings("SameParameterValue") RenderPipeline pipeline
    ) {
        // Build the buffer
        BuiltBuffer builtBuffer = buffer.end();
        BuiltBuffer.DrawParameters drawParameters = builtBuffer.getDrawParameters();
        VertexFormat format = drawParameters.format();

        GpuBuffer vertices = DrawHelper.upload(builtBuffer, getVertexBuffer(drawParameters, format));

        DrawHelper.draw(client, pipeline, builtBuffer, drawParameters, vertices, format, ALLOCATOR);

        // Rotate the vertex buffer so we are less likely to use buffers that the GPU is using
        vertexBuffer.rotate();
        buffer = null;
    }

    private MappableRingBuffer getVertexBuffer(
            BuiltBuffer.DrawParameters drawParameters,
            VertexFormat format
    ) {
        // Calculate the size needed for the vertex buffer
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        // Initialize or resize the vertex buffer as needed
        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            vertexBuffer = new MappableRingBuffer(() -> BuildingUtils.MOD_ID + " example render pipeline", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        }
        return vertexBuffer;
    }

    public void close() {
        ALLOCATOR.close();

        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }
}
