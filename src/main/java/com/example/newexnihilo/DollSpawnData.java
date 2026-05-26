package com.example.newexnihilo;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

public final class DollSpawnData {
    private static final Map<String, DollSpec> SPECS = Map.of(
            "blaze_doll", new DollSpec("minecraft:blaze", ExNihiloFluidIds.LAVA),
            "enderman_doll", new DollSpec("minecraft:enderman", ExNihiloFluidIds.WITCH_WATER),
            "shulker_doll", new DollSpec("minecraft:shulker", ExNihiloFluidIds.WITCH_WATER),
            "guardian_doll", new DollSpec("minecraft:guardian", ExNihiloFluidIds.SEA_WATER),
            "bee_doll", new DollSpec("minecraft:bee", ExNihiloFluidIds.WITCH_WATER));

    private DollSpawnData() {
    }

    public static DollSpec get(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!id.getNamespace().equals(ExampleMod.MODID)) {
            return null;
        }
        return SPECS.get(id.getPath());
    }

    public static boolean spawn(ServerLevel level, BlockPos pos, String entityId) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(entityId));
        if (type == null) {
            return false;
        }
        Entity entity = type.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (entity == null) {
            return false;
        }
        BlockPos spawnPos = pos.above();
        entity.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
        return level.addFreshEntity(entity);
    }

    public record DollSpec(String entityId, String fluidId) {
    }
}
