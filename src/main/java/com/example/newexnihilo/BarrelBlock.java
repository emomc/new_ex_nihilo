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

public class BarrelBlock extends Block implements EntityBlock {
    public BarrelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BarrelBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModContent.BARREL_BLOCK_ENTITY.get()
                ? (tickerLevel, tickerPos, tickerState, blockEntity) ->
                        BarrelBlockEntity.tick(tickerLevel, tickerPos, tickerState, (BarrelBlockEntity) blockEntity)
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
        if (!(level.getBlockEntity(pos) instanceof BarrelBlockEntity barrel)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (stack.getItem() == Items.BUCKET) {
            String drained = barrel.drainBucket();
            if (!drained.isEmpty()) {
                MachineInteraction.consumeOneAndGive(player, hand, stack, new ItemStack(ExNihiloFluidIds.bucketFor(drained)));
                MachineInteraction.fluidDrain(player, hand, level, pos);
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        String fluid = ExNihiloFluidIds.fluidFromBucket(stack.getItem());
        if (!fluid.isEmpty() && barrel.fillFluid(fluid, 1000)) {
            MachineInteraction.consumeOneAndGive(player, hand, stack, new ItemStack(Items.BUCKET));
            MachineInteraction.fluidFill(player, hand, level, pos);
            return InteractionResult.SUCCESS_SERVER;
        }

        if (level instanceof ServerLevel serverLevel && barrel.hasFluid()) {
            DollSpawnData.DollSpec dollSpec = DollSpawnData.get(stack);
            if (dollSpec != null && dollSpec.fluidId().equals(barrel.getFluidId())
                    && barrel.canStartDollSpawn() && barrel.startDollSpawn(dollSpec.entityId())) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                MachineInteraction.complete(level, pos);
                player.swing(hand, true);
                return InteractionResult.SUCCESS_SERVER;
            }

            ItemStack precipitate = ExNihiloMachineData.getPrecipitateResult(
                    serverLevel, barrel.getFluidId(), barrel.getFluidAmount(), stack);
            if (!precipitate.isEmpty() && barrel.consumeFluid(1000)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                MachineInteraction.give(player, precipitate);
                MachineInteraction.extract(player, hand, level, pos);
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        if (level instanceof ServerLevel serverLevel && barrel.canCompost()) {
            int amount = ExNihiloMachineData.getCompostAmount(serverLevel, stack);
            if (amount > 0) {
                barrel.addCompost(amount);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                MachineInteraction.compost(player, hand, level, pos, barrel.hasCompleteCompost());
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BarrelBlockEntity barrel)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack output = barrel.getExtractableOutput();
        if (!output.isEmpty()) {
            MachineInteraction.give(player, barrel.extractOutput(output.getCount()));
            MachineInteraction.extract(player, InteractionHand.MAIN_HAND, level, pos);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }
}
