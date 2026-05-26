package com.example.newexnihilo;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static final TagKey<Block> MINEABLE_WITH_HAMMER = blockTag("mineable/hammer");
    public static final TagKey<Block> MINEABLE_WITH_CROOK = blockTag("mineable/crook");

    public static final TagKey<Item> HAMMERS = itemTag("tools/hammers");
    public static final TagKey<Item> CROOKS = itemTag("tools/crooks");
    public static final TagKey<Item> MESHES = itemTag("meshes");

    private ModTags() {
    }

    private static TagKey<Block> blockTag(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ExampleMod.MODID, path));
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ExampleMod.MODID, path));
    }
}
