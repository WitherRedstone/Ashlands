package com.chinaex123.ashlands.init;

import com.chinaex123.ashlands.Ashlands;
import com.chinaex123.ashlands.block.PurifierBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public interface ModBlocks {
    DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Ashlands.MODID);

    DeferredBlock<PurifierBlock> PURIFIER = registerBlocks("purifier", () -> new PurifierBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GRAY)
            .sound(SoundType.STONE)
            .strength(2.5f, 6.0f)
            .noOcclusion()
            .requiresCorrectToolForDrops()
            .lightLevel(state -> 9)
    ), Rarity.RARE);

    private static <T extends Block> void registerBlockItems(String name, DeferredBlock<T> block, Rarity rarity) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().rarity(rarity)));
    }

    private static <T extends Block> void registerBlockItems(String name, DeferredBlock<T> block) {
        registerBlockItems(name, block, Rarity.COMMON);
    }

    private static <T extends Block> DeferredBlock<T> registerBlocks(String name, Supplier<T> block, Rarity rarity) {
        DeferredBlock<T> blocks = BLOCKS.register(name, block);
        registerBlockItems(name, blocks, rarity);
        return blocks;
    }

    private static <T extends Block> DeferredBlock<T> registerBlocks(String name, Supplier<T> block) {
        return registerBlocks(name, block, Rarity.COMMON);
    }

    static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
