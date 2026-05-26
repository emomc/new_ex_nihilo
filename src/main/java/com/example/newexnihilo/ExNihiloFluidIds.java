package com.example.newexnihilo;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class ExNihiloFluidIds {
    public static final String WATER = "minecraft:water";
    public static final String LAVA = "minecraft:lava";
    public static final String SEA_WATER = ExampleMod.MODID + ":sea_water";
    public static final String WITCH_WATER = ExampleMod.MODID + ":witch_water";

    private ExNihiloFluidIds() {
    }

    public static boolean isSupportedFluid(String id) {
        return WATER.equals(id) || LAVA.equals(id) || SEA_WATER.equals(id) || WITCH_WATER.equals(id);
    }

    public static String fluidFromBucket(Item item) {
        if (item == Items.WATER_BUCKET) {
            return WATER;
        }
        if (item == Items.LAVA_BUCKET) {
            return LAVA;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (id.equals(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "sea_water_bucket"))) {
            return SEA_WATER;
        }
        if (id.equals(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "witch_water_bucket"))) {
            return WITCH_WATER;
        }
        return "";
    }

    public static Item bucketFor(String fluidId) {
        if (WATER.equals(fluidId)) {
            return Items.WATER_BUCKET;
        }
        if (LAVA.equals(fluidId)) {
            return Items.LAVA_BUCKET;
        }
        if (SEA_WATER.equals(fluidId)) {
            return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "sea_water_bucket"));
        }
        if (WITCH_WATER.equals(fluidId)) {
            return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "witch_water_bucket"));
        }
        return Items.BUCKET;
    }

    public static Block blockFor(String fluidId) {
        if (WATER.equals(fluidId)) {
            return Blocks.WATER;
        }
        if (LAVA.equals(fluidId)) {
            return Blocks.LAVA;
        }
        if (SEA_WATER.equals(fluidId)) {
            return BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "sea_water"));
        }
        if (WITCH_WATER.equals(fluidId)) {
            return BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "witch_water"));
        }
        return Blocks.AIR;
    }

    public static Fluid fluidFor(String fluidId) {
        if (WATER.equals(fluidId)) {
            return Fluids.WATER;
        }
        if (LAVA.equals(fluidId)) {
            return Fluids.LAVA;
        }
        if (SEA_WATER.equals(fluidId)) {
            return ModContent.SEA_WATER.get();
        }
        if (WITCH_WATER.equals(fluidId)) {
            return ModContent.WITCH_WATER.get();
        }
        return Fluids.EMPTY;
    }

    public static String idForFluid(Fluid fluid) {
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
            return WATER;
        }
        if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
            return LAVA;
        }
        if (fluid == ModContent.SEA_WATER.get() || fluid == ModContent.FLOWING_SEA_WATER.get()) {
            return SEA_WATER;
        }
        if (fluid == ModContent.WITCH_WATER.get() || fluid == ModContent.FLOWING_WITCH_WATER.get()) {
            return WITCH_WATER;
        }
        return "";
    }

    public static String fluidFromBlock(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        if (block == Blocks.WATER) {
            return WATER;
        }
        if (block == Blocks.LAVA) {
            return LAVA;
        }
        if (id.equals(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "sea_water"))) {
            return SEA_WATER;
        }
        if (id.equals(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "witch_water"))) {
            return WITCH_WATER;
        }
        return "";
    }

    public static String remap(String id) {
        return id.replace("exnihilosequentia:", ExampleMod.MODID + ":");
    }
}
