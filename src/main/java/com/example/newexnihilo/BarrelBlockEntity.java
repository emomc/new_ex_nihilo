package com.example.newexnihilo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BarrelBlockEntity extends BlockEntity {
    public static final int CAPACITY = 1000;
    public static final int COMPOST_COMPLETE = 1000;
    public static final int COMPOST_TICKS = 1200;
    public static final int TRANSITION_TICKS = 1200;
    public static final int DOLL_TICKS = 1200;

    private String fluidId = "";
    private int fluidAmount;
    private int compostAmount;
    private int compostProgress;
    private int transitionProgress;
    private String transitionTargetFluid = "";
    private int tickCounter;
    private String dollEntityId = "";
    private int dollProgress;
    private ItemStack output = ItemStack.EMPTY;

    public BarrelBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModContent.BARREL_BLOCK_ENTITY.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BarrelBlockEntity barrel) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        barrel.tickCounter++;
        if (!barrel.hasFluid()) {
            barrel.clearTransitionProgress();
        }
        if (!barrel.dollEntityId.isEmpty()) {
            barrel.tickDoll(serverLevel, pos);
            return;
        }
        if (barrel.hasCompleteCompost()) {
            barrel.tickCompost();
            return;
        }
        if (barrel.fluidAmount <= 0 || barrel.fluidId.isEmpty()) {
            return;
        }
        if (barrel.tickCounter % 20 == 0) {
            String topFluid = ExNihiloFluidIds.fluidFromBlock(level.getBlockState(pos.above()).getBlock());
            if (!topFluid.isEmpty()) {
                ItemStack result = ExNihiloMachineData.getSolidifyResult(serverLevel, barrel.fluidId, barrel.fluidAmount, topFluid);
                if (!result.isEmpty() && barrel.addOutput(result)) {
                    level.removeBlock(pos.above(), false);
                    barrel.clearFluid();
                    MachineInteraction.complete(level, pos);
                    return;
                }
            }
        }
        barrel.tickTransition(serverLevel);
    }

    public boolean hasFluid() {
        return fluidAmount > 0 && !fluidId.isEmpty();
    }

    public String getFluidId() {
        return fluidId;
    }

    public int getFluidAmount() {
        return fluidAmount;
    }

    public Fluid getFluid() {
        return ExNihiloFluidIds.fluidFor(fluidId);
    }

    public float getFluidProportion() {
        return (float) fluidAmount / CAPACITY;
    }

    public int getCompostAmount() {
        return compostAmount;
    }

    public float getCompostProportion() {
        return (float) compostAmount / COMPOST_COMPLETE;
    }

    public int getCompostProgress() {
        return compostProgress;
    }

    public float getCompostProgressRatio() {
        return Math.min(1.0F, compostProgress / (float) COMPOST_TICKS);
    }

    public int getTransitionProgress() {
        return transitionProgress;
    }

    public float getTransitionProgressRatio() {
        return Math.min(1.0F, transitionProgress / (float) TRANSITION_TICKS);
    }

    public String getTransitionTargetFluid() {
        return transitionTargetFluid;
    }

    public int getCompostRemainingTicks() {
        return isCompostReady() ? 0 : Math.max(0, COMPOST_TICKS - compostProgress);
    }

    public int getTransitionRemainingTicks() {
        return transitionTargetFluid.isEmpty() ? 0 : Math.max(0, TRANSITION_TICKS - transitionProgress);
    }

    public String getDollEntityId() {
        return dollEntityId;
    }

    public int getDollProgress() {
        return dollProgress;
    }

    public float getDollProgressRatio() {
        return Math.min(1.0F, dollProgress / (float) DOLL_TICKS);
    }

    public int getDollRemainingTicks() {
        return dollEntityId.isEmpty() ? 0 : Math.max(0, DOLL_TICKS - dollProgress);
    }

    public ItemStack getOutput() {
        return output;
    }

    BarrelSnapshot createTransferSnapshot() {
        return new BarrelSnapshot(
                fluidId,
                fluidAmount,
                compostAmount,
                compostProgress,
                transitionProgress,
                transitionTargetFluid,
                tickCounter,
                dollEntityId,
                dollProgress,
                output.copy());
    }

    void restoreTransferSnapshot(BarrelSnapshot snapshot) {
        fluidId = snapshot.fluidId();
        fluidAmount = snapshot.fluidAmount();
        compostAmount = snapshot.compostAmount();
        compostProgress = snapshot.compostProgress();
        transitionProgress = snapshot.transitionProgress();
        transitionTargetFluid = snapshot.transitionTargetFluid();
        tickCounter = snapshot.tickCounter();
        dollEntityId = snapshot.dollEntityId();
        dollProgress = snapshot.dollProgress();
        output = snapshot.output().copy();
        setChanged();
    }

    public ItemStack getExtractableOutput() {
        if (!output.isEmpty()) {
            return output;
        }
        return isCompostReady() ? new ItemStack(Items.DIRT) : ItemStack.EMPTY;
    }

    public void setOutput(ItemStack stack) {
        output = stack.copy();
        setChanged();
    }

    public ItemStack extractOutput(int amount) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        if (output.isEmpty() && isCompostReady()) {
            clearCompost();
            return new ItemStack(Items.DIRT);
        }
        if (output.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = output.split(amount);
        if (output.isEmpty()) {
            output = ItemStack.EMPTY;
        }
        setChanged();
        return extracted;
    }

    public boolean addOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        if (output.isEmpty()) {
            output = stack.copy();
            setChanged();
            return true;
        }
        if (ItemStack.isSameItemSameComponents(output, stack)
                && output.getCount() + stack.getCount() <= output.getMaxStackSize()) {
            output.grow(stack.getCount());
            setChanged();
            return true;
        }
        return false;
    }

    public boolean hasCompleteCompost() {
        return compostAmount >= COMPOST_COMPLETE;
    }

    public boolean canCompost() {
        return !hasFluid() && !hasCompleteCompost() && output.isEmpty();
    }

    public boolean isStoneBarrel() {
        Identifier id = BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock());
        return id != null && id.getNamespace().equals(ExampleMod.MODID) && id.getPath().equals("stone_barrel");
    }

    public boolean canHoldFluid(String id) {
        return !ExNihiloFluidIds.LAVA.equals(id) || isStoneBarrel();
    }

    public boolean canAcceptFluid(String id, int amount) {
        if (id.isEmpty() || compostAmount > 0 || amount <= 0 || !canHoldFluid(id)) {
            return false;
        }
        if (hasFluid() && !fluidId.equals(id)) {
            return false;
        }
        return fluidAmount + amount <= CAPACITY;
    }

    public boolean fillFluid(String id, int amount) {
        if (!canAcceptFluid(id, amount)) {
            return false;
        }
        if (!id.equals(fluidId)) {
            clearTransitionProgress();
        }
        fluidId = id;
        fluidAmount += amount;
        setChanged();
        return true;
    }

    public String drainBucket() {
        if (fluidAmount < 1000 || fluidId.isEmpty()) {
            return "";
        }
        String drained = fluidId;
        fluidAmount -= 1000;
        if (fluidAmount <= 0) {
            fluidAmount = 0;
            fluidId = "";
            clearTransitionProgress();
        }
        setChanged();
        return drained;
    }

    public void clearFluid() {
        fluidId = "";
        fluidAmount = 0;
        clearTransitionProgress();
        setChanged();
    }

    public boolean consumeFluid(int amount) {
        if (fluidAmount < amount) {
            return false;
        }
        clearTransitionProgress();
        fluidAmount -= amount;
        if (fluidAmount <= 0) {
            clearFluid();
        } else {
            setChanged();
        }
        return true;
    }

    public boolean startDollSpawn(String entityId) {
        if (!canStartDollSpawn()) {
            return false;
        }
        dollEntityId = entityId;
        dollProgress = 0;
        setChanged();
        return true;
    }

    public boolean canStartDollSpawn() {
        return fluidAmount >= CAPACITY && !fluidId.isEmpty() && dollEntityId.isEmpty() && compostAmount <= 0;
    }

    public void addCompost(int amount) {
        compostAmount = Math.min(COMPOST_COMPLETE, compostAmount + amount);
        if (compostAmount < COMPOST_COMPLETE) {
            compostProgress = 0;
        }
        setChanged();
    }

    public void clearCompost() {
        compostAmount = 0;
        compostProgress = 0;
        setChanged();
    }

    public boolean collectCompostOutput() {
        if (!isCompostReady() || !addOutput(new ItemStack(Items.DIRT))) {
            return false;
        }
        clearCompost();
        return true;
    }

    public boolean isCompostReady() {
        return hasCompleteCompost() && compostProgress >= COMPOST_TICKS;
    }

    private void tickCompost() {
        if (isCompostReady()) {
            return;
        }
        clearTransitionProgress();
        compostProgress++;
        if (isCompostReady()) {
            MachineInteraction.complete(level, worldPosition);
        }
        setChanged();
    }

    private void tickDoll(ServerLevel level, BlockPos pos) {
        clearTransitionProgress();
        dollProgress++;
        if (dollProgress >= DOLL_TICKS && DollSpawnData.spawn(level, pos, dollEntityId)) {
            clearFluid();
            dollEntityId = "";
            dollProgress = 0;
        }
        setChanged();
    }

    private void tickTransition(ServerLevel serverLevel) {
        if (fluidAmount < CAPACITY || fluidId.isEmpty() || compostAmount > 0 || !dollEntityId.isEmpty()) {
            clearTransitionProgress();
            return;
        }
        BlockState catalyst = serverLevel.getBlockState(worldPosition.below());
        ExNihiloMachineData.TransitionResult result =
                ExNihiloMachineData.getTransition(serverLevel, fluidId, fluidAmount, catalyst);
        if (result == null || result.resultFluid().isEmpty() || result.resultFluid().equals(fluidId)) {
            clearTransitionProgress();
            return;
        }
        if (!result.resultFluid().equals(transitionTargetFluid)) {
            transitionTargetFluid = result.resultFluid();
            transitionProgress = 0;
        }
        transitionProgress++;
        if (transitionProgress >= TRANSITION_TICKS) {
            fluidId = result.resultFluid();
            fluidAmount = Math.max(result.amount(), Math.min(CAPACITY, fluidAmount));
            clearTransitionProgress();
            MachineInteraction.complete(serverLevel, worldPosition);
        }
        setChanged();
    }

    private void clearTransitionProgress() {
        if (transitionProgress != 0 || !transitionTargetFluid.isEmpty()) {
            transitionProgress = 0;
            transitionTargetFluid = "";
            setChanged();
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fluidId = input.getStringOr("fluid", "");
        fluidAmount = input.getIntOr("fluid_amount", 0);
        compostAmount = input.getIntOr("compost", 0);
        compostProgress = input.getIntOr("compost_progress", 0);
        transitionProgress = input.getIntOr("transition_progress", 0);
        transitionTargetFluid = input.getStringOr("transition_target_fluid", "");
        dollEntityId = input.getStringOr("doll_entity", "");
        dollProgress = input.getIntOr("doll_progress", 0);
        output = input.read("output", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("fluid", fluidId);
        output.putInt("fluid_amount", fluidAmount);
        output.putInt("compost", compostAmount);
        output.putInt("compost_progress", compostProgress);
        output.putInt("transition_progress", transitionProgress);
        output.putString("transition_target_fluid", transitionTargetFluid);
        output.putString("doll_entity", dollEntityId);
        output.putInt("doll_progress", dollProgress);
        if (!this.output.isEmpty()) {
            output.store("output", ItemStack.OPTIONAL_CODEC, this.output);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    record BarrelSnapshot(
            String fluidId,
            int fluidAmount,
            int compostAmount,
            int compostProgress,
            int transitionProgress,
            String transitionTargetFluid,
            int tickCounter,
            String dollEntityId,
            int dollProgress,
            ItemStack output) {
    }
}
