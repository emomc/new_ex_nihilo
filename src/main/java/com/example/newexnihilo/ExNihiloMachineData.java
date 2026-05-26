package com.example.newexnihilo;

import com.google.gson.Gson;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class ExNihiloMachineData {
    private static final Gson GSON = new Gson();
    private static volatile List<CompostRecipe> compostRecipes;
    private static volatile List<TransitionRecipe> transitionRecipes;
    private static volatile List<MeltingRecipe> meltingRecipes;
    private static volatile List<HeatRecipe> heatRecipes;
    private static volatile List<SolidifyRecipe> solidifyRecipes;
    private static volatile List<PrecipitateRecipe> precipitateRecipes;

    private ExNihiloMachineData() {
    }

    public static int getCompostAmount(ServerLevel level, ItemStack stack) {
        for (CompostRecipe recipe : loadCompost(level)) {
            if (recipe.input().matches(stack)) {
                return recipe.amount();
            }
        }
        return 0;
    }

    public static String getTransitionResult(ServerLevel level, String fluidId, int amount, BlockState catalyst) {
        TransitionResult result = getTransition(level, fluidId, amount, catalyst);
        return result == null ? "" : result.resultFluid();
    }

    public static TransitionResult getTransition(ServerLevel level, String fluidId, int amount, BlockState catalyst) {
        for (TransitionRecipe recipe : loadTransition(level)) {
            if (recipe.inputFluid().equals(fluidId) && amount >= recipe.amount() && recipe.catalyst().matches(catalyst)) {
                return new TransitionResult(recipe.resultFluid(), recipe.amount());
            }
        }
        return null;
    }

    public static MeltingResult getMeltingResult(ServerLevel level, ItemStack stack, String crucibleType) {
        for (MeltingRecipe recipe : loadMelting(level)) {
            if (recipe.crucibleType().equals(crucibleType) && recipe.input().matches(stack)) {
                return new MeltingResult(recipe.resultFluid(), recipe.amount());
            }
        }
        return null;
    }

    public static int getHeat(ServerLevel level, BlockState state) {
        int heat = 0;
        for (HeatRecipe recipe : loadHeat(level)) {
            if (recipe.block().matches(state)) {
                heat = Math.max(heat, recipe.amount());
            }
        }
        return heat;
    }

    public static ItemStack getSolidifyResult(ServerLevel level, String fluidInTank, int amountInTank, String fluidOnTop) {
        for (SolidifyRecipe recipe : loadSolidify(level)) {
            if (recipe.fluidInTank().equals(fluidInTank)
                    && recipe.fluidOnTop().equals(fluidOnTop)
                    && amountInTank >= recipe.tankAmount()) {
                return recipe.result().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getPrecipitateResult(ServerLevel level, String fluidId, int amount, ItemStack input) {
        for (PrecipitateRecipe recipe : loadPrecipitate(level)) {
            if (recipe.fluid().equals(fluidId) && amount >= recipe.amount() && recipe.input().matches(input)) {
                return recipe.result().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private static List<CompostRecipe> loadCompost(ServerLevel level) {
        List<CompostRecipe> recipes = compostRecipes;
        if (recipes == null) {
            recipes = load(level, "ex_nihilo/compost", ExNihiloMachineData::parseCompost);
            compostRecipes = recipes;
        }
        return recipes;
    }

    private static List<TransitionRecipe> loadTransition(ServerLevel level) {
        List<TransitionRecipe> recipes = transitionRecipes;
        if (recipes == null) {
            recipes = load(level, "ex_nihilo/transition", ExNihiloMachineData::parseTransition);
            transitionRecipes = recipes;
        }
        return recipes;
    }

    private static List<MeltingRecipe> loadMelting(ServerLevel level) {
        List<MeltingRecipe> recipes = meltingRecipes;
        if (recipes == null) {
            recipes = load(level, "ex_nihilo/melting", ExNihiloMachineData::parseMelting);
            meltingRecipes = recipes;
        }
        return recipes;
    }

    private static List<HeatRecipe> loadHeat(ServerLevel level) {
        List<HeatRecipe> recipes = heatRecipes;
        if (recipes == null) {
            recipes = load(level, "ex_nihilo/heat", ExNihiloMachineData::parseHeat);
            heatRecipes = recipes;
        }
        return recipes;
    }

    private static List<SolidifyRecipe> loadSolidify(ServerLevel level) {
        List<SolidifyRecipe> recipes = solidifyRecipes;
        if (recipes == null) {
            recipes = load(level, "ex_nihilo/solidify", ExNihiloMachineData::parseSolidify);
            solidifyRecipes = recipes;
        }
        return recipes;
    }

    private static List<PrecipitateRecipe> loadPrecipitate(ServerLevel level) {
        List<PrecipitateRecipe> recipes = precipitateRecipes;
        if (recipes == null) {
            recipes = load(level, "ex_nihilo/precipitate", ExNihiloMachineData::parsePrecipitate);
            precipitateRecipes = recipes;
        }
        return recipes;
    }

    private static <T> List<T> load(ServerLevel level, String path, Parser<T> parser) {
        List<T> recipes = new ArrayList<>();
        Map<Identifier, Resource> resources = level.getServer().getResourceManager()
                .listResources(path, id -> id.getPath().endsWith(".json"));
        for (Resource resource : resources.values()) {
            try (BufferedReader reader = resource.openAsReader()) {
                T recipe = parser.parse(GSON.fromJson(reader, JsonObject.class));
                if (recipe != null) {
                    recipes.add(recipe);
                }
            } catch (RuntimeException | IOException ignored) {
                // A malformed datapack entry should not break the machine.
            }
        }
        return Collections.unmodifiableList(recipes);
    }

    private static CompostRecipe parseCompost(JsonObject json) {
        if (json == null || !json.has("input") || !json.has("amount")) {
            return null;
        }
        return new CompostRecipe(ItemInputMatcher.create(json.get("input").getAsString()), json.get("amount").getAsInt());
    }

    private static TransitionRecipe parseTransition(JsonObject json) {
        if (json == null || !json.has("catalyst") || !json.has("fluidInTank") || !json.has("result")) {
            return null;
        }
        JsonObject input = json.getAsJsonObject("fluidInTank");
        JsonObject result = json.getAsJsonObject("result");
        return new TransitionRecipe(
                BlockInputMatcher.create(json.get("catalyst").getAsString()),
                ExNihiloFluidIds.remap(input.get("id").getAsString()),
                input.get("amount").getAsInt(),
                ExNihiloFluidIds.remap(result.get("id").getAsString()));
    }

    private static MeltingRecipe parseMelting(JsonObject json) {
        if (json == null || !json.has("input") || !json.has("crucibleType") || !json.has("fluidResult")) {
            return null;
        }
        JsonObject result = json.getAsJsonObject("fluidResult");
        String fluid = ExNihiloFluidIds.remap(result.get("id").getAsString());
        if (!ExNihiloFluidIds.isSupportedFluid(fluid)) {
            return null;
        }
        return new MeltingRecipe(
                ItemInputMatcher.create(json.get("input").getAsString()),
                json.get("crucibleType").getAsString(),
                fluid,
                result.get("amount").getAsInt());
    }

    private static HeatRecipe parseHeat(JsonObject json) {
        if (json == null || !json.has("block") || !json.has("amount")) {
            return null;
        }
        return new HeatRecipe(BlockInputMatcher.create(json.get("block").getAsString()), json.get("amount").getAsInt());
    }

    private static SolidifyRecipe parseSolidify(JsonObject json) {
        if (json == null || !json.has("fluidInTank") || !json.has("fluidOnTop") || !json.has("result")) {
            return null;
        }
        JsonObject tank = json.getAsJsonObject("fluidInTank");
        JsonObject top = json.getAsJsonObject("fluidOnTop");
        ItemStack result = parseItemStack(json.getAsJsonObject("result"));
        if (result.isEmpty()) {
            return null;
        }
        return new SolidifyRecipe(
                ExNihiloFluidIds.remap(tank.get("id").getAsString()),
                tank.get("amount").getAsInt(),
                ExNihiloFluidIds.remap(top.get("id").getAsString()),
                top.get("amount").getAsInt(),
                result);
    }

    private static PrecipitateRecipe parsePrecipitate(JsonObject json) {
        if (json == null || !json.has("fluid") || !json.has("input") || !json.has("result")) {
            return null;
        }
        JsonObject fluid = json.getAsJsonObject("fluid");
        ItemStack result = parseItemStack(json.getAsJsonObject("result"));
        if (result.isEmpty()) {
            return null;
        }
        return new PrecipitateRecipe(
                ExNihiloFluidIds.remap(fluid.get("id").getAsString()),
                fluid.get("amount").getAsInt(),
                ItemInputMatcher.create(json.get("input").getAsString()),
                result);
    }

    private static ItemStack parseItemStack(JsonObject json) {
        if (json == null || !json.has("id")) {
            return ItemStack.EMPTY;
        }
        Identifier id = Identifier.parse(ExNihiloFluidIds.remap(json.get("id").getAsString()));
        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        int count = json.has("count") ? json.get("count").getAsInt() : 1;
        return new ItemStack(item, count);
    }

    public record MeltingResult(String fluidId, int amount) {
    }

    public record TransitionResult(String resultFluid, int amount) {
    }

    private record CompostRecipe(ItemInputMatcher input, int amount) {
    }

    private record TransitionRecipe(BlockInputMatcher catalyst, String inputFluid, int amount, String resultFluid) {
    }

    private record MeltingRecipe(ItemInputMatcher input, String crucibleType, String resultFluid, int amount) {
    }

    private record HeatRecipe(BlockInputMatcher block, int amount) {
    }

    private record SolidifyRecipe(String fluidInTank, int tankAmount, String fluidOnTop, int topAmount, ItemStack result) {
    }

    private record PrecipitateRecipe(String fluid, int amount, ItemInputMatcher input, ItemStack result) {
    }

    private interface Parser<T> {
        T parse(JsonObject json);
    }

    private interface ItemInputMatcher {
        boolean matches(ItemStack stack);

        static ItemInputMatcher create(String value) {
            String remapped = ExNihiloFluidIds.remap(value);
            if (remapped.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(remapped.substring(1)));
                return stack -> stack.is(tag);
            }
            Identifier id = Identifier.parse(remapped);
            return stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(id);
        }
    }

    private interface BlockInputMatcher {
        boolean matches(BlockState state);

        static BlockInputMatcher create(String value) {
            String remapped = ExNihiloFluidIds.remap(value);
            if (remapped.startsWith("#")) {
                TagKey<Block> tag = TagKey.create(Registries.BLOCK, Identifier.parse(remapped.substring(1)));
                return state -> state.is(tag);
            }
            Identifier id = Identifier.parse(remapped);
            return state -> BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(id);
        }
    }
}
