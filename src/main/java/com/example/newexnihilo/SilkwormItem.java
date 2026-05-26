package com.example.newexnihilo;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public class SilkwormItem extends Item {
    public SilkwormItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (!state.is(BlockTags.LEAVES) || !InfestingLeavesBlock.canInfest(state)) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide()) {
            InfestingLeavesBlock.startInfesting(context.getLevel(), context.getClickedPos());
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
