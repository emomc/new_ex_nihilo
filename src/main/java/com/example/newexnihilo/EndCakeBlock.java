package com.example.newexnihilo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class EndCakeBlock extends CakeBlock {
    public EndCakeBlock(BlockBehaviour.Properties properties) {
        super(properties);
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
        int bites = state.getValue(BITES);
        if (stack.is(Items.ENDER_EYE) && bites > 0) {
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(pos, state.setValue(BITES, bites - 1));
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.canEat(true) && !player.getAbilities().instabuild) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel) || player.isPassenger() || player.getVehicle() != null) {
            return InteractionResult.FAIL;
        }

        if (!player.getAbilities().instabuild) {
            consumeCake(level, pos, state, player);
        }
        return teleportPlayer(serverLevel, player);
    }

    private static void consumeCake(Level level, BlockPos pos, BlockState state, Player player) {
        player.awardStat(Stats.EAT_CAKE_SLICE);
        player.getFoodData().eat(2, 0.1F);
        int bites = state.getValue(BITES);
        if (bites < 6) {
            level.setBlockAndUpdate(pos, state.setValue(BITES, bites + 1));
        } else {
            level.removeBlock(pos, false);
        }
    }

    private static InteractionResult teleportPlayer(ServerLevel currentLevel, Player player) {
        TeleportTransition transition = getPortalDestination(currentLevel, player);
        if (transition == null) {
            return InteractionResult.FAIL;
        }
        player.teleport(transition);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static TeleportTransition getPortalDestination(ServerLevel currentLevel, Entity entity) {
        ResourceKey<Level> targetKey = currentLevel.dimension() == Level.END ? Level.OVERWORLD : Level.END;
        ServerLevel targetLevel = currentLevel.getServer().getLevel(targetKey);
        if (targetLevel == null) {
            return null;
        }

        boolean toEnd = targetKey == Level.END;
        BlockPos targetPos = toEnd ? ServerLevel.END_SPAWN_POINT : targetLevel.getRespawnData().pos();
        Vec3 target = targetPos.getBottomCenter();
        float yRot = entity.getYRot();
        if (toEnd) {
            EndPlatformFeature.createEndPlatform(targetLevel, BlockPos.containing(target).below(), true);
            yRot = Direction.WEST.toYRot();
            if (entity instanceof ServerPlayer) {
                target = target.subtract(0.0D, 1.0D, 0.0D);
            }
        } else if (entity instanceof ServerPlayer serverPlayer) {
            return serverPlayer.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
        } else {
            target = entity.adjustSpawnLocation(targetLevel, targetPos).getBottomCenter();
        }

        return new TeleportTransition(
                targetLevel,
                target,
                entity.getDeltaMovement(),
                yRot,
                entity.getXRot(),
                TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
    }
}
