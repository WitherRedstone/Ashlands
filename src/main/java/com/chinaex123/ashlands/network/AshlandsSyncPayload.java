package com.chinaex123.ashlands.network;

import com.chinaex123.ashlands.Ashlands;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record AshlandsSyncPayload(boolean active, int centerChunkX, int centerChunkZ, int radius, List<Long> purified)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AshlandsSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Ashlands.id("sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AshlandsSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBoolean(payload.active());
                buf.writeVarInt(payload.centerChunkX());
                buf.writeVarInt(payload.centerChunkZ());
                buf.writeVarInt(payload.radius());
                buf.writeVarInt(payload.purified().size());
                for (long chunk : payload.purified()) {
                    buf.writeLong(chunk);
                }
            },
            buf -> {
                boolean active = buf.readBoolean();
                int centerChunkX = buf.readVarInt();
                int centerChunkZ = buf.readVarInt();
                int radius = buf.readVarInt();
                int size = buf.readVarInt();
                List<Long> purified = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    purified.add(buf.readLong());
                }
                return new AshlandsSyncPayload(active, centerChunkX, centerChunkZ, radius, List.copyOf(purified));
            }
    );

    @Override
    public CustomPacketPayload.Type<AshlandsSyncPayload> type() {
        return TYPE;
    }
}
