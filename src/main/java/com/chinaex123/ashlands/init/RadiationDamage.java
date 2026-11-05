package com.chinaex123.ashlands.init;

import com.chinaex123.ashlands.Ashlands;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public interface RadiationDamage {

    ResourceKey<DamageType> RADIATION = ResourceKey.create(Registries.DAMAGE_TYPE, Ashlands.id("radiation"));

    static DamageSource source(Level level) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(RADIATION));
    }
}
