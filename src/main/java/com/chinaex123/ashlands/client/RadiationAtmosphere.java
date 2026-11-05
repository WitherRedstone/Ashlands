package com.chinaex123.ashlands.client;

import com.chinaex123.ashlands.util.AshlandsWorld;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public final class RadiationAtmosphere {

    /** 雾色向浓绿化靠拢的比例：灰域中浓，安全区内只轻微带一点，免得地平线发蓝。 */
    private static final float FOG_COLOR_BLEND_WASTELAND = 0.85F;
    private static final float FOG_COLOR_BLEND_SAFE = 0.45F;

    /** 灰域中：视野立刻被压到几十格以内。 */
    private static final float WASTELAND_FOG_NEAR = 6.0F;
    private static final float WASTELAND_FOG_FAR = 56.0F;

    /** 刚跨出光幕时雾还比较远，随着淡入再压到眼前。 */
    private static final float ENTER_FOG_NEAR = 40.0F;
    private static final float ENTER_FOG_FAR = 96.0F;

    /** 跨出光幕多少格之后开始淡入效果。 */
    private static final double RAMP_OFFSET = 0.5D;

    /** 淡入过程持续多少格。 */
    private static final double RAMP_LENGTH = 2.0D;

    /** 雾颜色，偏黄绿，像被污染的空气。 */
    private static final float FOG_RED = 0.47F;
    private static final float FOG_GREEN = 0.60F;
    private static final float FOG_BLUE = 0.33F;

    /** 天空颜色，上方更暗更冷，靠近地平线更亮。 */
    private static final float SKY_TOP_RED = 0.06F;
    private static final float SKY_TOP_GREEN = 0.11F;
    private static final float SKY_TOP_BLUE = 0.05F;
    private static final float SKY_BOTTOM_RED = 0.31F;
    private static final float SKY_BOTTOM_GREEN = 0.43F;
    private static final float SKY_BOTTOM_BLUE = 0.20F;

    private static long cachedTick = Long.MIN_VALUE;
    private static float cachedIntensity;

    private RadiationAtmosphere() {
    }

    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (!hasAtmosphere()) {
            return;
        }

        // 安全区内完全不接管雾的浓淡，只是把地平线那圈雾色稍微调绿一点，和绿色天空接得上。
        float blend = intensity() > 0.0F ? FOG_COLOR_BLEND_WASTELAND : FOG_COLOR_BLEND_SAFE;
        event.setRed(Mth.lerp(blend, event.getRed(), FOG_RED));
        event.setGreen(Mth.lerp(blend, event.getGreen(), FOG_GREEN));
        event.setBlue(Mth.lerp(blend, event.getBlue(), FOG_BLUE));
    }

    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) {
            return;
        }

        Camera camera = event.getCamera();
        if (camera.getFluidInCamera() != FogType.NONE) {
            return;
        }

        if (!hasAtmosphere()) {
            return;
        }

        // 只有跨出光幕之后才接管雾。
        float t = intensity();
        if (t <= 0.0F) {
            return;
        }

        float far = Math.min(event.getFarPlaneDistance(), Mth.lerp(t, ENTER_FOG_FAR, WASTELAND_FOG_FAR));
        float near = Mth.lerp(t, ENTER_FOG_NEAR, WASTELAND_FOG_NEAR);

        event.setFarPlaneDistance(far);
        event.setNearPlaneDistance(Math.clamp(near, 0.5F, far * 0.9F));
        event.setCanceled(true);
    }

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }

        if (!hasAtmosphere()) {
            return;
        }

        renderSkyOverlay(event.getProjectionMatrix(), 1.0F);
    }

    private static void renderSkyOverlay(Matrix4f projectionMatrix, float intensity) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.identity();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f(), VertexSorting.DISTANCE_TO_ORIGIN);

        float bottomAlpha = 0.70F * intensity;
        float topAlpha = 0.94F * intensity;
        Matrix4f identity = new Matrix4f();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(identity, -1.0F, -1.0F, 0.0F).setColor(SKY_BOTTOM_RED, SKY_BOTTOM_GREEN, SKY_BOTTOM_BLUE, bottomAlpha);
        buffer.addVertex(identity, 1.0F, -1.0F, 0.0F).setColor(SKY_BOTTOM_RED, SKY_BOTTOM_GREEN, SKY_BOTTOM_BLUE, bottomAlpha);
        buffer.addVertex(identity, 1.0F, 1.0F, 0.0F).setColor(SKY_TOP_RED, SKY_TOP_GREEN, SKY_TOP_BLUE, topAlpha);
        buffer.addVertex(identity, -1.0F, 1.0F, 0.0F).setColor(SKY_TOP_RED, SKY_TOP_GREEN, SKY_TOP_BLUE, topAlpha);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        modelView.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.DISTANCE_TO_ORIGIN);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static float intensity() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return 0.0F;
        }
        if (!AshlandsWorld.isAshlandsWorld(minecraft.level)) {
            return 0.0F;
        }

        long tick = minecraft.level.getGameTime();
        if (tick == cachedTick) {
            return cachedIntensity;
        }
        cachedTick = tick;

        double distance = AshlandsWorld.clientDistanceToSafeZone(
                minecraft.player.getX(),
                minecraft.player.getZ());
        // 客户端还没收到安全区数据时，距离是 Integer.MAX_VALUE，这时按「在安全区里」处理。
        if (distance >= Integer.MAX_VALUE - 1) {
            cachedIntensity = 0.0F;
            return cachedIntensity;
        }

        double beyond = distance - RAMP_OFFSET;
        cachedIntensity = (float) Math.clamp(beyond / RAMP_LENGTH, 0.0D, 1.0D);
        return cachedIntensity;
    }

    public static boolean hasAtmosphere() {
        Minecraft minecraft = Minecraft.getInstance();
        return AshlandsWorld.isAshlandsWorld(minecraft.level);
    }
}
