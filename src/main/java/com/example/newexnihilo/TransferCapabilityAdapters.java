package com.example.newexnihilo;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

final class TransferCapabilityAdapters {
    private static final String CAPABILITIES = "net.neoforged.neoforge.capabilities.Capabilities$";
    private static final String RESOURCE_HANDLER = "net.neoforged.neoforge.transfer.ResourceHandler";
    private static final String ITEM_RESOURCE = "net.neoforged.neoforge.transfer.item.ItemResource";
    private static final String FLUID_RESOURCE = "net.neoforged.neoforge.transfer.fluid.FluidResource";

    private TransferCapabilityAdapters() {
    }

    static boolean hasTransferApi() {
        return classExists(CAPABILITIES + "Item") && classExists(CAPABILITIES + "Fluid") && classExists(RESOURCE_HANDLER);
    }

    static BlockCapability<?, Direction> itemCapability() {
        return blockCapability(hasTransferApi() ? "Item" : "ItemHandler");
    }

    static BlockCapability<?, Direction> fluidCapability() {
        return blockCapability(hasTransferApi() ? "Fluid" : "FluidHandler");
    }

    static Object itemHandler(BlockEntity blockEntity) {
        if (!hasTransferApi()) {
            return legacyItemHandler(blockEntity);
        }
        Object legacy = legacyItemHandler(blockEntity);
        return proxy((proxy, method, args) -> handleItem(legacy, method, args));
    }

    static Object fluidHandler(BlockEntity blockEntity) {
        if (!hasTransferApi()) {
            return new MachineFluidHandler(blockEntity);
        }
        return proxy((proxy, method, args) -> handleFluid(blockEntity, method, args));
    }

    @SuppressWarnings("unchecked")
    private static BlockCapability<?, Direction> blockCapability(String nestedClass) {
        try {
            return (BlockCapability<?, Direction>) Class.forName(CAPABILITIES + nestedClass).getField("BLOCK").get(null);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to resolve NeoForge capability " + nestedClass, exception);
        }
    }

    private static Object legacyItemHandler(BlockEntity blockEntity) {
        if (blockEntity instanceof BarrelBlockEntity barrel) {
            return new BarrelItemHandler(barrel);
        }
        if (blockEntity instanceof CrucibleBlockEntity crucible) {
            return new CrucibleItemHandler(crucible);
        }
        if (blockEntity instanceof SieveBlockEntity sieve) {
            return new SieveItemHandler(sieve);
        }
        return null;
    }

    private static Object proxy(java.lang.reflect.InvocationHandler handler) {
        try {
            Class<?> resourceHandler = Class.forName(RESOURCE_HANDLER);
            return Proxy.newProxyInstance(resourceHandler.getClassLoader(), new Class<?>[] { resourceHandler }, handler);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create NeoForge transfer handler", exception);
        }
    }

    private static Object handleItem(Object legacy, Method method, Object[] args) throws ReflectiveOperationException {
        String name = method.getName();
        int slots = (int) legacy.getClass().getMethod("getSlots").invoke(legacy);
        if (name.equals("size")) {
            return slots;
        }
        if (name.equals("getResource")) {
            return itemResource((ItemStack) legacy.getClass().getMethod("getStackInSlot", int.class).invoke(legacy, args[0]));
        }
        if (name.equals("getAmountAsLong") || name.equals("getAmountAsInt")) {
            return amountResult(method, ((ItemStack) legacy.getClass().getMethod("getStackInSlot", int.class).invoke(legacy, args[0])).getCount());
        }
        if (name.equals("getCapacityAsLong") || name.equals("getCapacityAsInt")) {
            return amountResult(method, (int) legacy.getClass().getMethod("getSlotLimit", int.class).invoke(legacy, args[0]));
        }
        if (name.equals("isValid")) {
            return legacy.getClass().getMethod("isItemValid", int.class, ItemStack.class)
                    .invoke(legacy, args[0], itemStack(args[1], 1));
        }
        if (name.equals("insert")) {
            return args.length == 4 ? insertItem(legacy, (int) args[0], args[1], (int) args[2], args[3])
                    : insertItemAnySlot(legacy, args[0], (int) args[1], args[2], slots);
        }
        if (name.equals("extract")) {
            return args.length == 4 ? extractItem(legacy, (int) args[0], args[1], (int) args[2], args[3])
                    : extractItemAnySlot(legacy, args[0], (int) args[1], args[2], slots);
        }
        if (name.equals("toString")) {
            return "NewExNihiloTransferItemHandler[" + legacy + "]";
        }
        return defaultValue(method.getReturnType());
    }

