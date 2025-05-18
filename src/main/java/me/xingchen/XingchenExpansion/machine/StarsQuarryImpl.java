package me.xingchen.XingchenExpansion.machine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import me.xingchen.XingchenExpansion.machine.abstractMachine.StarsQuarry;
import org.bukkit.inventory.ItemStack;

public class StarsQuarryImpl {
    public static class L1 extends StarsQuarry {
        public L1(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe,
                  XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, plugin, "STARS_QUARRY_L1");
        }
    }

    public static class L2 extends StarsQuarry {
        public L2(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe,
                  XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, plugin, "STARS_QUARRY_L2");
        }
    }
}