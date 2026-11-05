package com.chinaex123.ashlands.event;

import com.chinaex123.ashlands.Ashlands;
import com.chinaex123.ashlands.config.AshlandsConfig;
import com.chinaex123.ashlands.network.AshlandsSyncPayload;
import com.chinaex123.ashlands.util.AshlandsWorld;
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
            sync(player);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    private static void sync(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        boolean active = AshlandsWorld.isAshlandsWorld(level);
        BlockPos spawn = level.getSharedSpawnPos();
        PacketDistributor.sendToPlayer(player, new AshlandsSyncPayload(
                active,
                spawn.getX() >> 4,
                spawn.getZ() >> 4,
                AshlandsConfig.SAFE_RADIUS_CHUNKS.get()));
    }
}
