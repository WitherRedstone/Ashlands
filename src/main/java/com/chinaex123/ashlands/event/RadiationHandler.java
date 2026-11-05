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

    /** 饥饿值与饱和度的消耗节奏（每 2 秒一次） **/
    private static final int FOOD_INTERVAL = 40;

    /** 各等级扣血的间隔（刻）：1 级 60 秒、2 级 30 秒、3 级 10 秒 **/
    private static final int[] DAMAGE_INTERVAL = {1200, 600, 200};

    /** 各等级每次扣血的血量（2 点 = 1 颗心）：1 级半颗心、2 级 1 颗心、3 级 2 颗心 **/
    private static final float[] DAMAGE_AMOUNT = {1.0F, 2.0F, 4.0F};

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

        FoodData food = player.getFoodData();
        if (amplifier >= MAX_LEVEL - 1) {
            // 3 级：每刻锁定饥饿值与饱和度，任何食物都无法恢复。
            food.setFoodLevel(0);
            food.setSaturation(0.0F);
            food.setExhaustion(0.0F);
        } else if (now % FOOD_INTERVAL == 0) {
            if (amplifier == 0) {
                food.setFoodLevel(Math.max(0, food.getFoodLevel() - 1));
                food.setSaturation(Math.max(0.0F, food.getSaturationLevel() - 1.0F));
            } else {
                food.setFoodLevel(Math.max(0, food.getFoodLevel() - 3));
                food.setSaturation(Math.max(0.0F, food.getSaturationLevel() - 6.0F));
            }
        }

        // 扣血按「当前等级开始之后的时长」独立计时，每个玩家互不影响。
        long levelStart = infectedAt + (long) amplifier * ticksPerLevel();
        long localElapsed = now - levelStart;
        if (localElapsed > 0L && localElapsed % DAMAGE_INTERVAL[amplifier] == 0L) {
            hurt(player, DAMAGE_AMOUNT[amplifier]);
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
        long ticksPerLevel = ticksPerLevel();
        int level = 1 + (int) (elapsed / ticksPerLevel);
        return Math.max(0, Math.min(level, MAX_LEVEL) - 1);
    }

    /** 每提升一级辐射所需的刻数（配置项，默认 10 分钟） **/
    private static long ticksPerLevel() {
        double minutes = AshlandsConfig.MINUTES_PER_RADIATION_LEVEL.get();
        return Math.max(20L, (long) (minutes * 1200.0D));
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
