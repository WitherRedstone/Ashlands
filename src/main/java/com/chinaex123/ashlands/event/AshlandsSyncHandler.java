package com.chinaex123.ashlands.event;

import com.chinaex123.ashlands.Ashlands;
import com.chinaex123.ashlands.network.AshlandsSyncPayload;
import com.chinaex123.ashlands.util.AshlandsWorld;
import com.chinaex123.ashlands.util.PurifiedChunks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Ashlands.MODID)
public final class AshlandsSyncHandler {

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, payload(player.serverLevel()));
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, payload(player.serverLevel()));
        }
    }

    /**
     * 把这个维度的安全区信息重新广播给所有玩家，净化器点亮区块后调用。
     */
    public static void syncAll(ServerLevel level) {
        PacketDistributor.sendToPlayersInDimension(level, payload(level));
    }

    private static AshlandsSyncPayload payload(ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();
        return new AshlandsSyncPayload(
                AshlandsWorld.isAshlandsWorld(level),
                spawn.getX() >> 4,
                spawn.getZ() >> 4,
                AshlandsWorld.safeRadiusChunks(),
                PurifiedChunks.get(level).snapshot());
    }
}
