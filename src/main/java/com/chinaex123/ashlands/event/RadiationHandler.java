package com.chinaex123.ashlands.event;

import com.chinaex123.ashlands.Ashlands;
import com.chinaex123.ashlands.config.AshlandsConfig;
import com.chinaex123.ashlands.init.ModAttachments;
import com.chinaex123.ashlands.init.ModMobEffects;
import com.chinaex123.ashlands.util.AshlandsWorld;
import com.chinaex123.ashlands.init.RadiationDamage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Ashlands.MODID)
public final class RadiationHandler {

    private static final int MAX_LEVEL = 3;
    private static final int LOW_LEVEL_INTERVAL = 40;
    private static final int HIGH_LEVEL_INTERVAL = 100;
    private static final int MOB_SCAN_INTERVAL = 40;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        if (!AshlandsWorld.isAshlandsWorld(level)) {
            return;
        }

        long now = level.getGameTime();

        if (AshlandsWorld.isInSafeZone(level, player.blockPosition()) && !isInfected(player)) {
            return;
        }

        long infectedAt = infect(player, now);
        int amplifier = radiationAmplifier(infectedAt, now);
        applyRadiation(player, amplifier);

        if (amplifier >= MAX_LEVEL - 1) {
            // 3 级：每刻锁定饥饿值与饱和度，任何食物都无法恢复。
            FoodData food = player.getFoodData();
            food.setFoodLevel(0);
            food.setSaturation(0.0F);
            food.setExhaustion(0.0F);
            if (now % HIGH_LEVEL_INTERVAL == 0) {
                hurt(player, 4.0F);
            }
        } else if (now % LOW_LEVEL_INTERVAL == 0) {
            FoodData food = player.getFoodData();
            if (amplifier == 0) {
                food.setFoodLevel(Math.max(0, food.getFoodLevel() - 1));
                food.setSaturation(Math.max(0.0F, food.getSaturationLevel() - 1.0F));
                if (player.getRandom().nextFloat() < 0.08F) {
                    hurt(player, 1.0F);
                }
            } else {
                food.setFoodLevel(Math.max(0, food.getFoodLevel() - 3));
                food.setSaturation(Math.max(0.0F, food.getSaturationLevel() - 6.0F));
                hurt(player, 2.0F);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!AshlandsWorld.isAshlandsWorld(level)) {
            return;
        }

        if (level.getGameTime() % MOB_SCAN_INTERVAL != 0) {
            return;
        }

        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof LivingEntity living) || living instanceof Player) {
                continue;
            }
            if (living.hasEffect(ModMobEffects.RADIATION)) {
                living.removeEffect(ModMobEffects.RADIATION);
            }
            if (isInfected(living)) {
                living.setData(ModAttachments.RADIATION_START, -1L);
            }
        }
    }

    private static boolean isInfected(LivingEntity entity) {
        return entity.getExistingData(ModAttachments.RADIATION_START).orElse(-1L) >= 0L;
    }

    private static long infect(LivingEntity entity, long now) {
        long infectedAt = entity.getExistingData(ModAttachments.RADIATION_START).orElse(-1L);
        if (infectedAt >= 0L) {
            return infectedAt;
        }
        entity.setData(ModAttachments.RADIATION_START, now);
        return now;
    }

    private static int radiationAmplifier(long infectedAt, long now) {
        long elapsed = Math.max(0L, now - infectedAt);
        double minutes = AshlandsConfig.MINUTES_PER_RADIATION_LEVEL.get();
        long ticksPerLevel = Math.max(20L, (long) (minutes * 1200.0D));
        int level = 1 + (int) (elapsed / ticksPerLevel);
        return Math.max(0, Math.min(level, MAX_LEVEL) - 1);
    }

    private static void applyRadiation(LivingEntity entity, int amplifier) {
        MobEffectInstance current = entity.getEffect(ModMobEffects.RADIATION);
        if (current != null && current.getAmplifier() == amplifier && current.isInfiniteDuration()) {
            return;
        }
        entity.removeEffect(ModMobEffects.RADIATION);
        entity.addEffect(new MobEffectInstance(
                ModMobEffects.RADIATION,
                MobEffectInstance.INFINITE_DURATION,
                amplifier,
                false,
                true,
                true));
    }

    private static void hurt(Player player, float amount) {
        if (!AshlandsConfig.RADIATION_DAMAGE.get()) {
            return;
        }
        player.hurt(RadiationDamage.source(player.level()), amount);
    }
}
