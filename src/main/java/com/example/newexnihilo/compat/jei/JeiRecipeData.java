package com.example.newexnihilo.compat.jei;

import com.example.newexnihilo.ExNihiloFluidIds;
import com.example.newexnihilo.ExampleMod;
import com.example.newexnihilo.MeshType;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import org.slf4j.Logger;

final class JeiRecipeData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    private JeiRecipeData() {
    }

    static List<JeiRecipes.Sifting> sifting() {
        List<JeiRecipes.Sifting> recipes = new ArrayList<>();
        load("ex_nihilo/sifting", json -> {
            if (!has(json, "input", "result", "rolls")) {
                return;
            }
            List<ItemStack> input = stacks(json.get("input").getAsString());
            ItemStack result = stack(json.getAsJsonObject("result"));
            if (input.isEmpty() || result.isEmpty()) {
                return;
            }
            for (JsonElement element : json.getAsJsonArray("rolls")) {
                JsonObject roll = element.getAsJsonObject();
                MeshType mesh = MeshType.fromName(roll.get("mesh").getAsString());
                ItemStack meshStack = item(ExampleMod.MODID + ":" + mesh.getMeshName());
                if (mesh != MeshType.NONE && !meshStack.isEmpty()) {
                    recipes.add(new JeiRecipes.Sifting(input, meshStack, result, roll.get("chance").getAsFloat()));
                }
            }
        });
        return recipes;
    }

    static List<JeiRecipes.Drop> crushing() {
        return drops("ex_nihilo/crushing");
    }

    static List<JeiRecipes.Drop> harvest() {
        return drops("ex_nihilo/harvest");
    }

    static List<JeiRecipes.Compost> compost() {
        List<JeiRecipes.Compost> recipes = new ArrayList<>();
        load("ex_nihilo/compost", json -> {
            if (!has(json, "input", "amount")) {
                return;
            }
            List<ItemStack> input = stacks(json.get("input").getAsString());
            if (!input.isEmpty()) {
                recipes.add(new JeiRecipes.Compost(input, json.get("amount").getAsInt()));
            }
        });
        return recipes;
    }

    static List<JeiRecipes.Melting> melting() {
        List<JeiRecipes.Melting> recipes = new ArrayList<>();
        load("ex_nihilo/melting", json -> {
            if (!has(json, "input", "crucibleType", "fluidResult")) {
                return;
            }
            JsonObject result = json.getAsJsonObject("fluidResult");
            FluidStack fluid = fluid(result);
            List<ItemStack> input = stacks(json.get("input").getAsString());
            if (!input.isEmpty() && !fluid.isEmpty()) {
                recipes.add(new JeiRecipes.Melting(input, json.get("crucibleType").getAsString(), fluid));
            }
        });
        return recipes;
    }

    static List<JeiRecipes.Heat> heat() {
        List<JeiRecipes.Heat> recipes = new ArrayList<>();
        load("ex_nihilo/heat", json -> {
            if (!has(json, "block", "amount")) {
                return;
            }
            List<ItemStack> block = stacks(json.get("block").getAsString());
            if (!block.isEmpty()) {
                recipes.add(new JeiRecipes.Heat(block, json.get("amount").getAsInt()));
            }
        });
        return recipes;
    }

    static List<JeiRecipes.Transition> transition() {
        List<JeiRecipes.Transition> recipes = new ArrayList<>();
        load("ex_nihilo/transition", json -> {
            if (!has(json, "catalyst", "fluidInTank", "result")) {
                return;
            }
            FluidStack input = fluid(json.getAsJsonObject("fluidInTank"));
            FluidStack output = fluid(json.getAsJsonObject("result"));
            List<ItemStack> catalyst = stacks(json.get("catalyst").getAsString());
            if (!input.isEmpty() && !output.isEmpty() && !catalyst.isEmpty()) {
                recipes.add(new JeiRecipes.Transition(input, catalyst, output));
            }
        });
        return recipes;
    }

    static List<JeiRecipes.Solidify> solidify() {
        List<JeiRecipes.Solidify> recipes = new ArrayList<>();
        load("ex_nihilo/solidify", json -> {
            if (!has(json, "fluidInTank", "fluidOnTop", "result")) {
                return;
            }
            FluidStack tank = fluid(json.getAsJsonObject("fluidInTank"));
            FluidStack top = fluid(json.getAsJsonObject("fluidOnTop"));
            ItemStack result = stack(json.getAsJsonObject("result"));
            if (!tank.isEmpty() && !top.isEmpty() && !result.isEmpty()) {
                recipes.add(new JeiRecipes.Solidify(tank, top, result));
            }
        });
        return recipes;
    }

    static List<JeiRecipes.Precipitate> precipitate() {
        List<JeiRecipes.Precipitate> recipes = new ArrayList<>();
        load("ex_nihilo/precipitate", json -> {
            if (!has(json, "fluid", "input", "result")) {
                return;
            }
            FluidStack fluid = fluid(json.getAsJsonObject("fluid"));
            List<ItemStack> input = stacks(json.get("input").getAsString());
            ItemStack result = stack(json.getAsJsonObject("result"));
            if (!fluid.isEmpty() && !input.isEmpty() && !result.isEmpty()) {
                recipes.add(new JeiRecipes.Precipitate(fluid, input, result));
            }
        });
        return recipes;
    }

    static List<JeiRecipes.DollSpawn> dollSpawns() {
        return List.of(
                dollSpawn("blaze_doll", ExNihiloFluidIds.LAVA, Items.BLAZE_SPAWN_EGG, "entity.minecraft.blaze"),
                dollSpawn("enderman_doll", ExNihiloFluidIds.WITCH_WATER, Items.ENDERMAN_SPAWN_EGG, "entity.minecraft.enderman"),
                dollSpawn("shulker_doll", ExNihiloFluidIds.WITCH_WATER, Items.SHULKER_SPAWN_EGG, "entity.minecraft.shulker"),
                dollSpawn("guardian_doll", ExNihiloFluidIds.SEA_WATER, Items.GUARDIAN_SPAWN_EGG, "entity.minecraft.guardian"),
                dollSpawn("bee_doll", ExNihiloFluidIds.WITCH_WATER, Items.BEE_SPAWN_EGG, "entity.minecraft.bee"));
    }

    static List<JeiRecipes.SilkwormInfesting> silkwormInfesting() {
        ItemStack silkworm = item(ExampleMod.MODID + ":silkworm");
        ItemStack infestingLeaves = item(ExampleMod.MODID + ":infesting_leaves");
        ItemStack infestedLeaves = item(ExampleMod.MODID + ":infested_leaves");
        List<ItemStack> leaves = stacks("#minecraft:leaves");
        if (silkworm.isEmpty() || infestingLeaves.isEmpty() || infestedLeaves.isEmpty() || leaves.isEmpty()) {
            return List.of();
        }
        return List.of(new JeiRecipes.SilkwormInfesting(leaves, silkworm, infestingLeaves, infestedLeaves));
    }

    private static List<JeiRecipes.Drop> drops(String path) {
        List<JeiRecipes.Drop> recipes = new ArrayList<>();
        load(path, json -> {
            if (!has(json, "input", "results")) {
                return;
            }
            List<ItemStack> input = stacks(json.get("input").getAsString());
            List<JeiRecipes.OutputChance> outputs = new ArrayList<>();
            for (JsonElement element : json.getAsJsonArray("results")) {
                JsonObject result = element.getAsJsonObject();
                ItemStack stack = stack(result.getAsJsonObject("item"));
                if (!stack.isEmpty()) {
                    float chance = result.has("chance") ? result.get("chance").getAsFloat() : 1.0F;
                    outputs.add(new JeiRecipes.OutputChance(stack, chance));
                }
            }
            if (!input.isEmpty() && !outputs.isEmpty()) {
                recipes.add(new JeiRecipes.Drop(input, outputs));
            }
        });
        return recipes;
    }

    private static void load(String path, Consumer<JsonObject> consumer) {
        int count = loadFromServerResources(path, consumer);
        if (count == 0) {
            count = loadFromModFiles(path, consumer);
        }
        if (count == 0) {
            count = loadFromClasspath(path, consumer);
        }
        if (count == 0) {
            LOGGER.warn("No New Ex Nihilo JEI data found for {}", path);
        }
    }

    private static int loadFromServerResources(String path, Consumer<JsonObject> consumer) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            return 0;
        }
        Map<Identifier, Resource> resources = server.getResourceManager()
                .listResources(path, id -> id.getPath().endsWith(".json"));
        int count = 0;
        for (Resource resource : resources.values()) {
            try (BufferedReader reader = resource.openAsReader()) {
                consumer.accept(GSON.fromJson(reader, JsonObject.class));
                count++;
            } catch (RuntimeException | IOException exception) {
                LOGGER.warn("Skipping malformed New Ex Nihilo JEI data entry in {}", path, exception);
            }
        }
        return count;
    }

    private static int loadFromModFiles(String path, Consumer<JsonObject> consumer) {
        String suffix = "/" + path + "/";
        Set<String> loaded = new HashSet<>();
        final int[] count = {0};
        ModList.get().forEachModFile(modFile -> modFile.getContents().visitContent((name, resource) -> {
            String normalized = name.replace('\\', '/');
            while (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
            if (!normalized.startsWith("data/")
                    || !normalized.endsWith(".json")
                    || !normalized.contains(suffix)
                    || !loaded.add(normalized)) {
                return;
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(modFile.getContents().openFile(normalized), StandardCharsets.UTF_8))) {
                consumer.accept(GSON.fromJson(reader, JsonObject.class));
                count[0]++;
            } catch (RuntimeException | IOException exception) {
                LOGGER.warn("Skipping malformed New Ex Nihilo JEI data entry {}", normalized, exception);
            }
        }));
        return count[0];
    }

    private static int loadFromClasspath(String path, Consumer<JsonObject> consumer) {
        String prefix = "data/" + ExampleMod.MODID + "/" + path + "/";
        Set<String> loaded = new HashSet<>();
        final int[] count = {0};
        ClassLoader loader = JeiRecipeData.class.getClassLoader();
        ModList.get().forEachModFile(modFile -> modFile.getContents().visitContent((name, resource) -> {
            String normalized = name.replace('\\', '/');
            while (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
            if (!normalized.startsWith(prefix) || !normalized.endsWith(".json") || !loaded.add(normalized)) {
                return;
            }
            try (InputStream stream = loader.getResourceAsStream(normalized)) {
                if (stream == null) {
                    return;
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    consumer.accept(GSON.fromJson(reader, JsonObject.class));
                    count[0]++;
                }
            } catch (RuntimeException | IOException exception) {
                LOGGER.warn("Skipping malformed New Ex Nihilo JEI classpath data entry {}", normalized, exception);
            }
        }));
        return count[0];
    }

    private static boolean has(JsonObject json, String... keys) {
        if (json == null) {
            return false;
        }
        for (String key : keys) {
            if (!json.has(key)) {
                return false;
            }
        }
        return true;
    }

    private static FluidStack fluid(JsonObject json) {
        if (json == null || !json.has("id")) {
            return FluidStack.EMPTY;
        }
        Fluid fluid = ExNihiloFluidIds.fluidFor(ExNihiloFluidIds.remap(json.get("id").getAsString()));
        int amount = json.has("amount") ? json.get("amount").getAsInt() : 1000;
        return fluid == null || amount <= 0 ? FluidStack.EMPTY : new FluidStack(fluid, amount);
    }

    private static FluidStack fluid(String fluidId, int amount) {
        Fluid fluid = ExNihiloFluidIds.fluidFor(fluidId);
        return fluid == null || amount <= 0 ? FluidStack.EMPTY : new FluidStack(fluid, amount);
    }

    private static JeiRecipes.DollSpawn dollSpawn(String dollId, String fluidId, Item output, String entityDescriptionId) {
        return new JeiRecipes.DollSpawn(
                item(ExampleMod.MODID + ":" + dollId),
                fluid(fluidId, 1000),
                new ItemStack(output),
                entityDescriptionId);
    }

    private static ItemStack stack(JsonObject json) {
        if (json == null || !json.has("id")) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = item(ExNihiloFluidIds.remap(json.get("id").getAsString()));
        if (!stack.isEmpty() && json.has("count")) {
            stack.setCount(json.get("count").getAsInt());
        }
        return stack;
    }

    private static ItemStack item(String rawId) {
        Identifier id = Identifier.parse(ExNihiloFluidIds.remap(rawId));
        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item != null && item != Items.AIR) {
            return new ItemStack(item);
        }
        Block block = BuiltInRegistries.BLOCK.getValue(id);
        if (block != null && block.asItem() != Items.AIR) {
            return new ItemStack(block);
        }
        return ItemStack.EMPTY;
    }

    private static List<ItemStack> stacks(String rawValue) {
        String value = ExNihiloFluidIds.remap(rawValue);
        value = normalizeTagAlias(value);
        if (!value.startsWith("#")) {
            ItemStack stack = item(value);
            return stack.isEmpty() ? List.of() : List.of(stack);
        }

        Identifier tagId = Identifier.parse(value.substring(1));
        List<ItemStack> stacks = new ArrayList<>();
        TagKey<Item> itemTag = TagKey.create(Registries.ITEM, tagId);
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(itemTag)) {
            Item item = holder.value();
            if (item != Items.AIR) {
                stacks.add(new ItemStack(item));
            }
        }
        if (stacks.isEmpty()) {
            TagKey<Block> blockTag = TagKey.create(Registries.BLOCK, tagId);
            for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(blockTag)) {
                Item item = holder.value().asItem();
                if (item != Items.AIR) {
                    stacks.add(new ItemStack(item));
                }
            }
        }
        return stacks.isEmpty() ? List.of() : Collections.unmodifiableList(stacks);
    }

    private static String normalizeTagAlias(String value) {
        return switch (value) {
            case "#minecraft:sand" -> "#c:sands";
            case "#c:crops/beetroot" -> "minecraft:beetroot";
            case "#c:crops/carrot" -> "minecraft:carrot";
            case "#c:crops/nether_wart" -> "minecraft:nether_wart";
            case "#c:crops/potato" -> "minecraft:potato";
            case "#c:crops/wheat" -> "minecraft:wheat";
            case "#c:seeds" -> "minecraft:wheat_seeds";
            case "#c:eggs" -> "minecraft:egg";
            default -> value;
        };
    }
}
