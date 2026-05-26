package com.example.newexnihilo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class InfestingLeavesBlockEntity extends BlockEntity {
    public static final int INFESTING_TICKS = 200;
    private static final int SYNC_INTERVAL = 5;

    private int progress;
    private int spreadTimer;
    private BlockState originalState = Blocks.OAK_LEAVES.defaultBlockState();

    public InfestingLeavesBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModContent.INFESTING_LEAVES_BLOCK_ENTITY.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, InfestingLeavesBlockEntity leaves) {
        if (level.isClientSide()) {
            return;
        }
        leaves.progress++;
        leaves.spreadTimer++;
        if (leaves.spreadTimer >= 60) {
            leaves.spreadTimer = 0;
            InfestingLeavesBlock.spread(level, pos);
        }
        if (leaves.progress >= INFESTING_TICKS) {
            level.setBlock(pos, InfestingLeavesBlock.infestedStateFor(leaves.originalState),
                    Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            if (level.getBlockEntity(pos) instanceof InfestedLeavesBlockEntity infestedLeaves) {
                infestedLeaves.setOriginalState(leaves.originalState);
            }
        } else {
            leaves.setChanged(leaves.progress % SYNC_INTERVAL == 0 || leaves.spreadTimer == 0);
        }
    }

    public void setOriginalState(BlockState originalState) {
        if (InfestingLeavesBlock.canInfest(originalState)) {
            this.originalState = originalState;
        }
        setChanged(true);
    }

    public BlockState getOriginalState() {
        return originalState;
    }

    public InfestingLeavesVariant getVariant() {
        return getBlockState().getValue(InfestingLeavesVariantBlock.VARIANT);
    }

    public float getProgressRatio() {
        return Math.min(1.0F, Math.max(0.0F, progress / (float) INFESTING_TICKS));
    }

    public int getRemainingTicks() {
        return Math.max(0, INFESTING_TICKS - progress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        progress = input.getIntOr("progress", 0);
        spreadTimer = input.getIntOr("spread_timer", 0);
        originalState = input.read("original_state", BlockState.CODEC)
                .filter(InfestingLeavesBlock::canInfest)
                .orElse(Blocks.OAK_LEAVES.defaultBlockState());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("progress", progress);
        output.putInt("spread_timer", spreadTimer);
        output.store("original_state", BlockState.CODEC, originalState);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    private void setChanged(boolean sync) {
        super.setChanged();
        if (sync && level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void setChanged() {
        setChanged(false);
    }
}
