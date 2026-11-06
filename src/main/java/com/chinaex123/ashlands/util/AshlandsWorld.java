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

    /** 沿视线找光幕时最多走多远；再远雾也看不见了。 */
    private static final double MAX_FOG_RAY = 192.0D;

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

    public static boolean isChunkAdjacentToSafeZone(ServerLevel level, int chunkX, int chunkZ) {
        return isChunkSafe(level, chunkX + 1, chunkZ)
                || isChunkSafe(level, chunkX - 1, chunkZ)
                || isChunkSafe(level, chunkX, chunkZ + 1)
                || isChunkSafe(level, chunkX, chunkZ - 1);
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

    /**
     * 客户端：沿着视线方向往前走，多远才会离开安全区（方块数）。
     * <p>
     * 这是雾气真正要用的值：回头看安全区内部时它很大，雾线就被推到视野之外，
     * 于是界内一点雾都不沾；朝光幕外看时它很小，雾正好从光幕那里开始。
     */
    public static double clientDistanceToUnsafeChunk(double blockX, double blockZ, double dirX, double dirZ) {
        Set<Long> safe = clientSafeChunks;
        if (safe.isEmpty()) {
            // 还没收到安全区数据：当作「一路都是安全区」，别在界内糊上雾。
            return MAX_FOG_RAY;
        }

        double length = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (length < 1.0E-4D) {
            return clientDistanceToUnsafeChunk(blockX, blockZ);
        }

        double stepX = dirX / length;
        double stepZ = dirZ / length;
        for (double distance = 0.0D; distance <= MAX_FOG_RAY; distance += 1.0D) {
            int chunkX = Mth.floor(blockX + stepX * distance) >> 4;
            int chunkZ = Mth.floor(blockZ + stepZ * distance) >> 4;
            if (!safe.contains(ChunkPos.asLong(chunkX, chunkZ))) {
                return distance;
            }
        }
        return MAX_FOG_RAY;
    }

    /**
     * 客户端：从某个位置到「最近的非安全区块」的距离（方块数）。
     * <p>
     * 安全区内返回的是到光幕的距离，安全区外为 0。
     */
    public static double clientDistanceToUnsafeChunk(double blockX, double blockZ) {
        Set<Long> safe = clientSafeChunks;
        if (safe.isEmpty()) {
            return 0.0D;
        }

        int chunkX = Mth.floor(blockX) >> 4;
        int chunkZ = Mth.floor(blockZ) >> 4;

        for (int ring = 0; ring <= MAX_FOG_SEARCH_RING; ring++) {
            double best = Double.MAX_VALUE;
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) {
                        continue;
                    }
                    if (safe.contains(ChunkPos.asLong(chunkX + dx, chunkZ + dz))) {
                        continue;
                    }

                    int minX = (chunkX + dx) << 4;
                    int minZ = (chunkZ + dz) << 4;
                    double distance = Math.max(axisDistance(blockX, minX), axisDistance(blockZ, minZ));
                    if (distance < best) {
                        best = distance;
                    }
                }
            }
            if (best != Double.MAX_VALUE) {
                return best;
            }
        }
        return Double.MAX_VALUE;
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
