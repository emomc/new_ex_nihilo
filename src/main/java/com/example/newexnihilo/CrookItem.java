package com.example.newexnihilo;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class CrookItem extends Item {
    public CrookItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.is(ModTags.MINEABLE_WITH_CROOK) || super.isCorrectToolForDrops(stack, state);
    }
}