    private static int insertItem(Object legacy, int slot, Object resource, int maxAmount, Object transaction) throws ReflectiveOperationException {
        if (maxAmount <= 0) {
            return 0;
        }
        ItemStack stack = itemStack(resource, maxAmount);
        if (stack.isEmpty()) {
            return 0;
        }
        updateItemSnapshots(legacy, transaction);
        ItemStack remainder = (ItemStack) legacy.getClass()
                .getMethod("insertItem", int.class, ItemStack.class, boolean.class)
                .invoke(legacy, slot, stack, false);
        return maxAmount - remainder.getCount();
    }

    private static int insertItemAnySlot(Object legacy, Object resource, int maxAmount, Object transaction, int slots) throws ReflectiveOperationException {
        int inserted = 0;
        for (int slot = 0; slot < slots && inserted < maxAmount; slot++) {
            inserted += insertItem(legacy, slot, resource, maxAmount - inserted, transaction);
        }
        return inserted;
    }

    private static int extractItem(Object legacy, int slot, Object resource, int maxAmount, Object transaction) throws ReflectiveOperationException {
        if (maxAmount <= 0) {
            return 0;
        }
        ItemStack current = (ItemStack) legacy.getClass().getMethod("getStackInSlot", int.class).invoke(legacy, slot);
        ItemStack requested = itemStack(resource, 1);
        if (current.isEmpty() || requested.isEmpty() || !ItemStack.isSameItemSameComponents(current, requested)) {
            return 0;
        }
        updateItemSnapshots(legacy, transaction);
        ItemStack extracted = (ItemStack) legacy.getClass()
                .getMethod("extractItem", int.class, int.class, boolean.class)
                .invoke(legacy, slot, maxAmount, false);
        return extracted.getCount();
    }

    private static int extractItemAnySlot(Object legacy, Object resource, int maxAmount, Object transaction, int slots) throws ReflectiveOperationException {
        int extracted = 0;
        for (int slot = 0; slot < slots && extracted < maxAmount; slot++) {
            extracted += extractItem(legacy, slot, resource, maxAmount - extracted, transaction);
        }
        return extracted;
    }

    private static Object handleFluid(BlockEntity blockEntity, Method method, Object[] args) throws ReflectiveOperationException {
        String name = method.getName();
        if (name.equals("size")) {
            return 1;
        }
        if (name.equals("getResource")) {
            String id = fluidId(blockEntity);
            return id.isEmpty() ? emptyFluidResource() : fluidResource(ExNihiloFluidIds.fluidFor(id));
        }
        if (name.equals("getAmountAsLong") || name.equals("getAmountAsInt")) {
            return amountResult(method, fluidAmount(blockEntity));
        }
        if (name.equals("getCapacityAsLong") || name.equals("getCapacityAsInt")) {
            return amountResult(method, fluidCapacity(blockEntity));
        }
        if (name.equals("isValid")) {
            return !fluidId(args[1]).isEmpty();
        }
        if (name.equals("insert")) {
            return args.length == 4 ? insertFluid(blockEntity, args[1], (int) args[2], args[3])
                    : insertFluid(blockEntity, args[0], (int) args[1], args[2]);
        }
        if (name.equals("extract")) {
            return args.length == 4 ? extractFluid(blockEntity, args[1], (int) args[2], args[3])
                    : extractFluid(blockEntity, args[0], (int) args[1], args[2]);
        }
        if (name.equals("toString")) {
            return "NewExNihiloTransferFluidHandler[" + blockEntity + "]";
        }
        return defaultValue(method.getReturnType());
    }

