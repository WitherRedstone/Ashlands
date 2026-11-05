package com.chinaex123.ashlands.block;

import com.chinaex123.ashlands.event.AshlandsSyncHandler;
import com.chinaex123.ashlands.util.AshlandsWorld;
import com.chinaex123.ashlands.util.PurifiedChunks;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class PurifierBlock extends Block {

    public static final MapCodec<PurifierBlock> CODEC = simpleCodec(PurifierBlock::new);

    public PurifierBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level instanceof ServerLevel serverLevel) || !AshlandsWorld.isAshlandsWorld(serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        ChunkPos chunk = new ChunkPos(pos);
        if (AshlandsWorld.isChunkSafe(serverLevel, chunk.x, chunk.z)) {
            player.displayClientMessage(
                    Component.translatable("block.ashlands.purifier.already_safe").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.SUCCESS;
        }

        if (!PurifiedChunks.get(serverLevel).add(chunk.x, chunk.z)) {
            return InteractionResult.SUCCESS;
        }

        spawnPurifyParticles(serverLevel, pos);
        serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.6F, 0.75F);
        serverLevel.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.1F, 1.4F);
        AshlandsSyncHandler.syncAll(serverLevel);

        player.displayClientMessage(
                Component.translatable("block.ashlands.purifier.purified", chunk.x, chunk.z)
                        .withStyle(ChatFormatting.GREEN), true);

        serverLevel.destroyBlock(pos, false, player);
        return InteractionResult.SUCCESS;
    }

    private static void spawnPurifyParticles(ServerLevel level, BlockPos pos) {
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 1.0D;
        double centerZ = pos.getZ() + 0.5D;

        for (int ring = 0; ring < 3; ring++) {
            double radius = 0.45D + ring * 0.55D;
            int points = 10 + ring * 8;
            double height = centerY + ring * 0.5D;
            double phase = ring * Math.PI / 8.0D;
            for (int i = 0; i < points; i++) {
                double angle = Math.PI * 2.0D * i / points + phase;
                level.sendParticles(ParticleTypes.END_ROD,
                        centerX + Math.cos(angle) * radius, height, centerZ + Math.sin(angle) * radius,
                        1, 0.0D, 0.02D, 0.0D, 0.0D);
            }
        }

        for (int i = 0; i < 26; i++) {
            double height = centerY + i * 0.32D;
            double spread = 0.3D * (1.0D - (double) i / 26.0D);
            level.sendParticles(ParticleTypes.END_ROD,
                    centerX + (level.random.nextDouble() - 0.5D) * spread,
                    height,
                    centerZ + (level.random.nextDouble() - 0.5D) * spread,
                    1, 0.0D, 0.06D, 0.0D, 0.0D);
        }

        level.sendParticles(ParticleTypes.FIREWORK, centerX, centerY + 0.9D, centerZ, 24, 0.45D, 0.7D, 0.45D, 0.08D);
    }
}
