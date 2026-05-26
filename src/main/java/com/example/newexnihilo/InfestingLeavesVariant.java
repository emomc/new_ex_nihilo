package com.example.newexnihilo;

import net.minecraft.util.StringRepresentable;

public enum InfestingLeavesVariant implements StringRepresentable {
    OAK("oak"),
    SPRUCE("spruce"),
    BIRCH("birch"),
    JUNGLE("jungle"),
    ACACIA("acacia"),
    DARK_OAK("dark_oak"),
    MANGROVE("mangrove"),
    AZALEA("azalea");

    private final String name;

    InfestingLeavesVariant(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
