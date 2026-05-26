package com.example.newexnihilo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class ExNihiloDropData {
    private static final Gson GSON = new Gson();
    private static volatile List<DropRecipe> crushingRecipes;
    private static volatile List<DropRecipe> harvestRecipes;
    private static volatile List<SiftRecipe> siftingRecipes;

    private ExNihiloDropData() {
    }

    public static List<ItemStack> rollHammerDrops(ServerLevel level, BlockState state, RandomSource random) {
        return rollDrops(loadCrushing(level), state, random);
    }

    public static List<ItemStack> rollCrookDrops(ServerLevel level, BlockState state, RandomSource random) {
        return rollDrops(loadHarvest(level), state, random);
    }

    public static boolean canSift(ServerLevel level, BlockState state, MeshType meshType) {
        for (SiftRecipe recipe : loadSifting(level)) {
            if (recipe.input().matches(state) && recipe.hasMesh(meshType)) {
                return true;
            }
        }
        return false;
    }

    public static List<ItemStack> rollSieveDrops(ServerLevel level, BlockState state, MeshType meshType, RandomSource random) {
        List<ItemStack> drops = new ArrayList<>();
        for (SiftRecipe recipe : loadSifting(level)) {
            if (!recipe.input().matches(state)) {
                continue;
            }
            for (SiftRoll roll : recipe.rolls()) {
                if (roll.meshType() == meshType && random.nextFloat() <= roll.chance()) {
                    drops.add(recipe.result().copy());
                }
            }
        }
        return drops;
    }

    private static List<DropRecipe> loadCrushing(ServerLevel level) {
        List<DropRecipe> recipes = crushingRecipes;
        if (recipes == null) {
            recipes = loadRecipes(level, "ex_nihilo/crushing");
            crushingRecipes = recipes;
        }
        return recipes;
    }

    private static List<DropRecipe> loadHarvest(ServerLevel level) {
        List<DropRecipe> recipes = harvestRecipes;
        if (recipes == null) {
            recipes = loadRecipes(level, "ex_nihilo/harvest");
            harvestRecipes = recipes;
        }
        return recipes;
    }

    private static List<SiftRecipe> loadSifting(ServerLevel level) {
        List<SiftRecipe> recipes = siftingRecipes;
        if (recipes == null) {
            recipes = loadSiftingRecipes(level, "ex_nihilo/sifting");
            siftingRecipes = recipes;
        }
        return recipes;
    }

    private static List<DropRecipe> loadRecipes(ServerLevel level, String path) {
        List<DropRecipe> recipes = new ArrayList<>();
        Map<Identifier, Resource> resources = level.getServer().getResourceManager()
                .listResources(path, id -> id.getPath().endsWith(".json"));
        for (Resource resource : resources.values()) {
            try (BufferedReader reader = resource.openAsReader()) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                DropRecipe recipe = parseRecipe(json);
                if (recipe != null) {
                    recipes.add(recipe);
                }
            } catch (RuntimeException | IOException ignored) {
                // A bad datapack entry should not break every block drop.
            }
        }
        return Collections.unmodifiableList(recipes);
    }

    private static List<SiftRecipe> loadSiftingRecipes(ServerLevel level, String path) {
        List<SiftRecipe> recipes = new ArrayList<>();
        Map<Identifier, Resource> resources = level.getServer().getResourceManager()
                .listResources(path, id -> id.getPath().endsWith(".json"));
        for (Resource resource : resources.values()) {
            try (BufferedReader reader = resource.openAsReader()) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                SiftRecipe recipe = parseSiftRecipe(json);
                if (recipe != null) {
                    recipes.add(recipe);
                }
            } catch (RuntimeException | IOException ignored) {
                // A bad datapack entry should not break every sieve.
            }
        }
        return Collections.unmodifiableList(recipes);
    }

    private static DropRecipe parseRecipe(JsonObject json) {
        if (json == null || !json.has("input") || !json.has("results")) {
            return null;
        }
        String input = remapNamespace(json.get("input").getAsString());
        List<DropEntry> drops = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray("results")) {
            JsonObject result = element.getAsJsonObject();
            float chance = result.has("chance") ? result.get("chance").getAsFloat() : 1.0F;
            JsonObject itemJson = result.getAsJsonObject("item");
            ItemStack stack = parseItemStack(itemJson);
            if (!stack.isEmpty()) {
                drops.add(new DropEntry(stack, chance));
            }
        }
        return drops.isEmpty() ? null : new DropRecipe(InputMatcher.create(input), drops);
    }

    private static SiftRecipe parseSiftRecipe(JsonObject json) {
        if (json == null || !json.has("input") || !json.has("result") || !json.has("rolls")) {
            return null;
        }
        String input = remapNamespace(json.get("input").getAsString());
        ItemStack result = parseItemStack(json.getAsJsonObject("result"));
        if (result.isEmpty()) {
            return null;
        }

        List<SiftRoll> rolls = new ArrayList<>();
        JsonArray rollArray = json.getAsJsonArray("rolls");
        for (JsonElement element : rollArray) {
            JsonObject rollJson = element.getAsJsonObject();
            MeshType meshType = MeshType.fromName(rollJson.get("mesh").getAsString());
            if (meshType != MeshType.NONE) {
                rolls.add(new SiftRoll(meshType, rollJson.get("chance").getAsFloat()));
            }
        }
        return rolls.isEmpty() ? null : new SiftRecipe(InputMatcher.create(input), result, rolls);
    }

    private static ItemStack parseItemStack(JsonObject json) {
        if (json == null || !json.has("id")) {
            return ItemStack.EMPTY;
        }
        Identifier id = Identifier.parse(remapNamespace(json.get("id").getAsString()));
        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        int count = json.has("count") ? json.get("count").getAsInt() : 1;
        return new ItemStack(item, count);
    }

    private static List<ItemStack> rollDrops(List<DropRecipe> recipes, BlockState state, RandomSource random) {
        List<ItemStack> drops = new ArrayList<>();
        for (DropRecipe recipe : recipes) {
            if (!recipe.input().matches(state)) {
                continue;
            }
            for (DropEntry entry : recipe.drops()) {
                if (random.nextFloat() <= entry.chance()) {
                    drops.add(entry.stack().copy());
                }
            }
        }
        return drops;
    }

    private static String remapNamespace(String id) {
        return id.replace("exnihilosequentia:", ExampleMod.MODID + ":");
    }

    private record DropRecipe(InputMatcher input, List<DropEntry> drops) {
    }

    private record DropEntry(ItemStack stack, float chance) {
    }

    private record SiftRecipe(InputMatcher input, ItemStack result, List<SiftRoll> rolls) {
        boolean hasMesh(MeshType meshType) {
            for (SiftRoll roll : rolls) {
                if (roll.meshType() == meshType) {
                    return true;
                }
            }
            return false;
        }
    }

    private record SiftRoll(MeshType meshType, float chance) {
    }

    private interface InputMatcher {
        boolean matches(BlockState state);

        static InputMatcher create(String value) {
            if (value.startsWith("#")) {
                TagKey<Block> tag = TagKey.create(Registries.BLOCK, Identifier.parse(value.substring(1)));
                return state -> state.is(tag);
            }
            Identifier id = Identifier.parse(value);
            return state -> BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(id);
        }
    }
}
