package com.chinaex123.ashlands.event;

import com.chinaex123.ashlands.Ashlands;
import com.chinaex123.ashlands.config.AshlandsConfig;
import com.chinaex123.ashlands.util.AshlandsWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = Ashlands.MODID)
public final class DeadForestHandler {

    /** 每刻最多处理的区块数量。 */
    private static final int CHUNKS_PER_TICK = 4;

    /** 区块尚未就绪时的最大重试次数 */
    private static final int MAX_ATTEMPTS = 600;

    /** 世界加载时预登记的出生点周围区块范围 */
    private static final int PRELOAD_MARGIN = 8;

    /** 各维度的处理队列，按轮询方式取出，保证每个区块都能被尝试到。 */
    private static final Map<ResourceKey<Level>, ArrayDeque<Task>> QUEUES = new HashMap<>();

    /** 已在队列中的区块，用于去重。 */
    private static final Map<ResourceKey<Level>, Set<Long>> QUEUED = new HashMap<>();

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!AshlandsWorld.isAshlandsWorld(level)) {
            forget(level.dimension());
            return;
        }
        BlockPos spawn = level.getSharedSpawnPos();
        int radius = AshlandsWorld.safeRadiusChunks();
        Ashlands.LOGGER.info("Ashlands safe zone: radius {} chunk(s) = {}x{} chunks, centered on chunk ({}, {})",
                radius, radius * 2 + 1, radius * 2 + 1, spawn.getX() >> 4, spawn.getZ() >> 4);

        if (!AshlandsConfig.STRIP_TREES_OUTSIDE_SAFE_ZONE.get()) {
            return;
        }

        int margin = radius + PRELOAD_MARGIN;
        int queued = 0;
        for (int x = (spawn.getX() >> 4) - margin; x <= (spawn.getX() >> 4) + margin; x++) {
            for (int z = (spawn.getZ() >> 4) - margin; z <= (spawn.getZ() >> 4) + margin; z++) {
                if (!AshlandsWorld.isChunkInSafeZone(spawn, x, z, radius) && enqueue(level.dimension(), ChunkPos.asLong(x, z))) {
                    queued++;
                }
            }
        }
        Ashlands.LOGGER.debug("Ashlands dead forest: primed {} chunks around spawn chunk ({}, {})",
                queued, spawn.getX() >> 4, spawn.getZ() >> 4);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!AshlandsConfig.STRIP_TREES_OUTSIDE_SAFE_ZONE.get()) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level) || !AshlandsWorld.isAshlandsWorld(level)) {
            return;
        }

        ChunkPos pos = event.getChunk().getPos();
        if (AshlandsWorld.isChunkInSafeZone(level.getSharedSpawnPos(), pos.x, pos.z, AshlandsWorld.safeRadiusChunks())) {
            return;
        }
        enqueue(level.dimension(), pos.toLong());
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ArrayDeque<Task> queue = QUEUES.get(level.dimension());
        if (queue == null || queue.isEmpty()) {
            return;
        }
        if (!AshlandsWorld.isAshlandsWorld(level)) {
            forget(level.dimension());
            return;
        }

        for (int processed = 0; processed < CHUNKS_PER_TICK; processed++) {
            Task task = queue.pollFirst();
            if (task == null) {
                break;
            }
            ChunkPos pos = new ChunkPos(task.pos());
            if (stripLeaves(level, pos)) {
                removeQueued(level.dimension(), task.pos());
            } else if (task.attempts() > 1) {
                // 区块还没就绪：放到队尾，等下一轮再试
                queue.addLast(new Task(task.pos(), task.attempts() - 1));
            } else {
                removeQueued(level.dimension(), task.pos());
                Ashlands.LOGGER.debug("Ashlands dead forest: gave up chunk ({}, {}) - never became loaded", pos.x, pos.z);
            }
        }
    }

    private static boolean stripLeaves(ServerLevel level, ChunkPos pos) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
        if (chunk == null) {
            return false;
        }

        List<BlockPos> leaves = new ArrayList<>();
        chunk.findBlocks(state -> state.is(BlockTags.LEAVES),
                (blockPos, state) -> leaves.add(blockPos.immutable()));
        if (leaves.isEmpty()) {
            return true;
        }

        BlockState air = Blocks.AIR.defaultBlockState();
        int stripped = 0;
        for (BlockPos leaf : leaves) {
            if (level.getChunkSource().getChunkNow(pos.x, pos.z) != chunk) {
                Ashlands.LOGGER.debug("Ashlands dead forest: chunk ({}, {}) unloaded mid-strip, {} leaves left", pos.x, pos.z, leaves.size() - stripped);
                return true;
            }
            BlockState old = chunk.getBlockState(leaf);
            if (!old.is(BlockTags.LEAVES)) {
                continue;
            }
            if (chunk.setBlockState(leaf, air, false) != null) {
                level.sendBlockUpdated(leaf, old, air, Block.UPDATE_CLIENTS);
                stripped++;
            }
        }

        Ashlands.LOGGER.debug("Ashlands dead forest: stripped {} leaves in chunk ({}, {})", stripped, pos.x, pos.z);
        return true;
    }

    private static boolean enqueue(ResourceKey<Level> dimension, long packedPos) {
        Set<Long> queued = QUEUED.computeIfAbsent(dimension, key -> new HashSet<>());
        if (!queued.add(packedPos)) {
            return false;
        }
        QUEUES.computeIfAbsent(dimension, key -> new ArrayDeque<>()).addLast(new Task(packedPos, MAX_ATTEMPTS));
        return true;
    }

    private static void removeQueued(ResourceKey<Level> dimension, long packedPos) {
        Set<Long> queued = QUEUED.get(dimension);
        if (queued != null) {
            queued.remove(packedPos);
            if (queued.isEmpty()) {
                QUEUED.remove(dimension);
            }
        }
    }

    private static void forget(ResourceKey<Level> dimension) {
        QUEUES.remove(dimension);
        QUEUED.remove(dimension);
    }

    private record Task(long pos, int attempts) {
    }
}
