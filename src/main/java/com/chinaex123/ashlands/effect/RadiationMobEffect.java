package com.chinaex123.ashlands.effect;

import com.chinaex123.ashlands.config.AshlandsConfig;
import com.chinaex123.ashlands.init.RadiationDamage;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.neoforged.neoforge.common.EffectCure;

import java.util.Set;

public class RadiationMobEffect extends MobEffect {

    public RadiationMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x7CFC00);
    }

    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide || !(entity instanceof Player player)) {
            return true;
        }
        if (player.isCreative() || player.isSpectator()) {
            return true;
        }

        int level = Math.min(amplifier, 2);
        int tick = entity.tickCount;

        drainFood(player, level, tick);
        damagePlayer(player, level, tick);

        return true;
    }

    private static void drainFood(Player player, int level, int tick) {
        FoodData food = player.getFoodData();

        if (level >= 2) {
            if (AshlandsConfig.LEVEL3_LOCKS_FOOD.get()) {
                food.setFoodLevel(0);
                food.setSaturation(0.0F);
                food.setExhaustion(0.0F);
            }
            return;
        }

        int interval = level == 0
                ? AshlandsConfig.LEVEL1_FOOD_INTERVAL.get()
                : AshlandsConfig.LEVEL2_FOOD_INTERVAL.get();
        if (tick % interval != 0) {
            return;
        }

        int foodDrain = level == 0
                ? AshlandsConfig.LEVEL1_FOOD_DRAIN.get()
                : AshlandsConfig.LEVEL2_FOOD_DRAIN.get();
        double saturationDrain = level == 0
                ? AshlandsConfig.LEVEL1_SATURATION_DRAIN.get()
                : AshlandsConfig.LEVEL2_SATURATION_DRAIN.get();

        if (foodDrain > 0) {
            food.setFoodLevel(Math.max(0, food.getFoodLevel() - foodDrain));
        }
        if (saturationDrain > 0.0D) {
            food.setSaturation(Math.max(0.0F, food.getSaturationLevel() - (float) saturationDrain));
        }
    }

    private static void damagePlayer(Player player, int level, int tick) {
        if (!AshlandsConfig.RADIATION_DAMAGE.get()) {
            return;
        }

        double amount = switch (level) {
            case 0 -> AshlandsConfig.LEVEL1_DAMAGE.get();
            case 1 -> AshlandsConfig.LEVEL2_DAMAGE.get();
            default -> AshlandsConfig.LEVEL3_DAMAGE.get();
        };
        if (amount <= 0.0D) {
            return;
        }

        int interval = switch (level) {
            case 0 -> AshlandsConfig.LEVEL1_DAMAGE_INTERVAL.get();
            case 1 -> AshlandsConfig.LEVEL2_DAMAGE_INTERVAL.get();
            default -> AshlandsConfig.LEVEL3_DAMAGE_INTERVAL.get();
        };
        if (tick % interval != 0) {
            return;
        }

        player.hurt(RadiationDamage.source(player.level()), (float) amount);
    }
}
