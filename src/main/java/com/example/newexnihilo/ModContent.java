package com.example.newexnihilo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModContent {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ExampleMod.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ExampleMod.MODID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, ExampleMod.MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ExampleMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ExampleMod.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExampleMod.MODID);

    private static final List<DeferredBlock<? extends Block>> REGISTERED_BLOCKS = new ArrayList<>();
    private static final List<DeferredItem<BlockItem>> REGISTERED_BLOCK_ITEMS = new ArrayList<>();
    private static final List<DeferredItem<? extends Item>> REGISTERED_ITEMS = new ArrayList<>();

    public static final List<DeferredBlock<? extends Block>> ALL_BLOCKS = Collections.unmodifiableList(REGISTERED_BLOCKS);
    public static final List<DeferredItem<BlockItem>> ALL_BLOCK_ITEMS = Collections.unmodifiableList(REGISTERED_BLOCK_ITEMS);
    public static final List<DeferredItem<? extends Item>> ALL_ITEMS = Collections.unmodifiableList(REGISTERED_ITEMS);
    private static final List<DeferredItem<? extends Item>> TOOL_ITEMS = new ArrayList<>();
    private static final List<DeferredItem<? extends Item>> RESOURCE_ITEMS = new ArrayList<>();
    private static final List<DeferredItem<? extends Item>> MATERIAL_ITEMS = new ArrayList<>();
    private static final List<DeferredItem<? extends Item>> MISC_ITEMS = new ArrayList<>();
    private static final List<DeferredBlock<? extends Block>> SIEVE_BLOCKS = new ArrayList<>();
    private static final List<DeferredBlock<? extends Block>> BARREL_BLOCKS = new ArrayList<>();
    private static final List<DeferredBlock<? extends Block>> CRUCIBLE_BLOCKS = new ArrayList<>();
    public static DeferredBlock<? extends Block> INFESTING_LEAVES_BLOCK;
    public static DeferredBlock<? extends Block> INFESTED_LEAVES_BLOCK;
    public static DeferredBlock<? extends LiquidBlock> SEA_WATER_BLOCK;
    public static DeferredBlock<? extends LiquidBlock> WITCH_WATER_BLOCK;
    public static DeferredItem<? extends Item> SEA_WATER_BUCKET;
    public static DeferredItem<? extends Item> WITCH_WATER_BUCKET;

    public static final DeferredHolder<FluidType, FluidType> SEA_WATER_TYPE =
            FLUID_TYPES.register("sea_water", () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid." + ExampleMod.MODID + ".sea_water")
                    .canSwim(true)
                    .canDrown(true)
                    .canExtinguish(true)
                    .canHydrate(true)
                    .density(1000)
                    .viscosity(1000)
                    .temperature(300)));
    public static final DeferredHolder<FluidType, FluidType> WITCH_WATER_TYPE =
            FLUID_TYPES.register("witch_water", () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid." + ExampleMod.MODID + ".witch_water")
                    .canSwim(true)
                    .canDrown(true)
                    .density(1100)
                    .viscosity(1200)
                    .temperature(300)));
    public static final DeferredHolder<Fluid, FlowingFluid> SEA_WATER =
            FLUIDS.register("sea_water", () -> new ExNihiloFlowingFluid.Source(seaWaterProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_SEA_WATER =
            FLUIDS.register("flowing_sea_water", () -> new ExNihiloFlowingFluid.Flowing(seaWaterProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> WITCH_WATER =
            FLUIDS.register("witch_water", () -> new ExNihiloFlowingFluid.Source(witchWaterProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_WITCH_WATER =
            FLUIDS.register("flowing_witch_water", () -> new ExNihiloFlowingFluid.Flowing(witchWaterProperties()));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ALL_TAB =
            CREATIVE_TABS.register("all", ModContent::createAllTab);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKS_TAB =
            CREATIVE_TABS.register("blocks", () -> createBlockTab("itemGroup." + ExampleMod.MODID + ".blocks"));
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TOOLS_TAB =
            CREATIVE_TABS.register("tools", () -> createItemTab("itemGroup." + ExampleMod.MODID + ".tools", TOOL_ITEMS));
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RESOURCES_TAB =
            CREATIVE_TABS.register("resources", () -> createItemTab("itemGroup." + ExampleMod.MODID + ".resources", RESOURCE_ITEMS));
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MATERIALS_TAB =
            CREATIVE_TABS.register("materials", () -> createItemTab("itemGroup." + ExampleMod.MODID + ".materials", MATERIAL_ITEMS));
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MISC_TAB =
            CREATIVE_TABS.register("misc", () -> createItemTab("itemGroup." + ExampleMod.MODID + ".misc", MISC_ITEMS));

    private static final Entry[] BLOCK_ENTRIES = {
      new Entry("acacia_barrel", "Acacia Barrel", Kind.WOODEN_MACHINE),
      new Entry("acacia_crucible", "Acacia Crucible", Kind.WOODEN_MACHINE),
      new Entry("acacia_sieve", "Acacia Sieve", Kind.WOODEN_MACHINE),
      new Entry("bamboo_barrel", "Bamboo Barrel", Kind.WOODEN_MACHINE),
      new Entry("bamboo_crucible", "Bamboo Crucible", Kind.WOODEN_MACHINE),
      new Entry("bamboo_sieve", "Bamboo Sieve", Kind.WOODEN_MACHINE),
      new Entry("birch_barrel", "Birch Barrel", Kind.WOODEN_MACHINE),
      new Entry("birch_crucible", "Birch Crucible", Kind.WOODEN_MACHINE),
      new Entry("birch_sieve", "Birch Sieve", Kind.WOODEN_MACHINE),
      new Entry("cherry_barrel", "Cherry Barrel", Kind.WOODEN_MACHINE),
      new Entry("cherry_crucible", "Cherry Crucible", Kind.WOODEN_MACHINE),
      new Entry("cherry_sieve", "Cherry Sieve", Kind.WOODEN_MACHINE),
      new Entry("crimson_barrel", "Crimson Barrel", Kind.WOODEN_MACHINE),
      new Entry("crimson_crucible", "Crimson Crucible", Kind.WOODEN_MACHINE),
      new Entry("crimson_sieve", "Crimson Sieve", Kind.WOODEN_MACHINE),
      new Entry("crushed_andesite", "Crushed Andesite", Kind.CRUSHED),
      new Entry("crushed_basalt", "Crushed Basalt", Kind.CRUSHED),
      new Entry("crushed_blackstone", "Crushed Blackstone", Kind.CRUSHED),
      new Entry("crushed_calcite", "Crushed Calcite", Kind.CRUSHED),
      new Entry("crushed_deepslate", "Crushed Deepslate", Kind.CRUSHED),
      new Entry("crushed_diorite", "Crushed Diorite", Kind.CRUSHED),
      new Entry("crushed_dripstone", "Crushed Dripstone", Kind.CRUSHED),
      new Entry("crushed_end_stone", "Crushed End Stone", Kind.CRUSHED),
      new Entry("crushed_granite", "Crushed Granite", Kind.CRUSHED),
      new Entry("crushed_netherrack", "Crushed Netherrack", Kind.CRUSHED),
      new Entry("crushed_tuff", "Crushed Tuff", Kind.CRUSHED),
      new Entry("dark_oak_barrel", "Dark Oak Barrel", Kind.WOODEN_MACHINE),
      new Entry("dark_oak_crucible", "Dark Oak Crucible", Kind.WOODEN_MACHINE),
      new Entry("dark_oak_sieve", "Dark Oak Sieve", Kind.WOODEN_MACHINE),
      new Entry("dust", "Dust", Kind.CRUSHED),
      new Entry("end_cake", "End Cake", Kind.BASIC),
      new Entry("fired_crucible", "Fired Crucible", Kind.WOODEN_MACHINE),
      new Entry("infested_leaves", "Infested Leaves", Kind.LEAVES),
      new Entry("infesting_leaves", "Infesting Leaves", Kind.LEAVES),
      new Entry("jungle_barrel", "Jungle Barrel", Kind.WOODEN_MACHINE),
      new Entry("jungle_crucible", "Jungle Crucible", Kind.WOODEN_MACHINE),
      new Entry("jungle_sieve", "Jungle Sieve", Kind.WOODEN_MACHINE),
      new Entry("mangrove_barrel", "Mangrove Barrel", Kind.WOODEN_MACHINE),
      new Entry("mangrove_crucible", "Mangrove Crucible", Kind.WOODEN_MACHINE),
      new Entry("mangrove_sieve", "Mangrove Sieve", Kind.WOODEN_MACHINE),
      new Entry("oak_barrel", "Oak Barrel", Kind.WOODEN_MACHINE),
      new Entry("oak_crucible", "Oak Crucible", Kind.WOODEN_MACHINE),
      new Entry("oak_sieve", "Oak Sieve", Kind.WOODEN_MACHINE),
      new Entry("sea_water", "Sea Water", Kind.FLUID_PLACEHOLDER),
      new Entry("spruce_barrel", "Spruce Barrel", Kind.WOODEN_MACHINE),
      new Entry("spruce_crucible", "Spruce Crucible", Kind.WOODEN_MACHINE),
      new Entry("spruce_sieve", "Spruce Sieve", Kind.WOODEN_MACHINE),
      new Entry("stone_barrel", "Stone Barrel", Kind.WOODEN_MACHINE),
      new Entry("unfired_crucible", "Unfired Crucible", Kind.WOODEN_MACHINE),
      new Entry("warped_barrel", "Warped Barrel", Kind.WOODEN_MACHINE),
      new Entry("warped_crucible", "Warped Crucible", Kind.WOODEN_MACHINE),
      new Entry("warped_sieve", "Warped Sieve", Kind.WOODEN_MACHINE),
      new Entry("witch_water", "Witch Water", Kind.FLUID_PLACEHOLDER)
    };

    private static final Entry[] ITEM_ENTRIES = {
      new Entry("aluminum_ingot", "Aluminum Ingot", Kind.BASIC),
      new Entry("aluminum_nugget", "Aluminum Nugget", Kind.BASIC),
      new Entry("aluminum_pieces", "Aluminum Pieces", Kind.BASIC),
      new Entry("andesite_crook", "Andesite Crook", Kind.TOOL),
      new Entry("andesite_hammer", "Andesite Hammer", Kind.TOOL),
      new Entry("andesite_pebble", "Andesite Pebble", Kind.BASIC),
      new Entry("bamboo_crook", "Bamboo Crook", Kind.TOOL),
      new Entry("bamboo_hammer", "Bamboo Hammer", Kind.TOOL),
      new Entry("basalt_crook", "Basalt Crook", Kind.TOOL),
      new Entry("basalt_hammer", "Basalt Hammer", Kind.TOOL),
      new Entry("basalt_pebble", "Basalt Pebble", Kind.BASIC),
      new Entry("bee_doll", "Buzzing Doll", Kind.BASIC),
      new Entry("beehive_frame", "Beehive Frame", Kind.BASIC),
      new Entry("blackstone_crook", "Blackstone Crook", Kind.TOOL),
      new Entry("blackstone_hammer", "Blackstone Hammer", Kind.TOOL),
      new Entry("blackstone_pebble", "Blackstone Pebble", Kind.BASIC),
      new Entry("blaze_doll", "Blazing Doll", Kind.BASIC),
      new Entry("bone_crook", "Bone Crook", Kind.TOOL),
      new Entry("bone_hammer", "Bone Hammer", Kind.TOOL),
      new Entry("brain_coral_larva", "Brain Coral Larva", Kind.BASIC),
      new Entry("bubble_coral_larva", "Bubble Coral Larva", Kind.BASIC),
      new Entry("calcite_crook", "Calcite Crook", Kind.TOOL),
      new Entry("calcite_hammer", "Calcite Hammer", Kind.TOOL),
      new Entry("calcite_pebble", "Calcite Pebble", Kind.BASIC),
      new Entry("cherry_crook", "Cherry Crook", Kind.TOOL),
      new Entry("cherry_hammer", "Cherry Hammer", Kind.TOOL),
      new Entry("cooked_silkworm", "Cooked Silkworm", Kind.FOOD),
      new Entry("copper_crook", "Copper Crook", Kind.TOOL),
      new Entry("copper_hammer", "Copper Hammer", Kind.TOOL),
      new Entry("copper_nugget", "Copper Nugget", Kind.BASIC),
      new Entry("copper_pieces", "Copper Pieces", Kind.BASIC),
      new Entry("crimson_nylium_spores", "Crimson Nylium Spores", Kind.BASIC),
      new Entry("deepslate_crook", "Deepslate Crook", Kind.TOOL),
      new Entry("deepslate_hammer", "Deepslate Hammer", Kind.TOOL),
      new Entry("deepslate_pebble", "Deepslate Pebble", Kind.BASIC),
      new Entry("diamond_crook", "Diamond Crook", Kind.TOOL),
      new Entry("diamond_hammer", "Diamond Hammer", Kind.TOOL),
      new Entry("diamond_mesh", "Diamond Mesh", Kind.BASIC),
      new Entry("diorite_crook", "Diorite Crook", Kind.TOOL),
      new Entry("diorite_hammer", "Diorite Hammer", Kind.TOOL),
      new Entry("diorite_pebble", "Diorite Pebble", Kind.BASIC),
      new Entry("dripstone_crook", "Dripstone Crook", Kind.TOOL),
      new Entry("dripstone_hammer", "Dripstone Hammer", Kind.TOOL),
      new Entry("dripstone_pebble", "Dripstone Pebble", Kind.BASIC),
      new Entry("emerald_mesh", "Emerald Mesh", Kind.BASIC),
      new Entry("end_stone_pebble", "End Stone Pebble", Kind.BASIC),
      new Entry("enderman_doll", "Creeping Doll", Kind.BASIC),
      new Entry("fire_coral_larva", "Fire Coral Larva", Kind.BASIC),
      new Entry("flint_mesh", "Flint Mesh", Kind.BASIC),
      new Entry("gold_pieces", "Gold Pieces", Kind.BASIC),
      new Entry("golden_crook", "Golden Crook", Kind.TOOL),
      new Entry("golden_hammer", "Golden Hammer", Kind.TOOL),
      new Entry("granite_crook", "Granite Crook", Kind.TOOL),
      new Entry("granite_hammer", "Granite Hammer", Kind.TOOL),
      new Entry("granite_pebble", "Granite Pebble", Kind.BASIC),
      new Entry("grass_seeds", "Grass Seeds", Kind.BASIC),
      new Entry("guardian_doll", "Protecting Doll", Kind.BASIC),
      new Entry("horn_coral_larva", "Horn Coral Larva", Kind.BASIC),
      new Entry("iron_crook", "Iron Crook", Kind.TOOL),
      new Entry("iron_hammer", "Iron Hammer", Kind.TOOL),
      new Entry("iron_mesh", "Iron Mesh", Kind.BASIC),
      new Entry("iron_pieces", "Iron Pieces", Kind.BASIC),
      new Entry("lead_ingot", "Lead Ingot", Kind.BASIC),
      new Entry("lead_nugget", "Lead Nugget", Kind.BASIC),
      new Entry("lead_pieces", "Lead Pieces", Kind.BASIC),
      new Entry("mycelium_spores", "Mycelium Spores", Kind.BASIC),
      new Entry("nether_brick_crook", "Nether Brick Crook", Kind.TOOL),
      new Entry("nether_brick_hammer", "Nether Brick Hammer", Kind.TOOL),
      new Entry("netherite_crook", "Netherite Crook", Kind.TOOL),
      new Entry("netherite_hammer", "Netherite Hammer", Kind.TOOL),
      new Entry("netherite_mesh", "Netherite Mesh", Kind.BASIC),
      new Entry("netherrack_pebble", "Netherrack Pebble", Kind.BASIC),
      new Entry("nickel_ingot", "Nickel Ingot", Kind.BASIC),
      new Entry("nickel_nugget", "Nickel Nugget", Kind.BASIC),
      new Entry("nickel_pieces", "Nickel Pieces", Kind.BASIC),
      new Entry("platinum_ingot", "Platinum Ingot", Kind.BASIC),
      new Entry("platinum_nugget", "Platinum Nugget", Kind.BASIC),
      new Entry("platinum_pieces", "Platinum Pieces", Kind.BASIC),
      new Entry("porcelain_clay", "Porcelain Clay", Kind.BASIC),
      new Entry("porcelain_doll", "Porcelain Doll", Kind.BASIC),
      new Entry("raw_aluminum", "Raw Aluminum", Kind.BASIC),
      new Entry("raw_lead", "Raw Lead", Kind.BASIC),
      new Entry("raw_nickel", "Raw Nickel", Kind.BASIC),
      new Entry("raw_platinum", "Raw Platinum", Kind.BASIC),
      new Entry("raw_silver", "Raw Silver", Kind.BASIC),
      new Entry("raw_tin", "Raw Tin", Kind.BASIC),
      new Entry("raw_uranium", "Raw Uranium", Kind.BASIC),
      new Entry("raw_zinc", "Raw Zinc", Kind.BASIC),
      new Entry("red_nether_brick_crook", "Red Nether Brick Crook", Kind.TOOL),
      new Entry("red_nether_brick_hammer", "Red Nether Brick Hammer", Kind.TOOL),
      new Entry("sea_water_bucket", "Sea Water Bucket", Kind.BUCKET),
      new Entry("shulker_doll", "Floating Doll", Kind.BASIC),
      new Entry("silkworm", "Silkworm", Kind.FOOD),
      new Entry("silver_ingot", "Silver Ingot", Kind.BASIC),
      new Entry("silver_nugget", "Silver Nugget", Kind.BASIC),
      new Entry("silver_pieces", "Silver Pieces", Kind.BASIC),
      new Entry("stone_crook", "Stone Crook", Kind.TOOL),
      new Entry("stone_hammer", "Stone Hammer", Kind.TOOL),
      new Entry("stone_pebble", "Stone Pebble", Kind.BASIC),
      new Entry("string_mesh", "String Mesh", Kind.BASIC),
      new Entry("terracotta_crook", "Terracotta Crook", Kind.TOOL),
      new Entry("terracotta_hammer", "Terracotta Hammer", Kind.TOOL),
      new Entry("tin_ingot", "Tin Ingot", Kind.BASIC),
      new Entry("tin_nugget", "Tin Nugget", Kind.BASIC),
      new Entry("tin_pieces", "Tin Pieces", Kind.BASIC),
      new Entry("tube_coral_larva", "Tube Coral Larva", Kind.BASIC),
      new Entry("tuff_crook", "Tuff Crook", Kind.TOOL),
      new Entry("tuff_hammer", "Tuff Hammer", Kind.TOOL),
      new Entry("tuff_pebble", "Tuff Pebble", Kind.BASIC),
      new Entry("uranium_ingot", "Uranium Ingot", Kind.BASIC),
      new Entry("uranium_nugget", "Uranium Nugget", Kind.BASIC),
      new Entry("uranium_pieces", "Uranium Pieces", Kind.BASIC),
      new Entry("warped_nylium_spores", "Warped Nylium Spores", Kind.BASIC),
      new Entry("witch_water_bucket", "Witch Water Bucket", Kind.BUCKET),
      new Entry("wooden_crook", "Wooden Crook", Kind.TOOL),
      new Entry("wooden_hammer", "Wooden Hammer", Kind.TOOL),
      new Entry("zinc_ingot", "Zinc Ingot", Kind.BASIC),
      new Entry("zinc_nugget", "Zinc Nugget", Kind.BASIC),
      new Entry("zinc_pieces", "Zinc Pieces", Kind.BASIC)
    };

    static {
        for (Entry entry : BLOCK_ENTRIES) {
            registerBlock(entry);
        }
        for (Entry entry : ITEM_ENTRIES) {
            registerItem(entry);
        }
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SieveBlockEntity>> SIEVE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "sieve",
                    () -> new BlockEntityType<>(
                            SieveBlockEntity::new,
                            SIEVE_BLOCKS.stream().map(DeferredBlock::get).toArray(Block[]::new)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BarrelBlockEntity>> BARREL_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "barrel",
                    () -> new BlockEntityType<>(
                            BarrelBlockEntity::new,
                            BARREL_BLOCKS.stream().map(DeferredBlock::get).toArray(Block[]::new)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrucibleBlockEntity>> CRUCIBLE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "crucible",
                    () -> new BlockEntityType<>(
                            CrucibleBlockEntity::new,
                            CRUCIBLE_BLOCKS.stream().map(DeferredBlock::get).toArray(Block[]::new)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfestingLeavesBlockEntity>> INFESTING_LEAVES_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "infesting_leaves",
                    () -> new BlockEntityType<>(
                            InfestingLeavesBlockEntity::new,
                            INFESTING_LEAVES_BLOCK.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfestedLeavesBlockEntity>> INFESTED_LEAVES_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "infested_leaves",
                    () -> new BlockEntityType<>(
                            InfestedLeavesBlockEntity::new,
                            INFESTED_LEAVES_BLOCK.get()));

    private ModContent() {
    }

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
    }

    private static CreativeModeTab createAllTab() {
        return CreativeModeTab.builder()
                .title(Component.translatable("itemGroup." + ExampleMod.MODID))
                .icon(ModContent::rotatingTabIcon)
                .displayItems((parameters, output) -> {
                    for (DeferredItem<BlockItem> item : REGISTERED_BLOCK_ITEMS) {
                        output.accept(item.get());
                    }
                    for (DeferredItem<? extends Item> item : REGISTERED_ITEMS) {
                        output.accept(item.get());
                    }
                })
                .build();
    }

    private static CreativeModeTab createBlockTab(String titleKey) {
        return CreativeModeTab.builder()
                .title(Component.translatable(titleKey))
                .icon(() -> new ItemStack(REGISTERED_BLOCK_ITEMS.get(0).get()))
                .displayItems((parameters, output) -> {
                    for (DeferredItem<BlockItem> item : REGISTERED_BLOCK_ITEMS) {
                        output.accept(item.get());
                    }
                })
                .build();
    }

    private static ItemStack rotatingTabIcon() {
        List<ItemStack> icons = List.of(
                new ItemStack(REGISTERED_BLOCK_ITEMS.get(0).get()),
                new ItemStack(TOOL_ITEMS.get(0).get()),
                new ItemStack(RESOURCE_ITEMS.get(0).get()),
                new ItemStack(MATERIAL_ITEMS.get(0).get()),
                new ItemStack(MISC_ITEMS.get(0).get()));
        int index = (int) ((System.currentTimeMillis() / 1000L) % icons.size());
        return icons.get(index);
    }

    private static CreativeModeTab createItemTab(String titleKey, List<DeferredItem<? extends Item>> items) {
        DeferredItem<? extends Item> icon = items.isEmpty() ? REGISTERED_ITEMS.get(0) : items.get(0);
        return CreativeModeTab.builder()
                .title(Component.translatable(titleKey))
                .icon(() -> new ItemStack(icon.get()))
                .displayItems((parameters, output) -> {
                    for (DeferredItem<? extends Item> item : items) {
                        output.accept(item.get());
                    }
                })
                .build();
    }

    private static void registerBlock(Entry entry) {
        DeferredBlock<? extends Block> block;
        if (entry.id().equals("end_cake")) {
            block = BLOCKS.registerBlock(entry.id(), EndCakeBlock::new, () -> blockProperties(entry.kind()));
        } else if (entry.id().equals("infesting_leaves")) {
            block = BLOCKS.registerBlock(entry.id(), InfestingLeavesBlock::new, () -> blockProperties(entry.kind()));
            INFESTING_LEAVES_BLOCK = block;
        } else if (entry.id().equals("infested_leaves")) {
            block = BLOCKS.registerBlock(entry.id(), InfestedLeavesBlock::new, () -> blockProperties(entry.kind()));
            INFESTED_LEAVES_BLOCK = block;
        } else if (entry.id().equals("sea_water")) {
            block = BLOCKS.registerBlock(
                    entry.id(),
                    properties -> new LiquidBlock(SEA_WATER.get(), properties),
                    () -> blockProperties(entry.kind()));
            SEA_WATER_BLOCK = (DeferredBlock<? extends LiquidBlock>) block;
        } else if (entry.id().equals("witch_water")) {
            block = BLOCKS.registerBlock(
                    entry.id(),
                    properties -> new LiquidBlock(WITCH_WATER.get(), properties),
                    () -> blockProperties(entry.kind()));
            WITCH_WATER_BLOCK = (DeferredBlock<? extends LiquidBlock>) block;
        } else if (entry.id().endsWith("_barrel")) {
            block = BLOCKS.registerBlock(entry.id(), BarrelBlock::new, () -> blockProperties(entry.kind()));
            BARREL_BLOCKS.add(block);
        } else if (entry.id().endsWith("_crucible")) {
            block = BLOCKS.registerBlock(entry.id(), CrucibleBlock::new, () -> blockProperties(entry.kind()));
            CRUCIBLE_BLOCKS.add(block);
        } else if (entry.id().endsWith("_sieve")) {
            block = BLOCKS.registerBlock(entry.id(), SieveBlock::new, ModContent::sieveProperties);
            SIEVE_BLOCKS.add(block);
        } else {
            block = BLOCKS.registerSimpleBlock(entry.id(), () -> blockProperties(entry.kind()));
        }
        REGISTERED_BLOCKS.add(block);
        if (entry.kind() != Kind.FLUID_PLACEHOLDER) {
            REGISTERED_BLOCK_ITEMS.add(ITEMS.registerSimpleBlockItem(block));
        }
    }

    private static void registerItem(Entry entry) {
        DeferredItem<? extends Item> item = ITEMS.registerItem(entry.id(), properties -> createItem(entry, properties), () -> itemProperties(entry));
        if (entry.id().equals("sea_water_bucket")) {
            SEA_WATER_BUCKET = item;
        } else if (entry.id().equals("witch_water_bucket")) {
            WITCH_WATER_BUCKET = item;
        }
        REGISTERED_ITEMS.add(item);
        switch (category(entry)) {
            case TOOL -> TOOL_ITEMS.add(item);
            case RESOURCE -> RESOURCE_ITEMS.add(item);
            case MATERIAL -> MATERIAL_ITEMS.add(item);
            case MISC -> MISC_ITEMS.add(item);
        }
    }

    private static Item createItem(Entry entry, Item.Properties properties) {
        String id = entry.id();
        if (id.endsWith("_hammer")) {
            return new HammerItem(properties);
        }
        if (id.endsWith("_crook")) {
            return new CrookItem(properties);
        }
        if (id.endsWith("_mesh")) {
            return new MeshItem(MeshType.fromItemId(id), properties);
        }
        if (id.equals("silkworm")) {
            return new SilkwormItem(properties);
        }
        if (id.equals("grass_seeds")) {
            return new BlockTransformItem(properties, Map.of(
                    Blocks.DIRT, Blocks.GRASS_BLOCK,
                    Blocks.COARSE_DIRT, Blocks.GRASS_BLOCK,
                    Blocks.ROOTED_DIRT, Blocks.GRASS_BLOCK));
        }
        if (id.equals("mycelium_spores")) {
            return new BlockTransformItem(properties, Map.of(
                    Blocks.DIRT, Blocks.MYCELIUM,
                    Blocks.COARSE_DIRT, Blocks.MYCELIUM,
                    Blocks.ROOTED_DIRT, Blocks.MYCELIUM,
                    Blocks.GRASS_BLOCK, Blocks.MYCELIUM));
        }
        if (id.equals("crimson_nylium_spores")) {
            return new BlockTransformItem(properties, Map.of(
                    Blocks.NETHERRACK, Blocks.CRIMSON_NYLIUM));
        }
        if (id.equals("warped_nylium_spores")) {
            return new BlockTransformItem(properties, Map.of(
                    Blocks.NETHERRACK, Blocks.WARPED_NYLIUM));
        }
        if (id.equals("sea_water_bucket")) {
            return new BucketItem(SEA_WATER.get(), properties);
        }
        if (id.equals("witch_water_bucket")) {
            return new BucketItem(WITCH_WATER.get(), properties);
        }
        return new Item(properties);
    }

    private static BlockBehaviour.Properties blockProperties(Kind kind) {
        return switch (kind) {
            case CRUSHED -> BlockBehaviour.Properties.of().strength(0.7F).sound(SoundType.GRAVEL);
            case LEAVES -> BlockBehaviour.Properties.of().strength(0.2F).sound(SoundType.GRASS).noOcclusion();
            case WOODEN_MACHINE -> BlockBehaviour.Properties.of().strength(0.75F).sound(SoundType.WOOD).noOcclusion();
            case FLUID_PLACEHOLDER -> BlockBehaviour.Properties.of().replaceable().noOcclusion().strength(100.0F).noLootTable();
            default -> BlockBehaviour.Properties.of().strength(0.8F).sound(SoundType.STONE);
        };
    }

    private static BlockBehaviour.Properties sieveProperties() {
        return BlockBehaviour.Properties.of()
                .strength(0.75F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .isViewBlocking((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false);
    }

    private static Item.Properties itemProperties(Entry entry) {
        String id = entry.id();
        if (id.endsWith("_hammer")) {
            return toolProperties(id, ModTags.MINEABLE_WITH_HAMMER, -1.0F, -2.0F);
        }
        if (id.endsWith("_crook")) {
            return toolProperties(id, ModTags.MINEABLE_WITH_CROOK, -1.0F, -2.0F);
        }
        if (id.endsWith("_mesh")) {
            return meshProperties(MeshType.fromItemId(id));
        }
        return switch (entry.kind()) {
            case BUCKET -> new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET);
            case FOOD -> new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.6F).build());
            default -> new Item.Properties();
        };
    }

    private static Item.Properties toolProperties(String id, net.minecraft.tags.TagKey<Block> mineableTag, float baseDamage, float attackSpeed) {
        ToolMaterial material = toolMaterial(id);
        return new Item.Properties().tool(material, mineableTag, baseDamage, attackSpeed, 0.0F);
    }

    private static Item.Properties meshProperties(MeshType type) {
        int durability = switch (type) {
            case STRING -> 64;
            case FLINT -> 128;
            case IRON -> 256;
            case DIAMOND -> 512;
            case EMERALD -> 768;
            case NETHERITE -> 1024;
            default -> 64;
        };
        return new Item.Properties().durability(durability);
    }

    private static ToolMaterial toolMaterial(String id) {
        if (id.startsWith("wooden_") || id.startsWith("bamboo_") || id.startsWith("cherry_")) {
            return ToolMaterial.WOOD;
        }
        if (id.startsWith("copper_")) {
            return ToolMaterial.COPPER;
        }
        if (id.startsWith("iron_")) {
            return ToolMaterial.IRON;
        }
        if (id.startsWith("diamond_")) {
            return ToolMaterial.DIAMOND;
        }
        if (id.startsWith("golden_")) {
            return ToolMaterial.GOLD;
        }
        if (id.startsWith("netherite_")) {
            return ToolMaterial.NETHERITE;
        }
        return ToolMaterial.STONE;
    }

    private static TabCategory category(Entry entry) {
        String id = entry.id();
        if (entry.kind() == Kind.TOOL || id.endsWith("_crook") || id.endsWith("_hammer")) {
            return TabCategory.TOOL;
        }
        if (id.endsWith("_ingot") || id.endsWith("_nugget") || id.endsWith("_pieces")
                || id.startsWith("raw_") || id.endsWith("_pebble")) {
            return TabCategory.RESOURCE;
        }
        if (id.endsWith("_mesh") || id.endsWith("_spores") || id.endsWith("_larva")
                || id.contains("doll") || id.equals("porcelain_clay") || id.equals("beehive_frame")) {
            return TabCategory.MATERIAL;
        }
        return TabCategory.MISC;
    }

    private record Entry(String id, String displayName, Kind kind) {
    }

    private enum Kind {
        BASIC,
        BUCKET,
        CRUSHED,
        FLUID_PLACEHOLDER,
        FOOD,
        LEAVES,
        TOOL,
        WOODEN_MACHINE
    }

    private enum TabCategory {
        TOOL,
        RESOURCE,
        MATERIAL,
        MISC
    }

    private static BaseFlowingFluid.Properties seaWaterProperties() {
        return new BaseFlowingFluid.Properties(SEA_WATER_TYPE, SEA_WATER, FLOWING_SEA_WATER)
                .bucket(() -> SEA_WATER_BUCKET.get())
                .block(() -> SEA_WATER_BLOCK.get())
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5)
                .explosionResistance(100.0F);
    }

    private static BaseFlowingFluid.Properties witchWaterProperties() {
        return new BaseFlowingFluid.Properties(WITCH_WATER_TYPE, WITCH_WATER, FLOWING_WITCH_WATER)
                .bucket(() -> WITCH_WATER_BUCKET.get())
                .block(() -> WITCH_WATER_BLOCK.get())
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5)
                .explosionResistance(100.0F);
    }
}
