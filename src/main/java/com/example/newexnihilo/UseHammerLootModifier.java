package com.example.newexnihilo;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class UseHammerLootModifier extends LootModifier {
    public static final MapCodec<UseHammerLootModifier> CODEC =
            RecordCodecBuilder.mapCodec(instance -> codecStart(instance).apply(instance, UseHammerLootModifier::new));

    public UseHammerLootModifier(LootItemCondition[] conditionsIn, int modifierId) {
        super(conditionsIn, modifierId);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!context.hasParameter(LootContextParams.TOOL) || !context.hasParameter(LootContextParams.BLOCK_STATE)) {
            return generatedLoot;
        }
        ItemInstance tool = context.getParameter(LootContextParams.TOOL);
        BlockState state = context.getParameter(LootContextParams.BLOCK_STATE);
        if (!tool.is(ModTags.HAMMERS)) {
            return generatedLoot;
        }

        List<ItemStack> drops = ExNihiloDropData.rollHammerDrops(context.getLevel(), state, context.getRandom());
        if (drops.isEmpty()) {
            return generatedLoot;
        }

        ObjectArrayList<ItemStack> newLoot = new ObjectArrayList<>();
        newLoot.addAll(drops);
        return newLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
