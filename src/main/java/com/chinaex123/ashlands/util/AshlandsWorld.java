package com.chinaex123.ashlands.util;

import com.chinaex123.ashlands.config.AshlandsConfig;
import com.chinaex123.ashlands.worldgen.AshlandsChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class AshlandsWorld {

    private static volatile boolean clientActive = false;
    private static volatile int clientCenterChunkX = 0;
    private static volatile int clientCenterChunkZ = 0;
    private static volatile int clientRadius = 1;

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

    public static int centerChunkX(ServerLevel level) {
        return level.getSharedSpawnPos().getX() >> 4;
    }

    public static int centerChunkZ(ServerLevel level) {
        return level.getSharedSpawnPos().getZ() >> 4;
    }

    public static boolean isInSafeZone(BlockPos pos, int centerChunkX, int centerChunkZ, int radius) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        return Math.abs(chunkX - centerChunkX) <= radius && Math.abs(chunkZ - centerChunkZ) <= radius;
    }

    public static boolean isChunkInSafeZone(BlockPos spawnPos, int chunkX, int chunkZ, int radius) {
        int centerX = spawnPos.getX() >> 4;
        int centerZ = spawnPos.getZ() >> 4;
        return Math.abs(chunkX - centerX) <= radius && Math.abs(chunkZ - centerZ) <= radius;
    }

    public static void updateClientData(boolean active, int centerChunkX, int centerChunkZ, int radius) {
        clientActive = active;
        clientCenterChunkX = centerChunkX;
        clientCenterChunkZ = centerChunkZ;
        clientRadius = radius;
    }

    public static boolean isClientActive() {
        return clientActive;
    }

    public static int clientCenterChunkX() {
        return clientCenterChunkX;
    }

    public static int clientCenterChunkZ() {
        return clientCenterChunkZ;
    }

    public static int clientRadius() {
        return clientRadius;
    }
}
