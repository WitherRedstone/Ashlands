package com.chinaex123.ashlands.event;

import com.chinaex123.ashlands.Ashlands;
import com.chinaex123.ashlands.config.AshlandsConfig;
import com.chinaex123.ashlands.util.AshlandsWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.HangingRootsBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = Ashlands.MODID)
public final class DeadForestHandler {

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !AshlandsWorld.isAshlandsWorld(level)) {
            return;
        }
        BlockPos spawn = level.getSharedSpawnPos();
        int radius = AshlandsWorld.safeRadiusChunks();
        Ashlands.LOGGER.info("Ashlands safe zone: radius {} chunk(s) = {}x{} chunks, centered on chunk ({}, {})",
                radius, radius * 2 + 1, radius * 2 + 1, spawn.getX() >> 4, spawn.getZ() >> 4);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!AshlandsConfig.STRIP_TREES_OUTSIDE_SAFE_ZONE.get()) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level) || !AshlandsWorld.isAshlandsWorld(level)) {
            return;
        }
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }

        ChunkPos pos = chunk.getPos();
        if (AshlandsWorld.isChunkSafe(level, pos.x, pos.z)) {
            return;
        }

        stripTrees(chunk);
    }

    private static void stripTrees(LevelChunk chunk) {
        Set<BlockPos> decorations = new HashSet<>();
        chunk.findBlocks(state -> state.getBlock() instanceof VineBlock || state.getBlock() instanceof CocoaBlock,
                (blockPos, state) -> decorations.add(blockPos.immutable()));

        List<BlockPos> leaves = new ArrayList<>();
        chunk.findBlocks(state -> state.is(BlockTags.LEAVES),
                (blockPos, state) -> leaves.add(blockPos.immutable()));
        if (leaves.isEmpty() && decorations.isEmpty()) {
            return;
        }

        ChunkPos chunkPos = chunk.getPos();
        Set<BlockPos> logs = new HashSet<>();

        for (BlockPos leaf : leaves) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbour = leaf.relative(direction);
                if (!inChunk(chunkPos, neighbour)) {
                    continue;
                }
                BlockState state = chunk.getBlockState(neighbour);
                if (isLeafDecoration(state)) {
                    decorations.add(neighbour);
                } else if (state.is(BlockTags.LOGS)) {
                    logs.add(neighbour);
                }
            }
        }

        for (BlockPos log : logs) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbour = log.relative(direction);
                if (!inChunk(chunkPos, neighbour) || !isTrunkDecoration(chunk.getBlockState(neighbour))) {
                    continue;
                }
                decorations.add(neighbour);
            }
        }

        BlockState air = Blocks.AIR.defaultBlockState();
        for (BlockPos leaf : leaves) {
            chunk.setBlockState(leaf, air, false);
        }
        for (BlockPos pos : decorations) {
            chunk.setBlockState(pos, air, false);
        }

        Ashlands.LOGGER.trace("Ashlands dead forest: stripped {} leaves and {} decorations in chunk ({}, {})",
                leaves.size(), decorations.size(), chunkPos.x, chunkPos.z);
    }

    private static boolean inChunk(ChunkPos chunkPos, BlockPos pos) {
        return (pos.getX() >> 4) == chunkPos.x && (pos.getZ() >> 4) == chunkPos.z;
    }

    private static boolean isLeafDecoration(BlockState state) {
        Block block = state.getBlock();
        return block instanceof SnowLayerBlock
                || block instanceof MultifaceBlock
                || block instanceof HangingRootsBlock;
    }

    private static boolean isTrunkDecoration(BlockState state) {
        Block block = state.getBlock();
        return block instanceof MultifaceBlock
                || block instanceof HangingRootsBlock;
    }
}
