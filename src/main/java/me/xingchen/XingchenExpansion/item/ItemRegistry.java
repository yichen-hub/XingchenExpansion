package me.xingchen.XingchenExpansion.item;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import me.xingchen.XingchenExpansion.generator.StarsGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemRegistry {
    public static void register(JavaPlugin plugin) {
        // 普通物品
        registerItem(plugin, Items.STARS_ORE, "STARS_ORE");
        registerItem(plugin, Items.STARS_INGOT, "STARS_INGOT");
        registerItem(plugin, Items.STARS_CRYSTAL, "STARS_CRYSTAL");
        registerItem(plugin, Items.STARS_WASTE, "STARS_WASTE");
        // 机器
        registerGenerator(plugin, Items.STARS_GENERATOR, "STARS_GENERATOR");
    }

    private static void registerItem(JavaPlugin plugin, SlimefunItemStack item, String id) {
        if (item == null) {
            plugin.getLogger().warning("Failed to register item: " + id + " is null");
            return;
        }
        Recipes.RecipeEntry recipe = Recipes.getRecipe(item);
        if (recipe == null) {
            plugin.getLogger().info("No recipe found for item: " + item.getItemId() + ", skipping registration");
            return;
        }
        try {
            new SlimefunItem(
                    XingchenExpansion.XINGCHEN_GROUP,
                    item,
                    recipe.type,
                    recipe.recipe,
                    recipe.output
            ).register(XingchenExpansion.instance);
            plugin.getLogger().info("Registered item: " + item.getItemId());
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering item " + item.getItemId() + ": " + e.getMessage());
        }
    }

    private static void registerGenerator(JavaPlugin plugin, SlimefunItemStack item, String id) {
        if (item == null) {
            plugin.getLogger().warning("Failed to register generator: " + id + " is null");
            return;
        }
        Recipes.RecipeEntry recipe = Recipes.getRecipe(item);
        if (recipe == null) {
            plugin.getLogger().warning("No recipe found for generator: " + item.getItemId());
            return;
        }
        try {
            new StarsGenerator(
                    XingchenExpansion.XINGCHEN_GROUP,
                    item,
                    recipe.type,
                    recipe.recipe,
                    (XingchenExpansion) plugin
            ).register(XingchenExpansion.instance);
            plugin.getLogger().info("Registered generator: " + item.getItemId());
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering generator " + item.getItemId() + ": " + e.getMessage());
        }
    }
}