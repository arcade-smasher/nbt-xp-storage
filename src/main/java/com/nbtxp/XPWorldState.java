package com.nbtxp;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

public class XPWorldState extends PersistentState {

    private static final String DATA_NAME = "nbtxp_data";

    private static final int DEFAULT_XP_CONSUMED = 7;
    private static final boolean DEFAULT_CONSUMPTION = true;
    private int _XP_CONSUMED = DEFAULT_XP_CONSUMED;
    private boolean _ENABLE_CONSUMPTION = DEFAULT_CONSUMPTION;

    public XPWorldState() {}

    public static XPWorldState fromNbt(NbtCompound nbt) {
        XPWorldState state = new XPWorldState();
        state._XP_CONSUMED = nbt.getInt("XP_CONSUMED");
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("XP_CONSUMED", this._XP_CONSUMED);
        nbt.putBoolean("ENABLE_CONSUMPTION", this._ENABLE_CONSUMPTION);
        return nbt;
    }

    public int XP_CONSUMED() {
        return _XP_CONSUMED;
    }

    public void XP_CONSUMED(int value) {
        this._XP_CONSUMED = value;
        markDirty();
    }

    public boolean ENABLE_CONSUMPTION() {
        return _ENABLE_CONSUMPTION;
    }

    public void ENABLE_CONSUMPTION(boolean value) {
        this._ENABLE_CONSUMPTION = value;
        markDirty();
    }

    public static XPWorldState get(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        PersistentStateManager manager = overworld.getPersistentStateManager();
        return manager.getOrCreate(
            XPWorldState::fromNbt,
            XPWorldState::new,
            DATA_NAME
        );
    }
}