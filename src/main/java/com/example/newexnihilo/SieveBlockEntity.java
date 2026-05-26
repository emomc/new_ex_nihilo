package com.example.newexnihilo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SieveBlockEntity extends BlockEntity {
    public static final float MAX_SIEVE_CLICKS = 10.0F;

    private ItemStack mesh = ItemStack.EMPTY;
    private ItemStack input = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private float progress;
    private long lastSieveAction = -1L;

    public SieveBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModContent.SIEVE_BLOCK_ENTITY.get(), pos, blockState);
    }

    public ItemStack getMesh() {
        return mesh;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    SieveSnapshot createTransferSnapshot() {
        return new SieveSnapshot(mesh.copy(), input.copy(), output.copy(), progress, lastSieveAction);
    }

    void restoreTransferSnapshot(SieveSnapshot snapshot) {
        mesh = snapshot.mesh().copy();
        input = snapshot.input().copy();
        output = snapshot.output().copy();
        progress = snapshot.progress();
        lastSieveAction = snapshot.lastSieveAction();
        setChanged();
    }

    public float getProgress() {
        return progress;
    }

    public float getProgressRatio() {
        return Math.min(1.0F, progress / MAX_SIEVE_CLICKS);
    }

    public MeshType getMeshType() {
        return mesh.getItem() instanceof MeshItem meshItem ? meshItem.getType() : MeshType.NONE;
    }

    public boolean hasMesh() {
        return !mesh.isEmpty();
    }

    public boolean hasInput() {
        return !input.isEmpty();
    }

    public void setMesh(ItemStack stack) {
        mesh = stack.copyWithCount(1);
        progress = 0;
        setChanged();
    }

    public ItemStack removeMesh() {
        ItemStack stack = mesh;
        mesh = ItemStack.EMPTY;
        progress = 0;
        setChanged();
        return stack;
    }

    public void setInput(ItemStack stack) {
        input = stack.copyWithCount(1);
        progress = 0;
        setChanged();
    }

    public boolean canAcceptInput(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty() || hasInput() || !hasMesh()) {
            return false;
        }
        Block inputBlock = Block.byItem(stack.getItem());
        return ExNihiloDropData.canSift(level, inputBlock.defaultBlockState(), getMeshType());
    }

    public boolean insertInput(ItemStack stack, Player player) {
        if (!(level instanceof ServerLevel serverLevel) || !canAcceptInput(serverLevel, stack)) {
            return false;
        }
        setInput(stack);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return true;
    }

    public ItemStack removeInput() {
        ItemStack stack = input;
        input = ItemStack.EMPTY;
        progress = 0;
        setChanged();
        return stack;
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

    public ItemStack extractOutput(int amount) {
        if (output.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = output.split(amount);
        if (output.isEmpty()) {
            output = ItemStack.EMPTY;
        }
        setChanged();
        return extracted;
    }

    public boolean collectOutput(Player player) {
        if (output.isEmpty()) {
            return false;
        }
        MachineInteraction.give(player, extractOutput(output.getCount()));
        return true;
    }

    public boolean advance() {
        if (!hasMesh() || !hasInput()) {
            return false;
        }
        progress += 1.0F;
        setChanged();
        return progress >= MAX_SIEVE_CLICKS;
    }

    public boolean advanceAndFinish(ServerLevel level) {
        if (!hasMesh() || !hasInput()) {
            return false;
        }
        long gameTime = level.getLevelData().getGameTime();
        if (gameTime == lastSieveAction) {
            return false;
        }
        lastSieveAction = gameTime;
        if (!advance()) {
            level.playSound(null, worldPosition, SoundEvents.BRUSH_GRAVEL, SoundSource.BLOCKS,
                    0.28F, 0.9F + level.getRandom().nextFloat() * 0.2F);
            return true;
        }
        Block inputBlock = Block.byItem(input.getItem());
        for (ItemStack drop : ExNihiloDropData.rollSieveDrops(
                level, inputBlock.defaultBlockState(), getMeshType(), level.getRandom())) {
            if (!drop.isEmpty()) {
                Block.popResource(level, worldPosition, drop);
            }
        }
        finish();
        level.playSound(null, worldPosition, SoundEvents.BRUSH_GRAVEL_COMPLETED, SoundSource.BLOCKS, 0.55F, 1.0F);
        return true;
    }

    public void finish() {
        input = ItemStack.EMPTY;
        progress = 0;
        setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        mesh = input.read("mesh", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.input = input.read("input", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        output = input.read("output", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        progress = input.getFloatOr("progress", input.getIntOr("progress", 0));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!mesh.isEmpty()) {
            output.store("mesh", ItemStack.OPTIONAL_CODEC, mesh);
        }
        if (!input.isEmpty()) {
            output.store("input", ItemStack.OPTIONAL_CODEC, input);
        }
        if (!this.output.isEmpty()) {
            output.store("output", ItemStack.OPTIONAL_CODEC, this.output);
        }
        output.putFloat("progress", progress);
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

    record SieveSnapshot(ItemStack mesh, ItemStack input, ItemStack output, float progress, long lastSieveAction) {
    }
}
