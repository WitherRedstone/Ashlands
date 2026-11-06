package com.chinaex123.ashlands.event;

import com.chinaex123.ashlands.Ashlands;
import com.chinaex123.ashlands.config.AshlandsConfig;
import com.chinaex123.ashlands.init.ModAttachments;
import com.chinaex123.ashlands.init.ModMobEffects;
import com.chinaex123.ashlands.util.AshlandsWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Ashlands.MODID)
public final class RadiationHandler {

    private static final int MAX_LEVEL = 3;
    private static final int MOB_SCAN_INTERVAL = 40;

    private static final int POISON_INTERVAL = 20;
    private static final int POISON_DURATION = 100;

    private static final float RAIN_RADIATION_CHANCE = 0.05F;

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
        boolean inSafeZone = AshlandsWorld.isInSafeZone(level, player.blockPosition());

        handleWeather(level, player, inSafeZone, now);

        if (inSafeZone && !isInfected(player)) {
            return;
        }

        long infectedAt = infect(player, now);
        int amplifier = radiationAmplifier(infectedAt, now);
        ensureRadiation(player, amplifier);
    }

    private static void handleWeather(ServerLevel level, ServerPlayer player, boolean inSafeZone, long now) {
        if (now % POISON_INTERVAL != 0L) {
            return;
        }

        if (isInPrecipitation(level, player)) {
            applyPoison(player);
            if (player.getRandom().nextFloat() < RAIN_RADIATION_CHANCE) {
                ensureRadiation(player, radiationAmplifier(infect(player, now), now));
            }
        }

        if (!inSafeZone && player.isInWater()) {
            applyPoison(player);
        }
    }

    private static boolean isInPrecipitation(ServerLevel level, ServerPlayer player) {
        if (!level.isRaining()) {
            return false;
        }

        BlockPos pos = player.blockPosition();
        if (!level.canSeeSky(pos)) {
            return false;
        }

        return level.getBiome(pos).value().hasPrecipitation();
    }

    private static void applyPoison(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION, 0, false, true, true));
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

    private static long ticksPerLevel() {
        return Math.max(20L, AshlandsConfig.TICKS_PER_RADIATION_LEVEL.get());
    }

    private static void ensureRadiation(Player player, int amplifier) {
        MobEffectInstance current = player.getEffect(ModMobEffects.RADIATION);
        if (current == null) {
            player.addEffect(new MobEffectInstance(
                    ModMobEffects.RADIATION,
                    MobEffectInstance.INFINITE_DURATION,
                    amplifier,
                    false,
                    true,
                    true));
            return;
        }
        if (current.getAmplifier() >= amplifier) {
            return;
        }

        int duration = current.isInfiniteDuration() ? MobEffectInstance.INFINITE_DURATION : current.getDuration();
        player.removeEffect(ModMobEffects.RADIATION);
        player.addEffect(new MobEffectInstance(
                ModMobEffects.RADIATION,
                duration,
                amplifier,
                false,
                true,
                true));
    }
}
