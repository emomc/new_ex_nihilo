package com.example.newexnihilo;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockTransformItem extends Item {
    private final Map<Block, Block> transforms;

    public BlockTransformItem(Properties properties, Map<Block, Block> transforms) {
        super(properties);
        this.transforms = transforms;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block target = transforms.get(state.getBlock());
        if (target == null) {
            return InteractionResult.PASS;
        }

        BlockState targetState = target.defaultBlockState();
        if (!targetState.canSurvive(level, pos)) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (!level.isClientSide()) {
            level.setBlock(pos, targetState, Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 0.75F, 1.0F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5D,
                        pos.getY() + 1.0D,
                        pos.getZ() + 0.5D,
                        8,
                        0.25D,
                        0.12D,
                        0.25D,
                        0.02D);
            }
            if (player != null && !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        if (player != null) {
            player.swing(context.getHand(), true);
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
