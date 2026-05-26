package com.example.newexnihilo;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class UseCrookLootModifier extends LootModifier {
    public static final MapCodec<UseCrookLootModifier> CODEC =
            RecordCodecBuilder.mapCodec(instance -> codecStart(instance).apply(instance, UseCrookLootModifier::new));

    public UseCrookLootModifier(LootItemCondition[] conditionsIn, int modifierId) {
        super(conditionsIn, modifierId);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!context.hasParameter(LootContextParams.TOOL) || !context.hasParameter(LootContextParams.BLOCK_STATE)) {
            return generatedLoot;
        }
        ItemInstance tool = context.getParameter(LootContextParams.TOOL);
        BlockState state = context.getParameter(LootContextParams.BLOCK_STATE);
        if (!tool.is(ModTags.CROOKS)) {
            return generatedLoot;
        }

        ObjectArrayList<ItemStack> newLoot = new ObjectArrayList<>();
        addVanillaDrops(newLoot, context, state);
        List<ItemStack> crookDrops = ExNihiloDropData.rollCrookDrops(context.getLevel(), state, context.getRandom());
        newLoot.addAll(crookDrops);
        return newLoot.isEmpty() ? generatedLoot : newLoot;
    }

    private static void addVanillaDrops(ObjectArrayList<ItemStack> drops, LootContext context, BlockState state) {
        if (!context.hasParameter(LootContextParams.ORIGIN)) {
            return;
        }
        ServerLevel level = context.getLevel();
        Vec3 origin = context.getParameter(LootContextParams.ORIGIN);
        BlockPos pos = BlockPos.containing(origin);
        drops.addAll(Block.getDrops(state, level, pos, null));
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
