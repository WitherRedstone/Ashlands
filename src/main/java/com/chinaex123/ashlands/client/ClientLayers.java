package com.chinaex123.ashlands.client;

import com.chinaex123.ashlands.Ashlands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Ashlands.MODID, value = Dist.CLIENT)
public final class ClientLayers {

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS,
                Ashlands.id("radiation_vignette"),
                new RadiationVignetteLayer());
    }
}
