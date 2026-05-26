package com.example.newexnihilo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class SieveItemHandler implements IItemHandler {
    private final SieveBlockEntity sieve;

    public SieveItemHandler(SieveBlockEntity sieve) {
        this.sieve = sieve;
    }

    SieveBlockEntity sieve() {
        return sieve;
    }

    @Override
    public int getSlots() {
        return 3;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return switch (slot) {
            case 0 -> sieve.getMesh();
            case 1 -> sieve.getInput();
            case 2 -> sieve.getOutput();
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (slot == 0 && stack.is(ModTags.MESHES) && !sieve.hasMesh()) {
            if (!simulate) {
                sieve.setMesh(stack);
            }
            return remainder(stack);
        }
        if (slot == 1 && canInsertInput(stack)) {
            if (!simulate) {
                sieve.setInput(stack);
            }
            return remainder(stack);
        }
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0 || slot != 2 || sieve.getOutput().isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = sieve.getOutput().copyWithCount(Math.min(amount, sieve.getOutput().getCount()));
        if (!simulate) {
            sieve.extractOutput(amount);
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot == 2 ? 64 : 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot == 0 ? stack.is(ModTags.MESHES) : slot == 1 && canInsertInput(stack);
    }

    private boolean canInsertInput(ItemStack stack) {
        if (!(sieve.getLevel() instanceof ServerLevel serverLevel)) {
            return false;
        }
        return sieve.canAcceptInput(serverLevel, stack);
    }

    private static ItemStack remainder(ItemStack stack) {
        ItemStack remainder = stack.copy();
        remainder.shrink(1);
        return remainder;
    }
}
