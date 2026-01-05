package com.github.tartaricacid.netmusic.tileentity;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityMusicPersistentState extends PersistentState {
    public static final String KEY = "netmusic_entity_players";
    private final Map<UUID, NbtCompound> data = new HashMap<>();

    public EntityMusicPersistentState() {}

    public static EntityMusicPersistentState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        EntityMusicPersistentState s = new EntityMusicPersistentState();
        if (nbt == null) return s;
        NbtList list = nbt.getList("entities", 10);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound e = list.getCompound(i);
            try {
                UUID uuid = UUID.fromString(e.getString("uuid"));
                s.data.put(uuid, e);
            } catch (Exception ignored) {}
        }
        return s;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        NbtList list = new NbtList();
        for (Map.Entry<UUID, NbtCompound> e : data.entrySet()) {
            NbtCompound copy = e.getValue().copy();
            copy.putString("uuid", e.getKey().toString());
            list.add(copy);
        }
        nbt.put("entities", list);
        return nbt;
    }

    public void put(UUID uuid, NbtCompound compound) {
        data.put(uuid, compound);
        markDirty();
    }

    public NbtCompound get(UUID uuid) {
        return data.get(uuid);
    }

    public boolean contains(UUID uuid) {
        return data.containsKey(uuid);
    }

    public void remove(UUID uuid) {
        data.remove(uuid);
        markDirty();
    }
}
