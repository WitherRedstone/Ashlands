package com.chinaex123.ashlands.network;

import com.chinaex123.ashlands.util.AshlandsWorld;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class AshlandsNetwork {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                AshlandsSyncPayload.TYPE,
                AshlandsSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> AshlandsWorld.updateClientData(
                        payload.active(),
                        payload.centerChunkX(),
                        payload.centerChunkZ(),
                        payload.radius()))
        );
    }
}
