package com.chinaex123.ashlands.client;

import com.chinaex123.ashlands.Ashlands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Ashlands.MODID, dist = Dist.CLIENT)
public class AshlandsClient {

    public AshlandsClient(ModContainer container) {
        NeoForge.EVENT_BUS.addListener(RadiationCurtainRenderer::onRenderLevelStage);
    }
}
