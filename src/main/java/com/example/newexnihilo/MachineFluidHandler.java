package com.example.newexnihilo;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class MachineFluidHandler implements IFluidHandler {
    private final BlockEntity blockEntity;

    public MachineFluidHandler(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        String id = fluidId();
        int amount = amount();
        return id.isEmpty() || amount <= 0 ? FluidStack.EMPTY : new FluidStack(ExNihiloFluidIds.fluidFor(id), amount);
    }

    @Override
    public int getTankCapacity(int tank) {
        if (blockEntity instanceof BarrelBlockEntity) {
            return BarrelBlockEntity.CAPACITY;
        }
        if (blockEntity instanceof CrucibleBlockEntity) {
            return CrucibleBlockEntity.CAPACITY;
        }
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return !stack.isEmpty() && !ExNihiloFluidIds.idForFluid(stack.getFluid()).isEmpty();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        String id = ExNihiloFluidIds.idForFluid(resource.getFluid());
        if (id.isEmpty()) {
            return 0;
        }
        if (!canFill(id, 1)) {
            return 0;
        }
        int accepted = Math.min(resource.getAmount(), availableCapacity(id));
        if (accepted <= 0 || !canFill(id, accepted)) {
            return 0;
        }
        if (action.execute()) {
            fillMachine(id, accepted);
        }
        return accepted;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        String id = fluidId();
        if (resource.isEmpty() || id.isEmpty() || !resource.getFluid().isSame(ExNihiloFluidIds.fluidFor(id))) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        String id = fluidId();
        if (id.isEmpty() || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        int drained = Math.min(maxDrain, amount());
        if (action.execute()) {
            consumeMachine(drained);
        }
        return new FluidStack(ExNihiloFluidIds.fluidFor(id), drained);
    }

    private String fluidId() {
        if (blockEntity instanceof BarrelBlockEntity barrel) {
            return barrel.getFluidId();
        }
        if (blockEntity instanceof CrucibleBlockEntity crucible) {
            return crucible.getStoredFluidId();
        }
        return "";
    }

    private int amount() {
        if (blockEntity instanceof BarrelBlockEntity barrel) {
            return barrel.getFluidAmount();
        }
        if (blockEntity instanceof CrucibleBlockEntity crucible) {
            return crucible.getStoredAmount();
        }
        return 0;
    }

    private boolean canFill(String id, int amount) {
        if (blockEntity instanceof BarrelBlockEntity barrel) {
            return barrel.canAcceptFluid(id, amount);
        }
        if (blockEntity instanceof CrucibleBlockEntity crucible) {
            return crucible.canAcceptFluid(id, amount);
        }
        return false;
    }

    private int availableCapacity(String id) {
        if (blockEntity instanceof CrucibleBlockEntity crucible) {
            return crucible.getAvailableFluidCapacity(id);
        }
        return getTankCapacity(0) - amount();
    }

    private void fillMachine(String id, int amount) {
        if (blockEntity instanceof BarrelBlockEntity barrel) {
            barrel.fillFluid(id, amount);
        } else if (blockEntity instanceof CrucibleBlockEntity crucible) {
            crucible.fillFluid(id, amount);
        }
    }

    private void consumeMachine(int amount) {
        if (blockEntity instanceof BarrelBlockEntity barrel) {
            barrel.consumeFluid(amount);
        } else if (blockEntity instanceof CrucibleBlockEntity crucible) {
            crucible.consumeFluid(amount);
        }
    }
}
