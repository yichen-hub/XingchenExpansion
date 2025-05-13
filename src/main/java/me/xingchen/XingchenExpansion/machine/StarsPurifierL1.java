package me.xingchen.XingchenExpansion.machine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import org.bukkit.inventory.ItemStack;

public class StarsPurifierL1 extends StarsPurifier {
    public StarsPurifierL1(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, XingchenExpansion plugin) {
        super(itemGroup, item, recipeType, recipe, plugin, "Stars_Purifier_L1");
    }
}