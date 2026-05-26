package com.example.newexnihilo.compat.jade;

import com.example.newexnihilo.BarrelBlock;
import com.example.newexnihilo.BarrelBlockEntity;
import com.example.newexnihilo.CrucibleBlock;
import com.example.newexnihilo.CrucibleBlockEntity;
import com.example.newexnihilo.ExampleMod;
import com.example.newexnihilo.ExNihiloMachineData;
import com.example.newexnihilo.ExNihiloFluidIds;
import com.example.newexnihilo.InfestedLeavesBlock;
import com.example.newexnihilo.InfestedLeavesBlockEntity;
import com.example.newexnihilo.InfestingLeavesBlock;
import com.example.newexnihilo.InfestingLeavesBlockEntity;
import com.example.newexnihilo.SieveBlock;
import com.example.newexnihilo.SieveBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

import java.util.List;

@WailaPlugin
public class NewExNihiloJadePlugin implements IWailaPlugin {
    private static final Identifier BARREL = id("barrel");
    private static final Identifier CRUCIBLE = id("crucible");
    private static final Identifier SIEVE = id("sieve");
    private static final Identifier LEAVES = id("leaves");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(dataOnly(BarrelProvider.INSTANCE), BarrelBlockEntity.class);
        registration.registerBlockDataProvider(dataOnly(CrucibleProvider.INSTANCE), CrucibleBlockEntity.class);
        registration.registerBlockDataProvider(dataOnly(SieveProvider.INSTANCE), SieveBlockEntity.class);
        registration.registerBlockDataProvider(dataOnly(InfestingLeavesProvider.INSTANCE), InfestingLeavesBlockEntity.class);
        registration.registerBlockDataProvider(dataOnly(InfestedLeavesProvider.INSTANCE), InfestedLeavesBlockEntity.class);
        registration.registerFluidStorage(MachineFluidViewProvider.INSTANCE, BarrelBlockEntity.class);
        registration.registerFluidStorage(MachineFluidViewProvider.INSTANCE, CrucibleBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addConfig(BARREL, true);
        registration.addConfig(CRUCIBLE, true);
        registration.addConfig(SIEVE, true);
        registration.addConfig(LEAVES, true);
        registration.registerBlockComponent(BarrelProvider.INSTANCE, BarrelBlock.class);
        registration.registerBlockComponent(CrucibleProvider.INSTANCE, CrucibleBlock.class);
        registration.registerBlockComponent(SieveProvider.INSTANCE, SieveBlock.class);
        registration.registerBlockComponent(InfestingLeavesProvider.INSTANCE, InfestingLeavesBlock.class);
        registration.registerBlockComponent(InfestedLeavesProvider.INSTANCE, InfestedLeavesBlock.class);
        registration.registerFluidStorageClient(MachineFluidViewProvider.INSTANCE);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ExampleMod.MODID, path);
    }

    private static IServerDataProvider<BlockAccessor> dataOnly(IServerDataProvider<BlockAccessor> delegate) {
        return new ServerDataOnlyProvider(delegate);
    }

    private static String itemId(ItemStack stack) {
        return stack.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static Component itemComponent(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return Component.empty();
        }
        Identifier id = Identifier.tryParse(itemId);
        if (id == null) {
            return Component.literal(itemId);
        }
        Item item = BuiltInRegistries.ITEM.getValue(id);
        Identifier resolved = BuiltInRegistries.ITEM.getKey(item);
        if (!id.equals(resolved)) {
            return Component.literal(itemId);
        }
        return new ItemStack(item).getHoverName();
    }

    private static Component stackComponent(String itemId, int count) {
        MutableComponent component = itemComponent(itemId).copy();
        if (count > 1) {
            component.append(" x").append(String.valueOf(count));
        }
        return component;
    }

    private static Component fluidComponent(String fluidId) {
        if (fluidId == null || fluidId.isEmpty()) {
            return Component.empty();
        }
        Identifier id = Identifier.tryParse(fluidId);
        if (id == null) {
            return Component.literal(fluidId);
        }
        if (ExNihiloFluidIds.WATER.equals(fluidId) || ExNihiloFluidIds.LAVA.equals(fluidId)) {
            return Component.translatable("block." + id.getNamespace() + "." + id.getPath());
        }
        return Component.translatable("fluid." + id.getNamespace() + "." + id.getPath());
    }

    private static Component entityComponent(String entityId) {
        if (entityId == null || entityId.isEmpty()) {
            return Component.empty();
        }
        Identifier id = Identifier.tryParse(entityId);
        if (id == null) {
            return Component.literal(entityId);
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(id);
        Identifier resolved = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (!id.equals(resolved)) {
            return Component.literal(entityId);
        }
        return type.getDescription();
    }

    private static int percent(float ratio) {
        return Math.round(Math.max(0.0F, Math.min(1.0F, ratio)) * 100.0F);
    }

    private static String percentText(float ratio) {
        return percent(ratio) + "%";
    }

    private static Component timeComponent(int ticks) {
        if (ticks <= 0) {
            return Component.literal("0s");
        }
        if (ticks < 20) {
            return Component.translatable("jade.new_ex_nihilo.time.less_than_second");
        }
        int seconds = (int) Math.ceil(ticks / 20.0D);
        return Component.translatable("jade.new_ex_nihilo.time.seconds", seconds);
    }

    private static void putOutput(CompoundTag tag, ItemStack stack) {
        if (!stack.isEmpty()) {
            tag.putString("output_id", itemId(stack));
            tag.putInt("output_count", stack.getCount());
        }
    }

    private static void addProgress(ITooltip tooltip, String key, float ratio) {
        tooltip.add(Component.translatable(key, IThemeHelper.get().info(percentText(ratio))));
    }

    private static void addTimedProgress(ITooltip tooltip, String key, float ratio, int remainingTicks) {
        tooltip.add(Component.translatable(key,
                IThemeHelper.get().info(percentText(ratio)),
                IThemeHelper.get().info(timeComponent(remainingTicks))));
    }

    private static void addOutput(ITooltip tooltip, CompoundTag data) {
        String outputId = data.getStringOr("output_id", "");
        if (!outputId.isEmpty()) {
            int count = data.getIntOr("output_count", 1);
            tooltip.add(Component.translatable("jade.new_ex_nihilo.output",
                    IThemeHelper.get().info(stackComponent(outputId, count))));
        }
    }

    private abstract static class Provider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        private final Identifier uid;

        Provider(Identifier uid) {
            this.uid = uid;
        }

        @Override
        public Identifier getUid() {
            return uid;
        }

        @Override
        public boolean shouldRequestData(BlockAccessor accessor) {
            return accessor.getBlockEntity() != null;
        }
    }

    private static final class ServerDataOnlyProvider implements IServerDataProvider<BlockAccessor> {
        private final IServerDataProvider<BlockAccessor> delegate;

        private ServerDataOnlyProvider(IServerDataProvider<BlockAccessor> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Identifier getUid() {
            return delegate.getUid();
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            delegate.appendServerData(data, accessor);
        }

        @Override
        public boolean shouldRequestData(BlockAccessor accessor) {
            return delegate.shouldRequestData(accessor);
        }
    }

    private static final class BarrelProvider extends Provider {
        private static final BarrelProvider INSTANCE = new BarrelProvider();

        private BarrelProvider() {
            super(BARREL);
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof BarrelBlockEntity barrel)) {
                return;
            }
            data.putString("fluid", barrel.getFluidId());
            data.putInt("fluid_amount", barrel.getFluidAmount());
            data.putInt("capacity", BarrelBlockEntity.CAPACITY);
            data.putInt("compost", barrel.getCompostAmount());
            data.putFloat("compost_progress", barrel.getCompostProgressRatio());
            data.putInt("compost_remaining", barrel.getCompostRemainingTicks());
            data.putBoolean("compost_ready", barrel.isCompostReady());
            data.putString("transition", barrel.getTransitionTargetFluid());
            data.putFloat("transition_progress", barrel.getTransitionProgressRatio());
            data.putInt("transition_remaining", barrel.getTransitionRemainingTicks());
            data.putString("doll_entity", barrel.getDollEntityId());
            data.putFloat("doll_progress", barrel.getDollProgressRatio());
            data.putInt("doll_remaining", barrel.getDollRemainingTicks());
            putOutput(data, barrel.getOutput());
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            String fluidId = data.getStringOr("fluid", "");
            int fluidAmount = data.getIntOr("fluid_amount", 0);
            if (!fluidId.isEmpty() && fluidAmount > 0) {
                int capacity = data.getIntOr("capacity", BarrelBlockEntity.CAPACITY);
                tooltip.add(Component.translatable("jade.new_ex_nihilo.fluid",
                        IThemeHelper.get().info(fluidComponent(fluidId)), IThemeHelper.get().info(fluidAmount),
                        capacity, IThemeHelper.get().info(percentText(fluidAmount / (float) capacity))));
            }
            int compost = data.getIntOr("compost", 0);
            if (compost > 0) {
                tooltip.add(Component.translatable("jade.new_ex_nihilo.compost_fill",
                        IThemeHelper.get().info(compost), BarrelBlockEntity.COMPOST_COMPLETE));
                if (data.getBooleanOr("compost_ready", false)) {
                    tooltip.add(Component.translatable("jade.new_ex_nihilo.collectable",
                            IThemeHelper.get().success(itemComponent(BuiltInRegistries.ITEM.getKey(Items.DIRT).toString()))));
                } else if (compost >= BarrelBlockEntity.COMPOST_COMPLETE) {
                    addTimedProgress(tooltip, "jade.new_ex_nihilo.fermenting_dirt",
                            data.getFloatOr("compost_progress", 0.0F),
                            data.getIntOr("compost_remaining", 0));
                }
            }
            String transition = data.getStringOr("transition", "");
            if (!transition.isEmpty()) {
                tooltip.add(Component.translatable("jade.new_ex_nihilo.transition", IThemeHelper.get().info(fluidComponent(transition))));
                addTimedProgress(tooltip, "jade.new_ex_nihilo.progress_time",
                        data.getFloatOr("transition_progress", 0.0F),
                        data.getIntOr("transition_remaining", 0));
            }
            String dollEntity = data.getStringOr("doll_entity", "");
            if (!dollEntity.isEmpty()) {
                tooltip.add(Component.translatable("jade.new_ex_nihilo.summoning",
                        IThemeHelper.get().info(entityComponent(dollEntity))));
                addTimedProgress(tooltip, "jade.new_ex_nihilo.progress_time",
                        data.getFloatOr("doll_progress", 0.0F),
                        data.getIntOr("doll_remaining", 0));
            }
            addOutput(tooltip, data);
        }
    }

    private static final class CrucibleProvider extends Provider {
        private static final CrucibleProvider INSTANCE = new CrucibleProvider();

        private CrucibleProvider() {
            super(CRUCIBLE);
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof CrucibleBlockEntity crucible)) {
                return;
            }
            data.putString("stored_fluid", crucible.getStoredFluidId());
            data.putInt("stored_amount", crucible.getStoredAmount());
            data.putString("target_fluid", crucible.getTargetFluidId());
            data.putInt("target_amount", crucible.getTargetAmount());
            data.putInt("reserved_amount", crucible.getReservedFluidAmount());
            data.putString("input_id", itemId(crucible.getInput()));
            data.putInt("input_count", crucible.getInputCount());
            data.putFloat("progress", crucible.getProgressRatio());
            data.putString("type", crucible.getCrucibleType());
            data.putDouble("heat", accessor.getLevel() instanceof ServerLevel serverLevel
                    ? ExNihiloMachineData.getHeat(serverLevel, serverLevel.getBlockState(accessor.getPosition().below()))
                    : 0.0D);
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            String storedFluid = data.getStringOr("stored_fluid", "");
            int storedAmount = data.getIntOr("stored_amount", 0);
            if (!storedFluid.isEmpty() && storedAmount > 0) {
                tooltip.add(Component.translatable("jade.new_ex_nihilo.fluid",
                        IThemeHelper.get().info(fluidComponent(storedFluid)), IThemeHelper.get().info(storedAmount),
                        CrucibleBlockEntity.CAPACITY,
                        IThemeHelper.get().info(percentText(storedAmount / (float) CrucibleBlockEntity.CAPACITY))));
            }
            String inputId = data.getStringOr("input_id", "");
            if (!inputId.isEmpty()) {
                int count = data.getIntOr("input_count", 1);
                tooltip.add(Component.translatable("jade.new_ex_nihilo.input",
                        IThemeHelper.get().info(stackComponent(inputId, count))));
                String targetFluid = data.getStringOr("target_fluid", "");
                if (!targetFluid.isEmpty()) {
                    tooltip.add(Component.translatable("jade.new_ex_nihilo.fluid_per_item",
                            IThemeHelper.get().info(fluidComponent(targetFluid)),
                            IThemeHelper.get().info(data.getIntOr("target_amount", 0))));
                    tooltip.add(Component.translatable("jade.new_ex_nihilo.reserved_fluid",
                            IThemeHelper.get().info(fluidComponent(targetFluid)), IThemeHelper.get().info(data.getIntOr("reserved_amount", 0))));
                }
                double heat = data.getDoubleOr("heat", 0.0D);
                int remaining = 0;
                if (heat > 0.0D || "wood".equals(data.getStringOr("type", ""))) {
                    int missing = Math.max(0, Math.round((1.0F - data.getFloatOr("progress", 0.0F)) * 100.0F));
                    remaining = (int) Math.ceil(missing / Math.max(1.0D, heat));
                }
                addTimedProgress(tooltip, "jade.new_ex_nihilo.melting_time", data.getFloatOr("progress", 0.0F), remaining);
            }
            double heat = data.getDoubleOr("heat", 0.0D);
            if ("wood".equals(data.getStringOr("type", "")) || heat > 0.0D) {
                tooltip.add(Component.translatable("jade.new_ex_nihilo.heat", IThemeHelper.get().info(heat)));
            } else {
                tooltip.add(Component.translatable("jade.new_ex_nihilo.no_heat"));
            }
        }
    }

    private static final class SieveProvider extends Provider {
        private static final SieveProvider INSTANCE = new SieveProvider();

        private SieveProvider() {
            super(SIEVE);
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof SieveBlockEntity sieve)) {
                return;
            }
            data.putString("mesh_id", itemId(sieve.getMesh()));
            data.putString("input_id", itemId(sieve.getInput()));
            data.putFloat("progress", sieve.getProgressRatio());
            putOutput(data, sieve.getOutput());
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            String meshId = data.getStringOr("mesh_id", "");
            if (!meshId.isEmpty()) {
                tooltip.add(Component.translatable("jade.new_ex_nihilo.mesh", IThemeHelper.get().info(itemComponent(meshId))));
            }
            String inputId = data.getStringOr("input_id", "");
            if (!inputId.isEmpty()) {
                tooltip.add(Component.translatable("jade.new_ex_nihilo.input", IThemeHelper.get().info(itemComponent(inputId))));
                addProgress(tooltip, "jade.new_ex_nihilo.sifting", data.getFloatOr("progress", 0.0F));
            }
            addOutput(tooltip, data);
        }
    }

    private static final class InfestingLeavesProvider extends Provider {
        private static final InfestingLeavesProvider INSTANCE = new InfestingLeavesProvider();

        private InfestingLeavesProvider() {
            super(LEAVES);
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof InfestingLeavesBlockEntity leaves)) {
                return;
            }
            data.putFloat("progress", leaves.getProgressRatio());
            data.putInt("remaining", leaves.getRemainingTicks());
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            addTimedProgress(tooltip, "jade.new_ex_nihilo.infesting_time",
                    accessor.getServerData().getFloatOr("progress", 0.0F),
                    accessor.getServerData().getIntOr("remaining", 0));
        }
    }

    private static final class InfestedLeavesProvider extends Provider {
        private static final InfestedLeavesProvider INSTANCE = new InfestedLeavesProvider();

        private InfestedLeavesProvider() {
            super(LEAVES);
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            tooltip.add(Component.translatable("jade.new_ex_nihilo.infested", IThemeHelper.get().success("100%")));
        }
    }

    private static final class MachineFluidViewProvider
            implements IServerExtensionProvider<FluidView.Data>, IClientExtensionProvider<FluidView.Data, FluidView> {
        private static final MachineFluidViewProvider INSTANCE = new MachineFluidViewProvider();

        private MachineFluidViewProvider() {
        }

        @Override
        public Identifier getUid() {
            return JadeIds.UNIVERSAL_FLUID_STORAGE_DEFAULT;
        }

        @Override
        public List<ViewGroup<FluidView.Data>> getGroups(snownee.jade.api.Accessor<?> accessor) {
            String fluidId = "";
            int amount = 0;
            int capacity = 0;
            Object target = accessor.getTarget();
            if (target instanceof BarrelBlockEntity barrel) {
                fluidId = barrel.getFluidId();
                amount = barrel.getFluidAmount();
                capacity = BarrelBlockEntity.CAPACITY;
            } else if (target instanceof CrucibleBlockEntity crucible) {
                fluidId = crucible.getStoredFluidId();
                amount = crucible.getStoredAmount();
                capacity = CrucibleBlockEntity.CAPACITY;
            }
            if (fluidId.isEmpty() || amount <= 0 || capacity <= 0) {
                return List.of();
            }
            FluidStack stack = new FluidStack(ExNihiloFluidIds.fluidFor(fluidId), amount);
            return List.of(new ViewGroup<>(List.of(new FluidView.Data(
                    JadeFluidObject.of(stack.getFluid(), stack.getAmount(), stack.getComponentsPatch()),
                    capacity))));
        }

        @Override
        public boolean shouldRequestData(snownee.jade.api.Accessor<?> accessor) {
            Object target = accessor.getTarget();
            if (target instanceof BarrelBlockEntity barrel) {
                return barrel.hasFluid();
            }
            if (target instanceof CrucibleBlockEntity crucible) {
                return crucible.getStoredAmount() > 0 && !crucible.getStoredFluidId().isEmpty();
            }
            return false;
        }

        @Override
        public List<ClientViewGroup<FluidView>> getClientGroups(snownee.jade.api.Accessor<?> accessor, List<ViewGroup<FluidView.Data>> groups) {
            return ClientViewGroup.map(groups, FluidView::readDefault, null);
        }

        @Override
        public int getDefaultPriority() {
            return 10000;
        }
    }
}
