package com.example.newexnihilo;

import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum MeshType implements StringRepresentable {
    NONE(0),
    STRING(1),
    FLINT(2),
    IRON(3),
    DIAMOND(4),
    EMERALD(5),
    NETHERITE(6);

    public static final StringRepresentable.EnumCodec<MeshType> CODEC = StringRepresentable.fromEnum(MeshType::values);

    private final int level;

    MeshType(int level) {
        this.level = level;
    }

    public static MeshType fromItemId(String id) {
        if (!id.endsWith("_mesh")) {
            return NONE;
        }
        String name = id.substring(0, id.length() - "_mesh".length()).toUpperCase(Locale.ROOT);
        return fromName(name);
    }

    public static MeshType fromName(String name) {
        String normalized = name.toUpperCase(Locale.ROOT);
        try {
            return MeshType.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }

    public int getLevel() {
        return level;
    }

    public String getMeshName() {
        return getSerializedName() + "_mesh";
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
