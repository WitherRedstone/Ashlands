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
        if (AshlandsWorld.isChunkInSafeZone(level.getSharedSpawnPos(), pos.x, pos.z, AshlandsWorld.safeRadiusChunks())) {
            return;
        }

        stripLeaves(chunk);
    }

    private static void stripLeaves(LevelChunk chunk) {
        List<BlockPos> leaves = new ArrayList<>();
        chunk.findBlocks(state -> state.is(BlockTags.LEAVES),
                (blockPos, state) -> leaves.add(blockPos.immutable()));
        if (leaves.isEmpty()) {
            return;
        }

        ChunkPos chunkPos = chunk.getPos();
        BlockState air = Blocks.AIR.defaultBlockState();
        Set<BlockPos> hanging = new HashSet<>();

        for (BlockPos leaf : leaves) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbour = leaf.relative(direction);
                if ((neighbour.getX() >> 4) != chunkPos.x || (neighbour.getZ() >> 4) != chunkPos.z) {
                    continue;
                }
                if (hangsOnLeaves(chunk.getBlockState(neighbour))) {
                    hanging.add(neighbour);
                }
            }
            chunk.setBlockState(leaf, air, false);
        }

        for (BlockPos pos : hanging) {
            chunk.setBlockState(pos, air, false);
        }

        Ashlands.LOGGER.trace("Ashlands dead forest: stripped {} leaves in chunk ({}, {})",
                leaves.size(), chunkPos.x, chunkPos.z);
    }

    private static boolean hangsOnLeaves(BlockState state) {
        Block block = state.getBlock();
        return block instanceof SnowLayerBlock
                || block instanceof VineBlock
                || block instanceof MultifaceBlock
                || block instanceof HangingRootsBlock;
    }
}
