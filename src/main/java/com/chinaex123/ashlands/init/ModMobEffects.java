package com.chinaex123.ashlands.init;

import com.chinaex123.ashlands.Ashlands;
import com.chinaex123.ashlands.effect.RadiationMobEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface ModMobEffects {
    DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Ashlands.MODID);

    DeferredHolder<MobEffect, MobEffect> RADIATION = MOB_EFFECTS.register("radiation", RadiationMobEffect::new);

    static void register(IEventBus bus) {
        MOB_EFFECTS.register(bus);
    }
}
