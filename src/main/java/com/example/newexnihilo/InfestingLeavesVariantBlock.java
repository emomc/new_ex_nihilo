package com.example.newexnihilo;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class InfestingLeavesVariantBlock extends Block {
    public static final EnumProperty<InfestingLeavesVariant> VARIANT =
            EnumProperty.create("variant", InfestingLeavesVariant.class);

    public InfestingLeavesVariantBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(VARIANT, InfestingLeavesVariant.OAK));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }
}
