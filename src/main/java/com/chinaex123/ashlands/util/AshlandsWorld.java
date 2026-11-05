package com.chinaex123.ashlands.util;

import com.chinaex123.ashlands.config.AshlandsConfig;
import com.chinaex123.ashlands.worldgen.AshlandsChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AshlandsWorld {

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
}
