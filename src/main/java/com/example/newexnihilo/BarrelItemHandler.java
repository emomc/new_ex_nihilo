package com.example.newexnihilo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class BarrelItemHandler implements IItemHandler {
    private final BarrelBlockEntity barrel;

    public BarrelItemHandler(BarrelBlockEntity barrel) {
        this.barrel = barrel;
    }

    BarrelBlockEntity barrel() {
        return barrel;
    }

    @Override
    public int getSlots() {
        return 2;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot == 1 ? barrel.getExtractableOutput() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot != 0 || stack.isEmpty() || !(barrel.getLevel() instanceof ServerLevel serverLevel)) {
            return stack;
        }
        if (tryDoll(stack, simulate) || tryPrecipitate(stack, serverLevel, simulate) || tryCompost(stack, serverLevel, simulate)) {
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            return remainder;
        }
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack output = barrel.getExtractableOutput();
        if (slot != 1 || amount <= 0 || output.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = output.copyWithCount(Math.min(amount, output.getCount()));
        if (!simulate) {
            barrel.extractOutput(amount);
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot == 1 ? 64 : 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot == 0 && !stack.isEmpty();
    }

    private boolean tryDoll(ItemStack stack, boolean simulate) {
        DollSpawnData.DollSpec dollSpec = DollSpawnData.get(stack);
        if (dollSpec == null || !dollSpec.fluidId().equals(barrel.getFluidId()) || !barrel.canStartDollSpawn()) {
            return false;
        }
        if (!simulate) {
            return barrel.startDollSpawn(dollSpec.entityId());
        }
        return true;
    }

    private boolean tryPrecipitate(ItemStack stack, ServerLevel level, boolean simulate) {
        if (!barrel.hasFluid()) {
            return false;
        }
        ItemStack result = ExNihiloMachineData.getPrecipitateResult(level, barrel.getFluidId(), barrel.getFluidAmount(), stack);
        if (result.isEmpty() || !canMergeOutput(result)) {
            return false;
        }
        if (!simulate) {
            if (!barrel.consumeFluid(1000)) {
                return false;
            }
            return barrel.addOutput(result);
        }
        return true;
    }

    private boolean tryCompost(ItemStack stack, ServerLevel level, boolean simulate) {
        if (!barrel.canCompost()) {
            return false;
        }
        int amount = ExNihiloMachineData.getCompostAmount(level, stack);
        if (amount <= 0) {
            return false;
        }
        if (!simulate) {
            barrel.addCompost(amount);
        }
        return true;
    }

    private boolean canMergeOutput(ItemStack stack) {
        return barrel.getOutput().isEmpty()
                || (ItemStack.isSameItemSameComponents(barrel.getOutput(), stack)
                && barrel.getOutput().getCount() + stack.getCount() <= barrel.getOutput().getMaxStackSize());
    }
}
