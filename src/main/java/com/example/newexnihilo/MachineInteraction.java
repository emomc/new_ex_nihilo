package com.example.newexnihilo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

final class MachineInteraction {
    private MachineInteraction() {
    }

    static void consumeOneAndGive(Player player, InteractionHand hand, ItemStack input, ItemStack output) {
        if (!player.getAbilities().instabuild) {
            ItemStack result = output.copy();
            if (input.getCount() == 1) {
                player.setItemInHand(hand, result);
            } else {
                input.shrink(1);
                give(player, result);
            }
        }
    }

    static void use(Player player, InteractionHand hand, Level level, BlockPos pos, SoundEvent sound, float volume, float pitch) {
        player.swing(hand, true);
        level.playSound(null, pos, sound, SoundSource.BLOCKS, volume, pitch);
    }

    static void insert(Player player, InteractionHand hand, Level level, BlockPos pos) {
        use(player, hand, level, pos, SoundEvents.ITEM_PICKUP, 0.45F, 1.25F);
    }

    static void extract(Player player, InteractionHand hand, Level level, BlockPos pos) {
        use(player, hand, level, pos, SoundEvents.ITEM_PICKUP, 0.55F, 0.95F);
        sparkle(level, pos, 4);
    }

    static void fluidFill(Player player, InteractionHand hand, Level level, BlockPos pos) {
        use(player, hand, level, pos, SoundEvents.BUCKET_EMPTY, 0.75F, 1.0F);
        sparkle(level, pos, 3);
    }

    static void fluidDrain(Player player, InteractionHand hand, Level level, BlockPos pos) {
        use(player, hand, level, pos, SoundEvents.BUCKET_FILL, 0.75F, 1.0F);
        sparkle(level, pos, 3);
    }

    static void compost(Player player, InteractionHand hand, Level level, BlockPos pos, boolean full) {
        use(player, hand, level, pos, full ? SoundEvents.COMPOSTER_FILL_SUCCESS : SoundEvents.COMPOSTER_FILL,
                0.65F, full ? 1.0F : 0.9F);
        if (full) {
            sparkle(level, pos, 8);
        }
    }

    static void complete(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.55F, 1.15F);
        sparkle(level, pos, 10);
    }

    static void give(Player player, ItemStack output) {
        if (!output.isEmpty() && !player.addItem(output)) {
            player.drop(output, false);
        }
    }

    private static void sparkle(Level level, BlockPos pos, int count) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.78D,
                    pos.getZ() + 0.5D,
                    count,
                    0.22D,
                    0.18D,
                    0.22D,
                    0.02D);
        }
    }
}
