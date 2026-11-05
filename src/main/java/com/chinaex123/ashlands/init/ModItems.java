package com.chinaex123.ashlands.init;

import com.chinaex123.ashlands.Ashlands;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface ModItems {
    DeferredRegister.Items ITEMS = DeferredRegister.createItems(Ashlands.MODID);

    static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
