package com.chinaex123.ashlands.data;

import com.chinaex123.ashlands.Ashlands;
import com.chinaex123.ashlands.init.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Ashlands.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(ModBlocks.PURIFIER.get(), cubeAll(ModBlocks.PURIFIER.get()));
    }
}
