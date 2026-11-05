package com.chinaex123.ashlands.mixin.client;

import com.chinaex123.ashlands.client.RadiationAtmosphere;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 世界渲染器 Mixin。
 * <p>
 * 灰域世界里的天空完全由模组自己接管
 * 和云层都不再绘制，安全区内与光幕之外看到的是同一片天空。
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    /**
     * 屏蔽原版天空。
     *
     * @param frustumMatrix    视锥矩阵
     * @param projectionMatrix 投影矩阵
     * @param partialTick      部分 tick 插值
     * @param camera           相机
     * @param isFoggy          是否处于雾中
     * @param skyFogSetup      天空雾设置
     * @param callbackInfo     回调信息
     */
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void ashlands$hideVanillaSky(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo callbackInfo) {
        if (RadiationAtmosphere.hasAtmosphere()) {
            callbackInfo.cancel();
        }
    }

    /**
     * 屏蔽云层。
     *
     * @param poseStack        姿势栈
     * @param frustumMatrix    视锥矩阵
     * @param projectionMatrix 投影矩阵
     * @param partialTick      部分 tick 插值
     * @param camX             相机 X
     * @param camY             相机 Y
     * @param camZ             相机 Z
     * @param callbackInfo     回调信息
     */
    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void ashlands$hideClouds(PoseStack poseStack, Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ, CallbackInfo callbackInfo) {
        if (RadiationAtmosphere.hasAtmosphere()) {
            callbackInfo.cancel();
        }
    }
}
