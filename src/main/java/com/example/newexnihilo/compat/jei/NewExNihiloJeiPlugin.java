package com.example.newexnihilo.compat.jei;

import com.example.newexnihilo.ExampleMod;
import com.example.newexnihilo.ExNihiloFluidIds;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import org.slf4j.Logger;

@JeiPlugin
public final class NewExNihiloJeiPlugin implements IModPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int WIDTH = 140;
    private static final int HEIGHT = 72;
    private static final List<String> WOOD_BARRELS = List.of(
            "acacia_barrel",
            "bamboo_barrel",
            "birch_barrel",
            "cherry_barrel",
            "dark_oak_barrel",
            "jungle_barrel",
            "mangrove_barrel",
            "oak_barrel",
            "spruce_barrel",
            "crimson_barrel",
            "warped_barrel");
    private static final List<String> WOOD_CRUCIBLES = List.of(
            "acacia_crucible",
            "bamboo_crucible",
            "birch_crucible",
            "cherry_crucible",
            "dark_oak_crucible",
            "jungle_crucible",
            "mangrove_crucible",
            "oak_crucible",
            "spruce_crucible",
            "crimson_crucible",
            "warped_crucible");
    private static final List<String> SIEVES = List.of(
            "acacia_sieve",
            "bamboo_sieve",
            "birch_sieve",
            "cherry_sieve",
            "dark_oak_sieve",
            "jungle_sieve",
            "mangrove_sieve",
            "oak_sieve",
            "spruce_sieve",
            "crimson_sieve",
            "warped_sieve");
    private static final List<String> MESHES = List.of(
            "string_mesh",
            "flint_mesh",
            "iron_mesh",
            "diamond_mesh",
            "emerald_mesh",
            "netherite_mesh");
    private static final List<String> DOLLS = List.of(
            "blaze_doll",
            "enderman_doll",
            "shulker_doll",
            "guardian_doll",
            "bee_doll");

    private static final RecipeType<JeiRecipes.Sifting> SIFTING =
            RecipeType.create(ExampleMod.MODID, "sifting", JeiRecipes.Sifting.class);
    private static final RecipeType<JeiRecipes.Drop> CRUSHING =
            RecipeType.create(ExampleMod.MODID, "crushing", JeiRecipes.Drop.class);
    private static final RecipeType<JeiRecipes.Drop> HARVEST =
            RecipeType.create(ExampleMod.MODID, "harvest", JeiRecipes.Drop.class);
    private static final RecipeType<JeiRecipes.Compost> COMPOST =
            RecipeType.create(ExampleMod.MODID, "compost", JeiRecipes.Compost.class);
    private static final RecipeType<JeiRecipes.Melting> MELTING =
            RecipeType.create(ExampleMod.MODID, "melting", JeiRecipes.Melting.class);
    private static final RecipeType<JeiRecipes.Heat> HEAT =
            RecipeType.create(ExampleMod.MODID, "heat", JeiRecipes.Heat.class);
    private static final RecipeType<JeiRecipes.Transition> TRANSITION =
            RecipeType.create(ExampleMod.MODID, "transition", JeiRecipes.Transition.class);
    private static final RecipeType<JeiRecipes.Solidify> SOLIDIFY =
            RecipeType.create(ExampleMod.MODID, "solidify", JeiRecipes.Solidify.class);
    private static final RecipeType<JeiRecipes.Precipitate> PRECIPITATE =
            RecipeType.create(ExampleMod.MODID, "precipitate", JeiRecipes.Precipitate.class);
    private static final RecipeType<JeiRecipes.DollSpawn> DOLL_SPAWN =
            RecipeType.create(ExampleMod.MODID, "doll_spawn", JeiRecipes.DollSpawn.class);
    private static final RecipeType<JeiRecipes.SilkwormInfesting> SILKWORM_INFESTING =
            RecipeType.create(ExampleMod.MODID, "silkworm_infesting", JeiRecipes.SilkwormInfesting.class);

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(ExampleMod.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new SiftingCategory(gui),
                new DropCategory(gui, CRUSHING, "jei.new_ex_nihilo.category.crushing", stack("wooden_hammer")),
                new DropCategory(gui, HARVEST, "jei.new_ex_nihilo.category.harvest", stack("wooden_crook")),
                new CompostCategory(gui),
                new MeltingCategory(gui),
                new HeatCategory(gui),
                new TransitionCategory(gui),
                new SolidifyCategory(gui),
                new PrecipitateCategory(gui),
                new DollSpawnCategory(gui),
                new SilkwormInfestingCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<JeiRecipes.Sifting> sifting = JeiRecipeData.sifting();
        List<JeiRecipes.Drop> crushing = JeiRecipeData.crushing();
        List<JeiRecipes.Drop> harvest = JeiRecipeData.harvest();
        List<JeiRecipes.Compost> compost = JeiRecipeData.compost();
        List<JeiRecipes.Melting> melting = JeiRecipeData.melting();
        List<JeiRecipes.Heat> heat = JeiRecipeData.heat();
        List<JeiRecipes.Transition> transition = JeiRecipeData.transition();
        List<JeiRecipes.Solidify> solidify = JeiRecipeData.solidify();
        List<JeiRecipes.Precipitate> precipitate = JeiRecipeData.precipitate();
        List<JeiRecipes.DollSpawn> dollSpawn = JeiRecipeData.dollSpawns();
        List<JeiRecipes.SilkwormInfesting> silkwormInfesting = JeiRecipeData.silkwormInfesting();

        LOGGER.info(
                "Registering New Ex Nihilo JEI recipes: sifting={}, crushing={}, harvest={}, compost={}, melting={}, heat={}, transition={}, solidify={}, precipitate={}, doll_spawn={}, silkworm_infesting={}",
                sifting.size(),
                crushing.size(),
                harvest.size(),
                compost.size(),
                melting.size(),
                heat.size(),
                transition.size(),
                solidify.size(),
                precipitate.size(),
                dollSpawn.size(),
                silkwormInfesting.size());

        registration.addRecipes(SIFTING, sifting);
        registration.addRecipes(CRUSHING, crushing);
        registration.addRecipes(HARVEST, harvest);
        registration.addRecipes(COMPOST, compost);
        registration.addRecipes(MELTING, melting);
        registration.addRecipes(HEAT, heat);
        registration.addRecipes(TRANSITION, transition);
        registration.addRecipes(SOLIDIFY, solidify);
        registration.addRecipes(PRECIPITATE, precipitate);
        registration.addRecipes(DOLL_SPAWN, dollSpawn);
        registration.addRecipes(SILKWORM_INFESTING, silkwormInfesting);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        addCatalysts(registration, SIEVES, SIFTING);
        addCatalysts(registration, MESHES, SIFTING);
        addCatalystsEnding(registration, "_hammer", CRUSHING);
        addCatalystsEnding(registration, "_crook", HARVEST);
        addCatalysts(registration, allBarrelNames(), COMPOST, TRANSITION, SOLIDIFY, PRECIPITATE, DOLL_SPAWN);
        addCatalysts(registration, DOLLS, DOLL_SPAWN);
        addCatalysts(registration, List.of("silkworm", "infesting_leaves", "infested_leaves"), SILKWORM_INFESTING);
        addCatalysts(registration, allCrucibleNames(), MELTING);
        addCatalyst(registration, "fired_crucible", HEAT);
        addCatalyst(registration, "unfired_crucible", HEAT);
        for (JeiRecipes.Heat recipe : JeiRecipeData.heat()) {
            for (ItemStack stack : recipe.block()) {
                registration.addRecipeCatalyst(stack, HEAT);
            }
        }
    }

    private static void addCatalyst(IRecipeCatalystRegistration registration, String itemName, IRecipeType<?>... types) {
        ItemStack stack = stack(itemName);
        if (!stack.isEmpty()) {
            registration.addRecipeCatalyst(stack, types);
        }
    }

    private static void addCatalysts(IRecipeCatalystRegistration registration, List<String> itemNames, IRecipeType<?>... types) {
        for (String itemName : itemNames) {
            addCatalyst(registration, itemName, types);
        }
    }

    private static void addCatalystsEnding(IRecipeCatalystRegistration registration, String suffix, IRecipeType<?>... types) {
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null && id.getNamespace().equals(ExampleMod.MODID) && id.getPath().endsWith(suffix)) {
                registration.addRecipeCatalyst(new ItemStack(item), types);
            }
        }
    }

    private static ItemStack stack(String itemName) {
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(ExampleMod.MODID, itemName));
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static List<String> allBarrelNames() {
        List<String> barrels = new ArrayList<>();
        barrels.add("stone_barrel");
        barrels.addAll(WOOD_BARRELS);
        return barrels;
    }

    private static List<String> allCrucibleNames() {
        List<String> crucibles = new ArrayList<>();
        crucibles.add("fired_crucible");
        crucibles.add("unfired_crucible");
        crucibles.addAll(WOOD_CRUCIBLES);
        return crucibles;
    }

    private static List<ItemStack> stacks(List<String> itemNames) {
        List<ItemStack> stacks = new ArrayList<>();
        for (String itemName : itemNames) {
            ItemStack stack = stack(itemName);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    private static List<ItemStack> allBarrels() {
        return stacks(allBarrelNames());
    }

    private static List<ItemStack> stoneBarrel() {
        ItemStack stack = stack("stone_barrel");
        return stack.isEmpty() ? List.of() : List.of(stack);
    }

    private static boolean isLava(FluidStack stack) {
        return ExNihiloFluidIds.LAVA.equals(ExNihiloFluidIds.idForFluid(stack.getFluid()));
    }

    private static List<ItemStack> barrelsForTankFluid(FluidStack stack) {
        return isLava(stack) ? stoneBarrel() : allBarrels();
    }

    private static void barrelSlot(IRecipeLayoutBuilder builder, int x, int y, FluidStack tankFluid) {
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, x, y)
                .setStandardSlotBackground()
                .addItemStacks(barrelsForTankFluid(tankFluid))
                .addRichTooltipCallback((view, tooltip) -> {
                    if (isLava(tankFluid)) {
                        tooltip.add(Component.translatable("jei.new_ex_nihilo.requires_stone_barrel"));
                    }
                });
    }

    private static List<ItemStack> cruciblesFor(String crucibleType) {
        if ("wood".equals(crucibleType)) {
            return stacks(WOOD_CRUCIBLES);
        }
        ItemStack stack = stack("unfired".equals(crucibleType) ? "unfired_crucible" : "fired_crucible");
        return stack.isEmpty() ? List.of() : List.of(stack);
    }

    private static List<ItemStack> heatSources() {
        List<ItemStack> sources = new ArrayList<>();
        for (JeiRecipes.Heat recipe : JeiRecipeData.heat()) {
            for (ItemStack stack : recipe.block()) {
                if (stack.isEmpty() || containsSameItem(sources, stack)) {
                    continue;
                }
                sources.add(stack);
            }
        }
        return sources;
    }

    private static boolean containsSameItem(List<ItemStack> stacks, ItemStack candidate) {
        for (ItemStack stack : stacks) {
            if (ItemStack.isSameItemSameComponents(stack, candidate)) {
                return true;
            }
        }
        return false;
    }

    private static void itemSlot(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, List<ItemStack> stacks) {
        builder.addSlot(role, x, y).setStandardSlotBackground().addItemStacks(stacks);
    }

    private static void itemSlot(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, ItemStack stack) {
        builder.addSlot(role, x, y).setStandardSlotBackground().addItemStack(stack);
    }

    private static List<ItemStack> toolStacks(String suffix) {
        List<ItemStack> stacks = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null && id.getNamespace().equals(ExampleMod.MODID) && id.getPath().endsWith(suffix)) {
                stacks.add(new ItemStack(item));
            }
        }
        return stacks;
    }

    private static void fluidSlot(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, FluidStack stack) {
        builder.addSlot(role, x, y)
                .setStandardSlotBackground()
                .setFluidRenderer(Math.max(1000L, stack.getAmount()), false, 16, 16)
                .addFluidStack(stack.getFluid(), stack.getAmount(), stack.getComponentsPatch());
    }

    private static Component chance(float chance) {
        return Component.translatable("jei.new_ex_nihilo.chance", Math.round(chance * 1000.0F) / 10.0F);
    }

    private static Component heat(int amount) {
        return Component.translatable("jei.new_ex_nihilo.heat_amount", amount);
    }

    private static Component entity(String entityDescriptionId) {
        return Component.translatable("jei.new_ex_nihilo.entity_result", Component.translatable(entityDescriptionId));
    }

    private static List<Component> info(String... keys) {
        List<Component> lines = new ArrayList<>();
        for (String key : keys) {
            lines.add(Component.translatable(key));
        }
        return lines;
    }

    private static List<Component> silkwormInfo() {
        return List.of(
                Component.translatable("jei.new_ex_nihilo.silkworm_infesting.use"),
                Component.translatable("jei.new_ex_nihilo.silkworm_infesting.complete"),
                Component.translatable("jei.new_ex_nihilo.silkworm_infesting.spread"));
    }

    private abstract static class Category<T> implements IRecipeCategory<T> {
        private final IRecipeType<T> type;
        private final Component title;
        private final IDrawable icon;

        Category(IGuiHelper gui, IRecipeType<T> type, String titleKey, ItemStack iconStack) {
            this.type = type;
            this.title = Component.translatable(titleKey);
            this.icon = gui.createDrawableItemStack(iconStack);
        }

        @Override
        public IRecipeType<T> getRecipeType() {
            return type;
        }

        @Override
        public Component getTitle() {
            return title;
        }

        @Override
        public int getWidth() {
            return WIDTH;
        }

        @Override
        public int getHeight() {
            return HEIGHT;
        }

        @Override
        public IDrawable getIcon() {
            return icon;
        }
    }

    private static final class SiftingCategory extends Category<JeiRecipes.Sifting> {
        SiftingCategory(IGuiHelper gui) {
            super(gui, SIFTING, "jei.new_ex_nihilo.category.sifting", stack("oak_sieve"));
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, JeiRecipes.Sifting recipe, IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 8, 25)
                    .setStandardSlotBackground()
                    .addItemStacks(stacks(SIEVES))
                    .addRichTooltipCallback((view, tooltip) -> tooltip.addAll(info("jei.new_ex_nihilo.sifting.sieve")));
            itemSlot(builder, RecipeIngredientRole.INPUT, 36, 25, recipe.input());
            builder.addSlot(RecipeIngredientRole.INPUT, 66, 25)
                    .setStandardSlotBackground()
                    .addItemStack(recipe.mesh())
                    .addRichTooltipCallback((view, tooltip) -> tooltip.addAll(info("jei.new_ex_nihilo.sifting.mesh")));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 25)
                    .setOutputSlotBackground()
                    .addItemStack(recipe.output())
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(chance(recipe.chance())));
        }
    }

    private static final class DropCategory extends Category<JeiRecipes.Drop> {
        DropCategory(IGuiHelper gui, IRecipeType<JeiRecipes.Drop> type, String titleKey, ItemStack icon) {
            super(gui, type, titleKey, icon);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, JeiRecipes.Drop recipe, IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 8, 26)
                    .setStandardSlotBackground()
                    .addItemStacks(CRUSHING.equals(getRecipeType()) ? toolStacks("_hammer") : toolStacks("_crook"))
                    .addRichTooltipCallback((view, tooltip) -> tooltip.addAll(CRUSHING.equals(getRecipeType())
                            ? info("jei.new_ex_nihilo.crushing.info")
                            : info("jei.new_ex_nihilo.harvest.info")));
            itemSlot(builder, RecipeIngredientRole.INPUT, 32, 26, recipe.input());
            int index = 0;
            for (JeiRecipes.OutputChance output : recipe.outputs()) {
                int x = 62 + (index % 4) * 20;
                int y = 10 + (index / 4) * 20;
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                        .setOutputSlotBackground()
                        .addItemStack(output.stack())
                        .addRichTooltipCallback((view, tooltip) -> tooltip.add(chance(output.chance())));
                index++;
                if (index >= 12) {
                    break;
                }
            }
        }
    }

    private static final class CompostCategory extends Category<JeiRecipes.Compost> {
        CompostCategory(IGuiHelper gui) {
            super(gui, COMPOST, "jei.new_ex_nihilo.category.compost", stack("oak_barrel"));
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, JeiRecipes.Compost recipe, IFocusGroup focuses) {
            itemSlot(builder, RecipeIngredientRole.INPUT, 18, 25, recipe.input());
            builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 58, 25)
                    .setStandardSlotBackground()
                    .addItemStacks(allBarrels())
                    .addRichTooltipCallback((view, tooltip) -> tooltip.addAll(info(
                            "jei.new_ex_nihilo.compost.info",
                            "jei.new_ex_nihilo.compost.time")));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 25)
                    .setOutputSlotBackground()
                    .addItemStack(new ItemStack(Items.DIRT))
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.new_ex_nihilo.compost_amount", recipe.amount())));
        }
    }

    private static final class MeltingCategory extends Category<JeiRecipes.Melting> {
        MeltingCategory(IGuiHelper gui) {
            super(gui, MELTING, "jei.new_ex_nihilo.category.melting", stack("fired_crucible"));
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, JeiRecipes.Melting recipe, IFocusGroup focuses) {
            itemSlot(builder, RecipeIngredientRole.INPUT, 8, 25, recipe.input());
            builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 42, 25)
                    .setStandardSlotBackground()
                    .addItemStacks(cruciblesFor(recipe.crucibleType()))
                    .addRichTooltipCallback((view, tooltip) -> tooltip.addAll(info(
                            "jei.new_ex_nihilo.melting.info",
                            "jei.new_ex_nihilo.melting.capacity",
                            "wood".equals(recipe.crucibleType())
                                    ? "jei.new_ex_nihilo.melting.wood_heat"
                                    : "jei.new_ex_nihilo.melting.requires_heat")));
            if (!"wood".equals(recipe.crucibleType())) {
                builder.addSlot(RecipeIngredientRole.INPUT, 76, 25)
                        .setStandardSlotBackground()
                        .addItemStacks(heatSources())
                        .addRichTooltipCallback((view, tooltip) -> tooltip.addAll(info(
                                "jei.new_ex_nihilo.melting.heat_slot",
                                "jei.new_ex_nihilo.heat.info")));
            }
            fluidSlot(builder, RecipeIngredientRole.OUTPUT, 112, 25, recipe.output());
        }
    }

    private static final class HeatCategory extends Category<JeiRecipes.Heat> {
        HeatCategory(IGuiHelper gui) {
            super(gui, HEAT, "jei.new_ex_nihilo.category.heat", new ItemStack(Items.LAVA_BUCKET));
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, JeiRecipes.Heat recipe, IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.INPUT, 18, 25)
                    .setStandardSlotBackground()
                    .addItemStacks(recipe.block())
                    .addRichTooltipCallback((view, tooltip) -> {
                        tooltip.add(heat(recipe.amount()));
                        tooltip.addAll(info("jei.new_ex_nihilo.heat.info"));
                    });
            itemSlot(builder, RecipeIngredientRole.CRAFTING_STATION, 62, 25, stack("fired_crucible"));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 25)
                    .setOutputSlotBackground()
                    .addItemStack(new ItemStack(Items.LAVA_BUCKET))
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(heat(recipe.amount())));
        }
    }

    private static final class TransitionCategory extends Category<JeiRecipes.Transition> {
        TransitionCategory(IGuiHelper gui) {
            super(gui, TRANSITION, "jei.new_ex_nihilo.category.transition", stack("witch_water_bucket"));
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, JeiRecipes.Transition recipe, IFocusGroup focuses) {
            fluidSlot(builder, RecipeIngredientRole.INPUT, 10, 25, recipe.input());
            itemSlot(builder, RecipeIngredientRole.INPUT, 42, 25, recipe.catalyst());
            barrelSlot(builder, 70, 25, recipe.input());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 25)
                    .setStandardSlotBackground()
                    .setFluidRenderer(Math.max(1000L, recipe.output().getAmount()), false, 16, 16)
                    .addFluidStack(recipe.output().getFluid(), recipe.output().getAmount(), recipe.output().getComponentsPatch())
                    .addRichTooltipCallback((view, tooltip) -> tooltip.addAll(info("jei.new_ex_nihilo.transition.info")));
        }
    }

    private static final class SolidifyCategory extends Category<JeiRecipes.Solidify> {
        SolidifyCategory(IGuiHelper gui) {
            super(gui, SOLIDIFY, "jei.new_ex_nihilo.category.solidify", stack("stone_barrel"));
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, JeiRecipes.Solidify recipe, IFocusGroup focuses) {
            fluidSlot(builder, RecipeIngredientRole.INPUT, 10, 25, recipe.tankFluid());
            fluidSlot(builder, RecipeIngredientRole.INPUT, 42, 25, recipe.topFluid());
            barrelSlot(builder, 70, 25, recipe.tankFluid());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 25)
                    .setOutputSlotBackground()
                    .addItemStack(recipe.output())
                    .addRichTooltipCallback((view, tooltip) -> tooltip.addAll(info("jei.new_ex_nihilo.solidify.info")));
        }
    }

    private static final class PrecipitateCategory extends Category<JeiRecipes.Precipitate> {
        PrecipitateCategory(IGuiHelper gui) {
            super(gui, PRECIPITATE, "jei.new_ex_nihilo.category.precipitate", stack("sea_water_bucket"));
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, JeiRecipes.Precipitate recipe, IFocusGroup focuses) {
            fluidSlot(builder, RecipeIngredientRole.INPUT, 10, 25, recipe.fluid());
            itemSlot(builder, RecipeIngredientRole.INPUT, 42, 25, recipe.input());
            barrelSlot(builder, 70, 25, recipe.fluid());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 25)
                    .setOutputSlotBackground()
                    .addItemStack(recipe.output())
                    .addRichTooltipCallback((view, tooltip) -> tooltip.addAll(info("jei.new_ex_nihilo.precipitate.info")));
        }
    }

    private static final class DollSpawnCategory extends Category<JeiRecipes.DollSpawn> {
        DollSpawnCategory(IGuiHelper gui) {
            super(gui, DOLL_SPAWN, "jei.new_ex_nihilo.category.doll_spawn", stack("porcelain_doll"));
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, JeiRecipes.DollSpawn recipe, IFocusGroup focuses) {
            itemSlot(builder, RecipeIngredientRole.INPUT, 10, 25, recipe.doll());
            fluidSlot(builder, RecipeIngredientRole.INPUT, 42, 25, recipe.fluid());
            barrelSlot(builder, 70, 25, recipe.fluid());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 25)
                    .setOutputSlotBackground()
                    .addItemStack(recipe.output())
                    .addRichTooltipCallback((view, tooltip) -> {
                        tooltip.add(entity(recipe.entityDescriptionId()));
                        tooltip.addAll(info("jei.new_ex_nihilo.doll_spawn.info", "jei.new_ex_nihilo.doll_spawn.time"));
                    });
        }
    }

    private static final class SilkwormInfestingCategory extends Category<JeiRecipes.SilkwormInfesting> {
        SilkwormInfestingCategory(IGuiHelper gui) {
            super(gui, SILKWORM_INFESTING, "jei.new_ex_nihilo.category.silkworm_infesting", stack("silkworm"));
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, JeiRecipes.SilkwormInfesting recipe, IFocusGroup focuses) {
            itemSlot(builder, RecipeIngredientRole.INPUT, 8, 16, recipe.leaves());
            itemSlot(builder, RecipeIngredientRole.INPUT, 34, 16, recipe.silkworm());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 72, 16)
                    .setOutputSlotBackground()
                    .addItemStack(recipe.infestingLeaves())
                    .addRichTooltipCallback((view, tooltip) -> tooltip.addAll(silkwormInfo()));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 16)
                    .setOutputSlotBackground()
                    .addItemStack(recipe.infestedLeaves())
                    .addRichTooltipCallback((view, tooltip) -> tooltip.addAll(silkwormInfo()));
        }
    }
}
