package com.example.newexnihilo;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.Item;

public class MeshItem extends Item {
    private static final Map<MeshType, MeshItem> BY_TYPE = new EnumMap<>(MeshType.class);

    private final MeshType type;

    public MeshItem(MeshType type, Properties properties) {
        super(properties);
        this.type = type;
        BY_TYPE.put(type, this);
    }

    public static MeshItem getMesh(MeshType type) {
        return BY_TYPE.get(type);
    }

    public MeshType getType() {
        return type;
    }

    public int getLevel() {
        return type.getLevel();
    }
}
