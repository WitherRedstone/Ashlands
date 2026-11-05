package com.chinaex123.ashlands.init;

import com.chinaex123.ashlands.Ashlands;
import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Ashlands.MODID);

    public static final Supplier<AttachmentType<Long>> RADIATION_START =
            ATTACHMENT_TYPES.register("radiation_start", () -> AttachmentType.builder(() -> -1L)
                    .serialize(Codec.LONG)
                    .build());

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