    private static int insertFluid(BlockEntity blockEntity, Object resource, int maxAmount, Object transaction) throws ReflectiveOperationException {
        String id = fluidId(resource);
        if (id.isEmpty() || maxAmount <= 0) {
            return 0;
        }
        if (!canFillFluid(blockEntity, id, 1)) {
            return 0;
        }
        int accepted = Math.min(maxAmount, availableFluidCapacity(blockEntity, id));
        if (accepted <= 0 || !canFillFluid(blockEntity, id, accepted)) {
            return 0;
        }
        updateBlockEntitySnapshots(blockEntity, transaction);
        if (blockEntity instanceof BarrelBlockEntity barrel) {
            barrel.fillFluid(id, accepted);
        } else if (blockEntity instanceof CrucibleBlockEntity crucible) {
            crucible.fillFluid(id, accepted);
        }
        return accepted;
    }

    private static int extractFluid(BlockEntity blockEntity, Object resource, int maxAmount, Object transaction) throws ReflectiveOperationException {
        String stored = fluidId(blockEntity);
        String requested = fluidId(resource);
        if (stored.isEmpty() || requested.isEmpty() || !stored.equals(requested) || maxAmount <= 0) {
            return 0;
        }
        int extracted = Math.min(maxAmount, fluidAmount(blockEntity));
        updateBlockEntitySnapshots(blockEntity, transaction);
        if (blockEntity instanceof BarrelBlockEntity barrel) {
            barrel.consumeFluid(extracted);
        } else if (blockEntity instanceof CrucibleBlockEntity crucible) {
            crucible.consumeFluid(extracted);
        }
        return extracted;
    }

    private static void updateItemSnapshots(Object legacy, Object transaction) throws ReflectiveOperationException {
        if (legacy instanceof BarrelItemHandler barrelItemHandler) {
            updateBlockEntitySnapshots(barrelItemHandler.barrel(), transaction);
        } else if (legacy instanceof SieveItemHandler sieveItemHandler) {
            updateBlockEntitySnapshots(sieveItemHandler.sieve(), transaction);
        } else if (legacy instanceof CrucibleItemHandler crucibleItemHandler) {
            updateBlockEntitySnapshots(crucibleItemHandler.crucible(), transaction);
        }
    }

    private static void updateBlockEntitySnapshots(BlockEntity blockEntity, Object transaction) throws ReflectiveOperationException {
        if (transaction == null || blockEntity == null) {
            return;
        }
        if (blockEntity instanceof BarrelBlockEntity barrel) {
            BarrelBlockEntity.BarrelSnapshot snapshot = barrel.createTransferSnapshot();
            updateSnapshots(new SnapshotJournal<BarrelBlockEntity.BarrelSnapshot>() {
                @Override
                protected BarrelBlockEntity.BarrelSnapshot createSnapshot() {
                    return snapshot;
                }

                @Override
                protected void revertToSnapshot(BarrelBlockEntity.BarrelSnapshot snapshot) {
                    barrel.restoreTransferSnapshot(snapshot);
                }
            }, transaction);
        } else if (blockEntity instanceof SieveBlockEntity sieve) {
            SieveBlockEntity.SieveSnapshot snapshot = sieve.createTransferSnapshot();
            updateSnapshots(new SnapshotJournal<SieveBlockEntity.SieveSnapshot>() {
                @Override
                protected SieveBlockEntity.SieveSnapshot createSnapshot() {
                    return snapshot;
                }

                @Override
                protected void revertToSnapshot(SieveBlockEntity.SieveSnapshot snapshot) {
                    sieve.restoreTransferSnapshot(snapshot);
                }
            }, transaction);
        } else if (blockEntity instanceof CrucibleBlockEntity crucible) {
            CrucibleBlockEntity.CrucibleSnapshot snapshot = crucible.createTransferSnapshot();
            updateSnapshots(new SnapshotJournal<CrucibleBlockEntity.CrucibleSnapshot>() {
                @Override
                protected CrucibleBlockEntity.CrucibleSnapshot createSnapshot() {
                    return snapshot;
                }

                @Override
                protected void revertToSnapshot(CrucibleBlockEntity.CrucibleSnapshot snapshot) {
                    crucible.restoreTransferSnapshot(snapshot);
                }
            }, transaction);
        }
    }

