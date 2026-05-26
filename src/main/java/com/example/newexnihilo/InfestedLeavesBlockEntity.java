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

public class InfestedLeavesBlockEntity extends BlockEntity {
    private int spreadTimer;
    private BlockState originalState = Blocks.OAK_LEAVES.defaultBlockState();

    public InfestedLeavesBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModContent.INFESTED_LEAVES_BLOCK_ENTITY.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, InfestedLeavesBlockEntity leaves) {
        if (level.isClientSide()) {
            return;
        }
        leaves.spreadTimer++;
        if (leaves.spreadTimer >= 100) {
            leaves.spreadTimer = 0;
            InfestingLeavesBlock.spread(level, pos);
        }
        leaves.setChanged();
    }

    public void setOriginalState(BlockState originalState) {
        if (InfestingLeavesBlock.canInfest(originalState)) {
            this.originalState = originalState;
        }
        setChanged();
    }

    public BlockState getOriginalState() {
        return originalState;
    }

    public InfestingLeavesVariant getVariant() {
        return getBlockState().getValue(InfestingLeavesVariantBlock.VARIANT);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        spreadTimer = input.getIntOr("spread_timer", 0);
        originalState = input.read("original_state", BlockState.CODEC)
                .filter(InfestingLeavesBlock::canInfest)
                .orElse(Blocks.OAK_LEAVES.defaultBlockState());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
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

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }
}
