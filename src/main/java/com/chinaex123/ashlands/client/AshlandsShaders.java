package com.chinaex123.ashlands.client;

import com.chinaex123.ashlands.Ashlands;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@EventBusSubscriber(modid = Ashlands.MODID, value = Dist.CLIENT)
public final class AshlandsShaders {

    private static ShaderInstance radiationRain;

    public static ShaderInstance radiationRain() {
        return radiationRain;
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                Ashlands.id("radiation_rain"), DefaultVertexFormat.PARTICLE), shader -> radiationRain = shader);
    }
}
