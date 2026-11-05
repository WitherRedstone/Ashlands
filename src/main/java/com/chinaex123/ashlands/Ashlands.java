package com.chinaex123.ashlands;

import com.chinaex123.ashlands.config.AshlandsConfig;
import com.chinaex123.ashlands.init.*;
import com.chinaex123.ashlands.network.AshlandsNetwork;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Ashlands.MODID)
public class Ashlands {
    public static final String MODID = "ashlands";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Ashlands(IEventBus modEventBus, ModContainer modContainer) {
        ModChunkGenerators.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        modEventBus.addListener(AshlandsNetwork::register);
        modContainer.registerConfig(ModConfig.Type.COMMON, AshlandsConfig.SPEC);
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }
}
