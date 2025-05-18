package me.xingchen.XingchenExpansion.generator;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import me.xingchen.XingchenExpansion.generator.abstractGenerator.StarsSolarGenerator;
import org.bukkit.inventory.ItemStack;

public class StarsSolarGeneratorImpl {
    public static class L1 extends StarsSolarGenerator {
        public L1(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, plugin, "STARS_SOLAR_GENERATOR_L1");
        }
    }
    public static class L2 extends StarsSolarGenerator {
        public L2(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, plugin, "STARS_SOLAR_GENERATOR_L2");
        }
    }
    public static class L3 extends StarsSolarGenerator {
        public L3(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, plugin, "STARS_SOLAR_GENERATOR_L3");
        }
    }
}