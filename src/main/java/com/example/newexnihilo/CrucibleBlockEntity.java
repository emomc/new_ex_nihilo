package com.example.newexnihilo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CrucibleBlockEntity extends BlockEntity {
    public static final int CAPACITY = 4000;
    private static final int MELT_TARGET = 100;
    private static final int INPUT_STACK_LIMIT = 64;

    private ItemStack input = ItemStack.EMPTY;
    private String targetFluid = "";
    private int targetAmount;
    private String storedFluid = "";
    private int storedAmount;
    private int progress;

    public CrucibleBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModContent.CRUCIBLE_BLOCK_ENTITY.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible) {
        if (!(level instanceof ServerLevel serverLevel) || crucible.input.isEmpty()) {
            return;
        }
        int heat = ExNihiloMachineData.getHeat(serverLevel, level.getBlockState(pos.below()));
        String type = crucible.getCrucibleType();
        if (heat <= 0 && !"wood".equals(type)) {
            return;
        }
        crucible.progress += Math.max(1, heat);
        if (crucible.progress >= MELT_TARGET) {
            crucible.finishMelt();
        } else {
            crucible.setChanged();
        }
    }

    public String getCrucibleType() {
        if (level == null) {
            return "fired";
        }
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath();
        if (path.equals("fired_crucible")) {
            return "fired";
        }
        if (path.equals("unfired_crucible")) {
            return "unfired";
        }
        return "wood";
    }

    public boolean hasInput() {
        return !input.isEmpty();
    }

    public ItemStack getInput() {
        return input;
    }

    CrucibleSnapshot createTransferSnapshot() {
        return new CrucibleSnapshot(
                input.copy(),
                targetFluid,
                targetAmount,
                storedFluid,
                storedAmount,
                progress);
    }

    void restoreTransferSnapshot(CrucibleSnapshot snapshot) {
        input = snapshot.input().copy();
        targetFluid = snapshot.targetFluid();
        targetAmount = snapshot.targetAmount();
        storedFluid = snapshot.storedFluid();
        storedAmount = snapshot.storedAmount();
        progress = snapshot.progress();
        setChanged();
    }

    public String getStoredFluidId() {
        return storedFluid;
    }

    public String getTargetFluidId() {
        return targetFluid;
    }

    public int getStoredAmount() {
        return storedAmount;
    }

    public int getTargetAmount() {
        return targetAmount;
    }

    public Fluid getFluid() {
        return ExNihiloFluidIds.fluidFor(storedFluid);
    }

    public float getFluidProportion() {
        return (float) storedAmount / CAPACITY;
    }

    public int getProgress() {
        return progress;
    }

    public int getInputCount() {
        return input.getCount();
    }

    public int getInputLimit() {
        if (targetAmount > 0) {
            return Math.min(INPUT_STACK_LIMIT, Math.max(1, CAPACITY / targetAmount));
        }
        return INPUT_STACK_LIMIT;
    }

    public float getProgressRatio() {
        return Math.min(1.0F, progress / (float) MELT_TARGET);
    }

    public float getQueuedFluidProportion() {
        return Math.min(1.0F, queuedFluidAmount() / (float) CAPACITY);
    }

    public float getSolidProportion() {
        if (input.isEmpty() || targetAmount <= 0) {
            return 0.0F;
        }
        float activeMelted = targetAmount * getProgressRatio();
        float remainingSolid = Math.max(0.0F, targetAmount * input.getCount() - activeMelted);
        return Math.min(1.0F, remainingSolid / (float) CAPACITY);
    }

    public int getReservedFluidAmount() {
        return queuedFluidAmount();
    }

    public int getAvailableFluidCapacity(String fluid) {
        if (fluid.isEmpty()) {
            return 0;
        }
        if (!storedFluid.isEmpty() && !storedFluid.equals(fluid)) {
            return 0;
        }
        if (!targetFluid.isEmpty() && !targetFluid.equals(fluid)) {
            return 0;
        }
        return Math.max(0, CAPACITY - storedAmount - queuedFluidAmount());
    }

    public boolean canAcceptFluid(String fluid, int amount) {
        if (fluid.isEmpty() || amount <= 0 || amount > getAvailableFluidCapacity(fluid)) {
            return false;
        }
        return true;
    }

    public boolean tryInsert(ItemStack stack, ServerLevel level) {
        ExNihiloMachineData.MeltingResult result = getAcceptedMeltingResult(stack, level);
        if (result == null) {
            return false;
        }
        if (input.isEmpty()) {
            input = stack.copyWithCount(1);
        } else {
            input.grow(1);
        }
        targetFluid = result.fluidId();
        targetAmount = result.amount();
        setChanged();
        return true;
    }

    public ExNihiloMachineData.MeltingResult getAcceptedMeltingResult(ItemStack stack, ServerLevel level) {
        ExNihiloMachineData.MeltingResult result = ExNihiloMachineData.getMeltingResult(level, stack, getCrucibleType());
        if (result == null || !canQueueMelting(stack, result)) {
            return null;
        }
        return result;
    }

    public boolean fillFluid(String id, int amount) {
        if (id.isEmpty() || amount <= 0 || !canAcceptFluid(id, amount)) {
            return false;
        }
        storedFluid = id;
        storedAmount += amount;
        setChanged();
        return true;
    }

    public String drainBucket() {
        if (storedAmount < 1000 || storedFluid.isEmpty()) {
            return "";
        }
        String drained = storedFluid;
        storedAmount -= 1000;
        if (storedAmount <= 0) {
            storedAmount = 0;
            storedFluid = "";
        }
        setChanged();
        return drained;
    }

    public boolean consumeFluid(int amount) {
        if (storedAmount < amount || amount <= 0) {
            return false;
        }
        storedAmount -= amount;
        if (storedAmount <= 0) {
            storedAmount = 0;
            storedFluid = "";
        }
        setChanged();
        return true;
    }

    public ItemStack removeInput() {
        ItemStack stack = input.copy();
        input = ItemStack.EMPTY;
        targetFluid = "";
        targetAmount = 0;
        progress = 0;
        setChanged();
        return stack;
    }

    public ItemStack extractInput(int amount) {
        if (amount <= 0 || input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = input.split(Math.min(amount, input.getCount()));
        if (input.isEmpty()) {
            input = ItemStack.EMPTY;
            targetFluid = "";
            targetAmount = 0;
            progress = 0;
        }
        setChanged();
        return extracted;
    }

    private void finishMelt() {
        if (targetFluid.isEmpty() || targetAmount <= 0 || !canStoreFluid(targetFluid, targetAmount)) {
            progress = MELT_TARGET;
            setChanged();
            return;
        }
        storedFluid = targetFluid;
        storedAmount += targetAmount;
        input.shrink(1);
        if (input.isEmpty()) {
            input = ItemStack.EMPTY;
            targetFluid = "";
            targetAmount = 0;
        }
        progress = 0;
        MachineInteraction.complete(level, worldPosition);
        setChanged();
    }

    private boolean canQueueMelting(ItemStack stack, ExNihiloMachineData.MeltingResult result) {
        if (result.amount() <= 0 || storedAmount + queuedFluidAmount() + result.amount() > CAPACITY) {
            return false;
        }
        if (!storedFluid.isEmpty() && !storedFluid.equals(result.fluidId())) {
            return false;
        }
        if (input.isEmpty()) {
            return true;
        }
        int inputLimit = Math.min(getInputLimitFor(result.amount()), input.getMaxStackSize());
        if (!ItemStack.isSameItemSameComponents(input, stack) || input.getCount() >= inputLimit) {
            return false;
        }
        return targetFluid.equals(result.fluidId()) && targetAmount == result.amount();
    }

    private int getInputLimitFor(int amountPerItem) {
        if (amountPerItem <= 0) {
            return 0;
        }
        return Math.min(INPUT_STACK_LIMIT, Math.max(1, CAPACITY / amountPerItem));
    }

    private boolean canStoreFluid(String fluid, int amount) {
        if (fluid.isEmpty() || amount <= 0 || storedAmount + amount > CAPACITY) {
            return false;
        }
        return storedFluid.isEmpty() || storedFluid.equals(fluid);
    }

    private int queuedFluidAmount() {
        return input.isEmpty() ? 0 : targetAmount * input.getCount();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.input = input.read("input", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        targetFluid = input.getStringOr("target_fluid", "");
        targetAmount = input.getIntOr("target_amount", 0);
        storedFluid = input.getStringOr("stored_fluid", "");
        storedAmount = input.getIntOr("stored_amount", 0);
        progress = input.getIntOr("progress", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!input.isEmpty()) {
            output.store("input", ItemStack.OPTIONAL_CODEC, input);
        }
        output.putString("target_fluid", targetFluid);
        output.putInt("target_amount", targetAmount);
        output.putString("stored_fluid", storedFluid);
        output.putInt("stored_amount", storedAmount);
        output.putInt("progress", progress);
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

    record CrucibleSnapshot(
            ItemStack input,
            String targetFluid,
            int targetAmount,
            String storedFluid,
            int storedAmount,
            int progress) {
    }
}
