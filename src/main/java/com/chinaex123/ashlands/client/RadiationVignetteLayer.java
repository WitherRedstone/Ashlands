package com.chinaex123.ashlands.client;

import com.chinaex123.ashlands.Ashlands;
import com.chinaex123.ashlands.init.ModMobEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;

public class RadiationVignetteLayer implements LayeredDraw.Layer {

    private static final ResourceLocation VIGNETTE = Ashlands.id("textures/misc/radiation_vignette.png");

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        MobEffectInstance effect = minecraft.player.getEffect(ModMobEffects.RADIATION);
        if (effect == null) {
            return;
        }

        int level = Mth.clamp(effect.getAmplifier(), 0, 2);

        // 缓慢呼吸的脉动，配上等级越高压得越重。
        float pulse = 0.88F + 0.12F * Mth.sin((Util.getMillis() % 3600L) / 3600.0F * Mth.TWO_PI);
        float alpha = (0.42F + level * 0.26F) * pulse;

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.blit(VIGNETTE, 0, 0, width, height, 0.0F, 0.0F, 256, 256, 256, 256);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}
