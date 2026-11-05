package com.chinaex123.ashlands.network;

import com.chinaex123.ashlands.Ashlands;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AshlandsSyncPayload(boolean active, int centerChunkX, int centerChunkZ, int radius)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AshlandsSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Ashlands.id("sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AshlandsSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBoolean(payload.active());
                buf.writeVarInt(payload.centerChunkX());
                buf.writeVarInt(payload.centerChunkZ());
                buf.writeVarInt(payload.radius());
            },
            buf -> new AshlandsSyncPayload(buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt())
    );

    @Override
    public CustomPacketPayload.Type<AshlandsSyncPayload> type() {
        return TYPE;
    }
}
