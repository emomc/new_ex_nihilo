package com.example.newexnihilo;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class InfestingLeavesBlock extends InfestingLeavesVariantBlock implements EntityBlock {
    public InfestingLeavesBlock(Properties properties) {
        super(properties);
    }

    public static void startInfesting(Level level, BlockPos pos) {
        BlockState originalState = level.getBlockState(pos);
        if (!canInfest(originalState)) {
            return;
        }
        level.setBlock(pos, infestingStateFor(originalState),
                Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
        if (level.getBlockEntity(pos) instanceof InfestingLeavesBlockEntity leaves) {
            leaves.setOriginalState(originalState);
        }
    }

    public static void spread(Level level, BlockPos pos) {
        for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (nearby.equals(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(nearby);
            if (canInfest(state) && level.getRandom().nextFloat() <= 0.15F) {
                startInfesting(level, nearby);
            }
        }
    }

    public static boolean canInfest(BlockState state) {
        return state.is(BlockTags.LEAVES)
                && state.getBlock() != ModContent.INFESTING_LEAVES_BLOCK.get()
                && state.getBlock() != ModContent.INFESTED_LEAVES_BLOCK.get();
    }

    public static BlockState infestingStateFor(BlockState originalState) {
        return leafStateFor(originalState, ModContent.INFESTING_LEAVES_BLOCK.get().defaultBlockState());
    }

    public static BlockState infestedStateFor(BlockState originalState) {
        return leafStateFor(originalState, ModContent.INFESTED_LEAVES_BLOCK.get().defaultBlockState());
    }

    private static BlockState leafStateFor(BlockState originalState, BlockState fallbackState) {
        Block block = originalState.getBlock();
        if (block == Blocks.OAK_LEAVES || block == Blocks.CHERRY_LEAVES || block == Blocks.PALE_OAK_LEAVES
                || block == Blocks.FLOWERING_AZALEA_LEAVES) {
            return fallbackState;
        }
        if (block == Blocks.SPRUCE_LEAVES) {
            return fallbackState.setValue(InfestingLeavesVariantBlock.VARIANT, InfestingLeavesVariant.SPRUCE);
        }
        if (block == Blocks.BIRCH_LEAVES) {
            return fallbackState.setValue(InfestingLeavesVariantBlock.VARIANT, InfestingLeavesVariant.BIRCH);
        }
        if (block == Blocks.JUNGLE_LEAVES) {
            return fallbackState.setValue(InfestingLeavesVariantBlock.VARIANT, InfestingLeavesVariant.JUNGLE);
        }
        if (block == Blocks.ACACIA_LEAVES) {
            return fallbackState.setValue(InfestingLeavesVariantBlock.VARIANT, InfestingLeavesVariant.ACACIA);
        }
        if (block == Blocks.DARK_OAK_LEAVES) {
            return fallbackState.setValue(InfestingLeavesVariantBlock.VARIANT, InfestingLeavesVariant.DARK_OAK);
        }
        if (block == Blocks.MANGROVE_LEAVES) {
            return fallbackState.setValue(InfestingLeavesVariantBlock.VARIANT, InfestingLeavesVariant.MANGROVE);
        }
        if (block == Blocks.AZALEA_LEAVES) {
            return fallbackState.setValue(InfestingLeavesVariantBlock.VARIANT, InfestingLeavesVariant.AZALEA);
        }
        return fallbackState;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfestingLeavesBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModContent.INFESTING_LEAVES_BLOCK_ENTITY.get()
                ? (tickerLevel, tickerPos, tickerState, blockEntity) ->
                        InfestingLeavesBlockEntity.tick(tickerLevel, tickerPos, tickerState, (InfestingLeavesBlockEntity) blockEntity)
                : null;
    }
}
