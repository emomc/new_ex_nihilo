package com.example.newexnihilo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class CrucibleItemHandler implements IItemHandler {
    private final CrucibleBlockEntity crucible;

    public CrucibleItemHandler(CrucibleBlockEntity crucible) {
        this.crucible = crucible;
    }

    CrucibleBlockEntity crucible() {
        return crucible;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot == 0 ? crucible.getInput() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot != 0 || stack.isEmpty() || !(crucible.getLevel() instanceof ServerLevel serverLevel)
                || crucible.getAcceptedMeltingResult(stack, serverLevel) == null) {
            return stack;
        }
        ItemStack remainder = stack.copy();
        while (!remainder.isEmpty() && crucible.getAcceptedMeltingResult(remainder, serverLevel) != null) {
            if (!simulate) {
                crucible.tryInsert(remainder, serverLevel);
            }
            remainder.shrink(1);
        }
        return remainder;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 0 || amount <= 0 || !crucible.hasInput()) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = crucible.getInput().copyWithCount(Math.min(amount, crucible.getInput().getCount()));
        if (!simulate) {
            crucible.extractInput(extracted.getCount());
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return crucible.getInputLimit();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot == 0 && crucible.getLevel() instanceof ServerLevel serverLevel
                && crucible.getAcceptedMeltingResult(stack, serverLevel) != null;
    }
}
