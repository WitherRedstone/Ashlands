package com.chinaex123.ashlands.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PurifiedChunks extends SavedData {

    private static final String DATA_NAME = "ashlands_purified_chunks";
    private static final String CHUNKS_KEY = "chunks";

    private static final SavedData.Factory<PurifiedChunks> FACTORY =
            new SavedData.Factory<>(PurifiedChunks::new, PurifiedChunks::load);

    private final Set<Long> chunks = new HashSet<>();

    public static PurifiedChunks get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public static PurifiedChunks load(CompoundTag tag, HolderLookup.Provider registries) {
        PurifiedChunks data = new PurifiedChunks();
        for (long packed : tag.getLongArray(CHUNKS_KEY)) {
            data.chunks.add(packed);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        long[] packed = new long[chunks.size()];
        int index = 0;
        for (long value : chunks) {
            packed[index++] = value;
        }
        tag.putLongArray(CHUNKS_KEY, packed);
        return tag;
    }

    public boolean contains(int chunkX, int chunkZ) {
        return chunks.contains(ChunkPos.asLong(chunkX, chunkZ));
    }

    public boolean add(int chunkX, int chunkZ) {
        if (chunks.add(ChunkPos.asLong(chunkX, chunkZ))) {
            setDirty();
            return true;
        }
        return false;
    }

    public List<Long> snapshot() {
        return List.copyOf(chunks);
    }
}
