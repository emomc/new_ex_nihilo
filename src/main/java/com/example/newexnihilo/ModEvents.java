package com.example.newexnihilo;

import java.lang.reflect.Proxy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;

public final class ModEvents {
    private ModEvents() {
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModEvents::registerDispenserFluids);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        BlockCapability<Object, Direction> itemHandler = (BlockCapability<Object, Direction>) (BlockCapability) TransferCapabilityAdapters.itemCapability();
        BlockCapability<Object, Direction> fluidHandler = (BlockCapability<Object, Direction>) (BlockCapability) TransferCapabilityAdapters.fluidCapability();
        event.registerBlockEntity(
                itemHandler,
                ModContent.BARREL_BLOCK_ENTITY.get(),
                (blockEntity, side) -> TransferCapabilityAdapters.itemHandler(blockEntity));
        event.registerBlockEntity(
                fluidHandler,
                ModContent.BARREL_BLOCK_ENTITY.get(),
                (blockEntity, side) -> TransferCapabilityAdapters.fluidHandler(blockEntity));
        event.registerBlockEntity(
                itemHandler,
                ModContent.CRUCIBLE_BLOCK_ENTITY.get(),
                (blockEntity, side) -> TransferCapabilityAdapters.itemHandler(blockEntity));
        event.registerBlockEntity(
                fluidHandler,
                ModContent.CRUCIBLE_BLOCK_ENTITY.get(),
                (blockEntity, side) -> TransferCapabilityAdapters.fluidHandler(blockEntity));
        event.registerBlockEntity(
                itemHandler,
                ModContent.SIEVE_BLOCK_ENTITY.get(),
                (blockEntity, side) -> TransferCapabilityAdapters.itemHandler(blockEntity));
    }

    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(fluidTextures("sea_water", "sea_water_flow"), ModContent.SEA_WATER_TYPE.get());
        event.registerFluidType(fluidTextures("witch_water", "witch_water_flow"), ModContent.WITCH_WATER_TYPE.get());
    }

    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        event.register(fluidModel("sea_water", "sea_water_flow"), ModContent.SEA_WATER, ModContent.FLOWING_SEA_WATER);
        event.register(fluidModel("witch_water", "witch_water_flow"), ModContent.WITCH_WATER, ModContent.FLOWING_WITCH_WATER);
    }

    private static FluidModel.Unbaked fluidModel(String still, String flowing) {
        Material stillMaterial = new Material(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "block/" + still));
        Material flowingMaterial = new Material(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "block/" + flowing));
        return new FluidModel.Unbaked(stillMaterial, flowingMaterial, stillMaterial, FluidTintSources.constant(0xFFFFFFFF));
    }

    private static IClientFluidTypeExtensions fluidTextures(String still, String flowing) {
        Identifier stillTexture = Identifier.fromNamespaceAndPath(ExampleMod.MODID, "block/" + still);
        Identifier flowingTexture = Identifier.fromNamespaceAndPath(ExampleMod.MODID, "block/" + flowing);
        return (IClientFluidTypeExtensions) Proxy.newProxyInstance(
                IClientFluidTypeExtensions.class.getClassLoader(),
                new Class<?>[] { IClientFluidTypeExtensions.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("getStillTexture")) {
                        return stillTexture;
                    }
                    if (method.getName().equals("getFlowingTexture")) {
                        return flowingTexture;
                    }
                    if (method.getName().equals("getTintColor")) {
                        return 0xFFFFFFFF;
                    }
                    if (method.isDefault()) {
                        return java.lang.reflect.InvocationHandler.invokeDefault(proxy, method, args);
                    }
                    if (method.getReturnType() == boolean.class) {
                        return false;
                    }
                    if (method.getReturnType() == int.class) {
                        return 0;
                    }
                    return null;
                });
    }

    private static void registerDispenserFluids() {
        DispenseItemBehavior behavior = new DefaultDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior fallback = new DefaultDispenseItemBehavior();

            @Override
            public ItemStack execute(BlockSource source, ItemStack stack) {
                BucketItem bucket = (BucketItem) stack.getItem();
                BlockPos target = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
                Level level = source.level();
                if (bucket.emptyContents(null, level, target, null, stack)) {
                    bucket.checkExtraContent(null, level, stack, target);
                    return new ItemStack(Items.BUCKET);
                }
                return fallback.dispense(source, stack);
            }
        };
        DispenserBlock.registerBehavior(ModContent.SEA_WATER_BUCKET.get(), behavior);
        DispenserBlock.registerBehavior(ModContent.WITCH_WATER_BUCKET.get(), behavior);
    }
}
