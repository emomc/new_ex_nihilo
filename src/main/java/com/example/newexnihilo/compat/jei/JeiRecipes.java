package com.example.newexnihilo.compat.jei;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public final class JeiRecipes {
    private JeiRecipes() {
    }

    public record Sifting(List<ItemStack> input, ItemStack mesh, ItemStack output, float chance) {
    }

    public record Drop(List<ItemStack> input, List<OutputChance> outputs) {
    }

    public record Compost(List<ItemStack> input, int amount) {
    }

    public record Melting(List<ItemStack> input, String crucibleType, FluidStack output) {
    }

    public record Heat(List<ItemStack> block, int amount) {
    }

    public record Transition(FluidStack input, List<ItemStack> catalyst, FluidStack output) {
    }

    public record Solidify(FluidStack tankFluid, FluidStack topFluid, ItemStack output) {
    }

    public record Precipitate(FluidStack fluid, List<ItemStack> input, ItemStack output) {
    }

    public record DollSpawn(ItemStack doll, FluidStack fluid, ItemStack output, String entityDescriptionId) {
    }

    public record SilkwormInfesting(List<ItemStack> leaves, ItemStack silkworm, ItemStack infestingLeaves, ItemStack infestedLeaves) {
    }

    public record OutputChance(ItemStack stack, float chance) {
    }
}
