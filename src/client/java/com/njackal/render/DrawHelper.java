package com.njackal.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.njackal.BuildingUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.util.BufferAllocator;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public class DrawHelper {
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);

    public static GpuBuffer upload(
            BuiltBuffer builtBuffer,
            MappableRingBuffer vertexBuffer
    ) {
        // Copy vertex data into the vertex buffer
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.getBlocking().slice(0, builtBuffer.getBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.getBuffer(), mappedView.data());
        }

        return vertexBuffer.getBlocking();
    }

    public static void draw(MinecraftClient client,
                            RenderPipeline pipeline,
                            BuiltBuffer builtBuffer,
                            BuiltBuffer.DrawParameters drawParameters,
                            GpuBuffer vertices,
                            VertexFormat format,
                            BufferAllocator allocator) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        if (pipeline.getVertexFormatMode() == VertexFormat.DrawMode.QUADS) {
            // Sort the quads if there is translucency
            builtBuffer.sortQuads(allocator, RenderSystem.getProjectionType().getVertexSorter());
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
}
