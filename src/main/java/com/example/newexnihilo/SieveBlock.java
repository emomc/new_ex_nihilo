package com.example.newexnihilo;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;

public class SieveBlock extends Block implements EntityBlock {
    public SieveBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SieveBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
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
        if (!(level.getBlockEntity(pos) instanceof SieveBlockEntity sieve)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (stack.is(ModTags.MESHES)) {
            if (!sieve.hasMesh()) {
                sieve.setMesh(stack);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                MachineInteraction.insert(player, hand, level, pos);
                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel serverLevel && processNearbySieves(serverLevel, pos, player, hand, stack)) {
            return InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof SieveBlockEntity sieve)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            if (sieve.hasInput()) {
                MachineInteraction.give(player, sieve.removeInput());
                MachineInteraction.extract(player, InteractionHand.MAIN_HAND, level, pos);
                return InteractionResult.SUCCESS_SERVER;
            }
            if (sieve.hasMesh()) {
                giveRemovedStackToPlayer(level, pos, player, sieve.removeMesh());
                MachineInteraction.extract(player, InteractionHand.MAIN_HAND, level, pos);
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        if (sieve.collectOutput(player)) {
            MachineInteraction.extract(player, InteractionHand.MAIN_HAND, level, pos);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }

    private boolean processNearbySieves(ServerLevel level, BlockPos center, Player player, InteractionHand hand, ItemStack stack) {
        boolean changed = processSingleSieve(level, center, player, hand, stack);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }
                if (stack.isEmpty() && !player.getAbilities().instabuild) {
                    return changed;
                }
                BlockPos current = center.offset(x, 0, z);
                changed |= processSingleSieve(level, current, player, hand, stack);
            }
        }
        return changed;
    }

    private boolean processSingleSieve(ServerLevel level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        if (!(level.getBlockEntity(pos) instanceof SieveBlockEntity sieve)) {
            return false;
        }
        if (sieve.hasInput()) {
            return sieve.advanceAndFinish(level);
        }
        if (!stack.isEmpty() && sieve.insertInput(stack, player)) {
            MachineInteraction.insert(player, hand, level, pos);
            return true;
        }
        return false;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SieveBlockEntity sieve) {
            if (sieve.hasMesh()) {
                Block.popResource(level, pos, sieve.removeMesh());
            }
            if (sieve.hasInput()) {
                Block.popResource(level, pos, sieve.removeInput());
            }
            if (!sieve.getOutput().isEmpty()) {
                Block.popResource(level, pos, sieve.extractOutput(sieve.getOutput().getCount()));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private static void giveRemovedStackToPlayer(Level level, BlockPos pos, Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            return;
        }
        if (player.getOffhandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.OFF_HAND, stack);
            return;
        }
        if (!player.addItem(stack)) {
            Block.popResource(level, pos, stack);
        }
    }
}