    private static void updateSnapshots(SnapshotJournal<?> journal, Object transaction) {
        journal.updateSnapshots((TransactionContext) transaction);
    }

    private static ItemStack itemStack(Object resource, int amount) throws ReflectiveOperationException {
        if (resource == null || amount <= 0) {
            return ItemStack.EMPTY;
        }
        return (ItemStack) resource.getClass().getMethod("toStack", int.class).invoke(resource, amount);
    }

    private static Object itemResource(ItemStack stack) throws ReflectiveOperationException {
        if (stack.isEmpty()) {
            return Class.forName(ITEM_RESOURCE).getField("EMPTY").get(null);
        }
        return Class.forName(ITEM_RESOURCE).getMethod("of", ItemStack.class).invoke(null, stack);
    }

    private static String fluidId(Object resource) {
        try {
            if (resource == null) {
                return "";
            }
            Fluid fluid = (Fluid) resource.getClass().getMethod("getFluid").invoke(resource);
            return ExNihiloFluidIds.idForFluid(fluid);
        } catch (ReflectiveOperationException exception) {
            return "";
        }
    }

    private static String fluidId(BlockEntity blockEntity) {
        if (blockEntity instanceof BarrelBlockEntity barrel) {
            return barrel.getFluidId();
        }
        if (blockEntity instanceof CrucibleBlockEntity crucible) {
            return crucible.getStoredFluidId();
        }
        return "";
    }

    private static int fluidAmount(BlockEntity blockEntity) {
        if (blockEntity instanceof BarrelBlockEntity barrel) {
            return barrel.getFluidAmount();
        }
        if (blockEntity instanceof CrucibleBlockEntity crucible) {
            return crucible.getStoredAmount();
        }
        return 0;
    }

    private static int fluidCapacity(BlockEntity blockEntity) {
        if (blockEntity instanceof BarrelBlockEntity) {
            return BarrelBlockEntity.CAPACITY;
        }
        if (blockEntity instanceof CrucibleBlockEntity) {
            return CrucibleBlockEntity.CAPACITY;
        }
        return 0;
    }

    private static boolean canFillFluid(BlockEntity blockEntity, String id, int amount) {
        if (blockEntity instanceof BarrelBlockEntity barrel) {
            return barrel.canAcceptFluid(id, amount);
        }
        if (blockEntity instanceof CrucibleBlockEntity crucible) {
            return crucible.canAcceptFluid(id, amount);
        }
        return false;
    }

    private static int availableFluidCapacity(BlockEntity blockEntity, String id) {
        if (blockEntity instanceof CrucibleBlockEntity crucible) {
            return crucible.getAvailableFluidCapacity(id);
        }
        return fluidCapacity(blockEntity) - fluidAmount(blockEntity);
    }

    private static Object fluidResource(Fluid fluid) throws ReflectiveOperationException {
        return Class.forName(FLUID_RESOURCE).getMethod("of", Fluid.class).invoke(null, fluid);
    }

    private static Object emptyFluidResource() throws ReflectiveOperationException {
        return Class.forName(FLUID_RESOURCE).getField("EMPTY").get(null);
    }

    private static Object amountResult(Method method, int amount) {
        return method.getReturnType() == long.class ? (long) amount : amount;
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        return null;
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, TransferCapabilityAdapters.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }
}
