package me.xingchen.XingchenExpansion.generator;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import me.xingchen.XingchenExpansion.generator.abstractGenerator.StarsGenerator;
import org.bukkit.inventory.ItemStack;
public class StarsGeneratorImpl {
    // 等级 L1 子类
    public static class L1 extends StarsGenerator {
        public L1(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, plugin, "STARS_GENERATOR_L1");
        }
    }

    // 等级 L2 子类
    public static class L2 extends StarsGenerator {
        public L2(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, plugin, "STARS_GENERATOR_L2");
        }
    }
}