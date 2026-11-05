package com.chinaex123.ashlands.util;

import com.chinaex123.ashlands.config.AshlandsConfig;
import com.chinaex123.ashlands.worldgen.AshlandsChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AshlandsWorld {

    /** 计算雾气起点时，最多向外找这么多圈区块；再远就当作「安全区极大」。 */
    private static final int MAX_FOG_SEARCH_RING = 24;

    private static volatile boolean clientActive = false;
    private static volatile Set<Long> clientSafeChunks = Set.of();

    public static boolean isAshlandsWorld(Level level) {
        if (level == null || level.dimension() != Level.OVERWORLD) {
            return false;
        }
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getChunkSource().getGenerator() instanceof AshlandsChunkGenerator;
        }
        return clientActive;
    }

    public static int safeRadiusChunks() {
        return AshlandsConfig.SAFE_RADIUS_CHUNKS.get();
    }

    public static boolean isChunkSafe(ServerLevel level, int chunkX, int chunkZ) {
        return isChunkInSafeZone(level.getSharedSpawnPos(), chunkX, chunkZ, safeRadiusChunks())
                || PurifiedChunks.get(level).contains(chunkX, chunkZ);
    }

    public static boolean isInSafeZone(ServerLevel level, BlockPos pos) {
        return isChunkSafe(level, pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static boolean isChunkInSafeZone(BlockPos spawnPos, int chunkX, int chunkZ, int radius) {
        int centerX = spawnPos.getX() >> 4;
        int centerZ = spawnPos.getZ() >> 4;
        return Math.abs(chunkX - centerX) <= radius && Math.abs(chunkZ - centerZ) <= radius;
    }

    public static void updateClientData(boolean active, int centerChunkX, int centerChunkZ, int radius, List<Long> purified) {
        clientActive = active;

        Set<Long> safe = new HashSet<>(purified);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                safe.add(ChunkPos.asLong(centerChunkX + dx, centerChunkZ + dz));
            }
        }
        clientSafeChunks = Set.copyOf(safe);
    }

    public static Set<Long> clientSafeChunks() {
        return clientSafeChunks;
    }

    public static int clientDistanceToSafeZone(int blockX, int blockZ) {
        Set<Long> safe = clientSafeChunks;
        if (safe.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        int best = Integer.MAX_VALUE;
        for (long key : safe) {
            int chunkX = ChunkPos.getX(key);
            int chunkZ = ChunkPos.getZ(key);

            int minX = chunkX << 4;
            int minZ = chunkZ << 4;
            int dx = axisDistance(blockX, minX);
            int dz = axisDistance(blockZ, minZ);
            int distance = Math.max(dx, dz);
            if (distance < best) {
                best = distance;
                if (best == 0) {
                    return 0;
                }
            }
        }
        return best;
    }

    public static double clientDistanceToSafeZone(double blockX, double blockZ) {
        Set<Long> safe = clientSafeChunks;
        if (safe.isEmpty()) {
            return 0.0D;
        }

        double best = Double.MAX_VALUE;
        for (long key : safe) {
            int minX = ChunkPos.getX(key) << 4;
            int minZ = ChunkPos.getZ(key) << 4;

            double dx = axisDistance(blockX, minX);
            double dz = axisDistance(blockZ, minZ);
            double distance = Math.max(dx, dz);
            if (distance < best) {
                best = distance;
                if (best <= 0.0D) {
                    return 0.0D;
                }
            }
        }
        return best;
    }

    private static double axisDistance(double value, int chunkMin) {
        double chunkMax = chunkMin + 16.0D;
        if (value < chunkMin) {
            return chunkMin - value;
        }
        if (value > chunkMax) {
            return value - chunkMax;
        }
        return 0.0D;
    }

    private static int axisDistance(int value, int chunkMin) {
        int chunkMax = chunkMin + 15;
        if (value < chunkMin) {
            return chunkMin - value;
        }
        if (value > chunkMax) {
            return value - chunkMax;
        }
        return 0;
    }
}
