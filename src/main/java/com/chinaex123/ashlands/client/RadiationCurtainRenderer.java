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
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class RadiationCurtainRenderer {

    private static final ResourceLocation FORCEFIELD_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/forcefield.png");

    /** 光幕颜色：辐射绿。 */
    private static final float COLOR_RED = 0.42F;
    private static final float COLOR_GREEN = 0.98F;
    private static final float COLOR_BLUE = 0.32F;

    /** 贴图横向每 2 格重复一次，纵向每 1 格重复一次，与边境一致。 */
    private static final double TEXTURE_SCALE_H = 2.0D;

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (!AshlandsWorld.isAshlandsWorld(level)) {
            return;
        }

        Set<Long> safeChunks = AshlandsWorld.clientSafeChunks();
        if (safeChunks.isEmpty()) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

        // 视野边缘淡出，超出渲染距离直接不画。
        double fadeDistance = (double) (minecraft.options.getEffectiveRenderDistance() * 16);
        if (fadeDistance <= 0.0D) {
            return;
        }

        List<double[]> walls = collectWalls(safeChunks, cameraPos, fadeDistance);
        if (walls.isEmpty()) {
            return;
        }

        double nearest = Double.MAX_VALUE;
        for (double[] wall : walls) {
            nearest = Math.min(nearest, wall[4]);
        }
        float fade = (float) Mth.clamp(Math.pow(1.0D - nearest / fadeDistance, 4.0D), 0.0D, 1.0D);
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
        for (double[] wall : walls) {
            drawWall(buffer, cameraPos, wall[0], wall[1], wall[2], wall[3], bottom, top, scroll);
        }

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

    /**
     * 只在「该区块安全、相邻区块不安全」的那一侧生成光幕，所以安全区可以是不规则的形状。
     * 每个元素是 {x0, z0, x1, z1, 到相机的最短距离}。
     */
    private static List<double[]> collectWalls(Set<Long> safeChunks, Vec3 cameraPos, double fadeDistance) {
        List<double[]> walls = new ArrayList<>();

        for (long key : safeChunks) {
            int chunkX = ChunkPos.getX(key);
            int chunkZ = ChunkPos.getZ(key);

            double minX = (double) chunkX * 16.0D;
            double maxX = minX + 16.0D;
            double minZ = (double) chunkZ * 16.0D;
            double maxZ = minZ + 16.0D;

            if (!safeChunks.contains(ChunkPos.asLong(chunkX, chunkZ - 1))) {
                addWall(walls, cameraPos, fadeDistance, minX, minZ, maxX, minZ);
            }
            if (!safeChunks.contains(ChunkPos.asLong(chunkX, chunkZ + 1))) {
                addWall(walls, cameraPos, fadeDistance, minX, maxZ, maxX, maxZ);
            }
            if (!safeChunks.contains(ChunkPos.asLong(chunkX - 1, chunkZ))) {
                addWall(walls, cameraPos, fadeDistance, minX, minZ, minX, maxZ);
            }
            if (!safeChunks.contains(ChunkPos.asLong(chunkX + 1, chunkZ))) {
                addWall(walls, cameraPos, fadeDistance, maxX, minZ, maxX, maxZ);
            }
        }

        return walls;
    }

    private static void addWall(List<double[]> walls, Vec3 cameraPos, double fadeDistance,
                                double x0, double z0, double x1, double z1) {
        double nearestX = Mth.clamp(cameraPos.x, Math.min(x0, x1), Math.max(x0, x1));
        double nearestZ = Mth.clamp(cameraPos.z, Math.min(z0, z1), Math.max(z0, z1));
        double dx = cameraPos.x - nearestX;
        double dz = cameraPos.z - nearestZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < fadeDistance) {
            walls.add(new double[]{x0, z0, x1, z1, distance});
        }
    }

    private static void drawWall(BufferBuilder buffer, Vec3 cameraPos,
                                 double x0, double z0, double x1, double z1,
                                 double bottom, double top, float scroll) {
        float u0 = (float) (x0 + z0) / (float) TEXTURE_SCALE_H;
        float u1 = (float) (x1 + z1) / (float) TEXTURE_SCALE_H;
        float vBottom = (float) bottom + scroll;
        float vTop = (float) top + scroll;

        vertex(buffer, cameraPos, x0, bottom, z0, u0, vBottom);
        vertex(buffer, cameraPos, x1, bottom, z1, u1, vBottom);
        vertex(buffer, cameraPos, x1, top, z1, u1, vTop);
        vertex(buffer, cameraPos, x0, top, z0, u0, vTop);
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
