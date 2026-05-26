package com.example.newexnihilo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class InfestedLeavesBlock extends InfestingLeavesVariantBlock implements EntityBlock {
    public InfestedLeavesBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfestedLeavesBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModContent.INFESTED_LEAVES_BLOCK_ENTITY.get()
                ? (tickerLevel, tickerPos, tickerState, blockEntity) ->
                        InfestedLeavesBlockEntity.tick(tickerLevel, tickerPos, tickerState, (InfestedLeavesBlockEntity) blockEntity)
                : null;
    }
}
