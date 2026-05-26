package com.example.newexnihilo;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CrucibleBlock extends Block implements EntityBlock {
    public CrucibleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrucibleBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModContent.CRUCIBLE_BLOCK_ENTITY.get()
                ? (tickerLevel, tickerPos, tickerState, blockEntity) ->
                        CrucibleBlockEntity.tick(tickerLevel, tickerPos, tickerState, (CrucibleBlockEntity) blockEntity)
                : null;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (stack.getItem() == Items.BUCKET) {
            String drained = crucible.drainBucket();
            if (!drained.isEmpty()) {
                MachineInteraction.consumeOneAndGive(player, hand, stack, new ItemStack(ExNihiloFluidIds.bucketFor(drained)));
                MachineInteraction.fluidDrain(player, hand, level, pos);
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        String fluid = ExNihiloFluidIds.fluidFromBucket(stack.getItem());
        if (!fluid.isEmpty() && crucible.fillFluid(fluid, 1000)) {
            MachineInteraction.consumeOneAndGive(player, hand, stack, new ItemStack(Items.BUCKET));
            MachineInteraction.fluidFill(player, hand, level, pos);
            return InteractionResult.SUCCESS_SERVER;
        }

        if (level instanceof ServerLevel serverLevel && crucible.tryInsert(stack, serverLevel)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            MachineInteraction.insert(player, hand, level, pos);
            return InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown() && crucible.hasInput()) {
            MachineInteraction.give(player, crucible.removeInput());
            MachineInteraction.extract(player, InteractionHand.MAIN_HAND, level, pos);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }
}
