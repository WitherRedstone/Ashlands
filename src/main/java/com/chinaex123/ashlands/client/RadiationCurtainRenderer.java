package com.chinaex123.ashlands.client;

import com.chinaex123.ashlands.util.AshlandsWorld;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class RadiationCurtainRenderer {

    private static final ResourceLocation FORCEFIELD_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/forcefield.png");

    /** 光幕颜色：辐射绿。 */
    private static final float COLOR_RED = 0.42F;
    private static final float COLOR_GREEN = 0.98F;
    private static final float COLOR_BLUE = 0.32F;

    /** 每多少格滚动一次贴图。 */
    private static final double TEXTURE_SCALE = 4.0D;
    private static final double TEXTURE_SCALE_V = 32.0D;

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (!AshlandsWorld.isAshlandsWorld(level)) {
            return;
        }

        int radius = AshlandsWorld.clientRadius();
        int centerChunkX = AshlandsWorld.clientCenterChunkX();
        int centerChunkZ = AshlandsWorld.clientCenterChunkZ();

        double minX = (double) (centerChunkX - radius) * 16.0D;
        double maxX = (double) (centerChunkX + radius + 1) * 16.0D;
        double minZ = (double) (centerChunkZ - radius) * 16.0D;
        double maxZ = (double) (centerChunkZ + radius + 1) * 16.0D;

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

        // 视野边缘淡出，超出渲染距离直接不画。
        double fadeDistance = (double) (minecraft.options.getEffectiveRenderDistance() * 16);
        if (fadeDistance <= 0.0D) {
            return;
        }

        float fade = fadeFactor(cameraPos, minX, minZ, maxX, maxZ, fadeDistance);
        if (fade <= 0.0001F) {
            return;
        }

        double bottom = level.getMinBuildHeight();
        double top = level.getMaxBuildHeight();

        // 光幕贴图滚动
        float scroll = (float) (Util.getMillis() % 3000L) / 3000.0F;

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        RenderSystem.setShaderTexture(0, FORCEFIELD_LOCATION);
        RenderSystem.depthMask(Minecraft.useShaderTransparency());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(COLOR_RED, COLOR_GREEN, COLOR_BLUE, fade);
        RenderSystem.polygonOffset(-3.0F, -3.0F);
        RenderSystem.enablePolygonOffset();
        RenderSystem.disableCull();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        drawWall(buffer, cameraPos, minX, minZ, maxX, minZ, minX, maxX, bottom, top, scroll);
        drawWall(buffer, cameraPos, minX, maxZ, maxX, maxZ, minX, maxX, bottom, top, scroll);
        drawWall(buffer, cameraPos, minX, minZ, minX, maxZ, minZ, maxZ, bottom, top, scroll);
        drawWall(buffer, cameraPos, maxX, minZ, maxX, maxZ, minZ, maxZ, bottom, top, scroll);

        MeshData mesh = buffer.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }

        RenderSystem.polygonOffset(0.0F, 0.0F);
        RenderSystem.disablePolygonOffset();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
    }

    private static float fadeFactor(Vec3 cameraPos, double minX, double minZ, double maxX, double maxZ, double fadeDistance) {
        double nearestX = Mth.clamp(cameraPos.x, minX, maxX);
        double nearestZ = Mth.clamp(cameraPos.z, minZ, maxZ);
        double dx = cameraPos.x - nearestX;
        double dz = cameraPos.z - nearestZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance >= fadeDistance) {
            return 0.0F;
        }

        return (float) Mth.clamp(Math.pow(1.0D - distance / fadeDistance, 4.0D), 0.0D, 1.0D);
    }

    private static void drawWall(BufferBuilder buffer, Vec3 cameraPos,
                                 double ax, double az, double bx, double bz,
                                 double start, double end,
                                 double bottom, double top, float scroll) {
        double length = Math.abs(end - start);
        int segments = (int) Math.max(1L, Math.round(length));
        double height = top - bottom;

        for (int i = 0; i < segments; i++) {
            double s0 = (double) i / segments;
            double s1 = (double) (i + 1) / segments;
            double x0 = ax + (bx - ax) * s0;
            double z0 = az + (bz - az) * s0;
            double x1 = ax + (bx - ax) * s1;
            double z1 = az + (bz - az) * s1;

            float u0 = (float) ((start + length * s0) / TEXTURE_SCALE);
            float u1 = (float) ((start + length * s1) / TEXTURE_SCALE);
            float vBottom = (float) (height / TEXTURE_SCALE_V) + scroll;

            vertex(buffer, cameraPos, x0, bottom, z0, u0, vBottom);
            vertex(buffer, cameraPos, x1, bottom, z1, u1, vBottom);
            vertex(buffer, cameraPos, x1, top, z1, u1, scroll);
            vertex(buffer, cameraPos, x0, top, z0, u0, scroll);
        }
    }

    private static void vertex(BufferBuilder buffer, Vec3 cameraPos,
                               double x, double y, double z, float u, float v) {
        buffer.addVertex(
                (float) (x - cameraPos.x),
                (float) (y - cameraPos.y),
                (float) (z - cameraPos.z)
        ).setUv(u, v);
    }
}
