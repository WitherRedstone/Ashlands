package com.chinaex123.ashlands.init;

import com.chinaex123.ashlands.Ashlands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface ModCreativeTabs {
    DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ashlands.MODID);

    DeferredHolder<CreativeModeTab, CreativeModeTab> ASHLANDS_TAB =
            CREATIVE_MODE_TABS.register(Ashlands.MODID, () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.PURIFIER.get()))
                    .title(Component.translatable("itemGroup.ashlands"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(ModBlocks.PURIFIER.get());

                    })
                    .build());

    static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
