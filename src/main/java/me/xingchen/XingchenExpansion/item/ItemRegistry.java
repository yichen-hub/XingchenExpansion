package me.xingchen.XingchenExpansion.item;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import me.xingchen.XingchenExpansion.generator.StarsGenerator;
import me.xingchen.XingchenExpansion.generator.StarsSolarGeneratorL1;
import me.xingchen.XingchenExpansion.generator.StarsSolarGeneratorL2;
import me.xingchen.XingchenExpansion.machine.StarsPurifierL1;
import org.bukkit.plugin.java.JavaPlugin;



public class ItemRegistry {
    /**
     * 注册所有物品和机器。
     *
     * @param plugin 主插件实例
     */
    public static void register(JavaPlugin plugin) {
        // 注册材料
        registerItem(plugin, Items.STARS_ORE, "STARS_ORE");
        registerItem(plugin, Items.STARS_INGOT, "STARS_INGOT");
        registerItem(plugin, Items.STARS_CRYSTAL, "STARS_CRYSTAL");
        registerItem(plugin, Items.STARS_WASTE, "STARS_WASTE");
        registerItem(plugin, Items.STARS_SOURCE_QUALITY, "STARS_SOURCE_QUALITY");

        // 注册发电机
        registerGenerator(plugin, Items.STARS_GENERATOR, new StarsGenerator(
                XingchenExpansion.XINGCHEN_GROUP,
                Items.STARS_GENERATOR,
                Recipes.getRecipe(Items.STARS_GENERATOR).type,
                Recipes.getRecipe(Items.STARS_GENERATOR).recipe,
                (XingchenExpansion) plugin
        ));
        registerGenerator(plugin, Items.SOLAR_GENERATOR_L1, new StarsSolarGeneratorL1(
                XingchenExpansion.XINGCHEN_GROUP,
                Items.SOLAR_GENERATOR_L1,
                Recipes.getRecipe(Items.SOLAR_GENERATOR_L1).type,
                Recipes.getRecipe(Items.SOLAR_GENERATOR_L1).recipe,
                (XingchenExpansion) plugin
        ));
        registerGenerator(plugin, Items.SOLAR_GENERATOR_L2, new StarsSolarGeneratorL2(
                XingchenExpansion.XINGCHEN_GROUP,
                Items.SOLAR_GENERATOR_L2,
                Recipes.getRecipe(Items.SOLAR_GENERATOR_L2).type,
                Recipes.getRecipe(Items.SOLAR_GENERATOR_L2).recipe,
                (XingchenExpansion) plugin
        ));
        registerGenerator(plugin, Items.SOLAR_GENERATOR_L3, new StarsSolarGeneratorL2(
                XingchenExpansion.XINGCHEN_GROUP,
                Items.SOLAR_GENERATOR_L3,
                Recipes.getRecipe(Items.SOLAR_GENERATOR_L3).type,
                Recipes.getRecipe(Items.SOLAR_GENERATOR_L3).recipe,
                (XingchenExpansion) plugin
        ));
        //注册机器
        registerMachine(plugin, Items.PURIFIER_L1, new StarsPurifierL1(
                XingchenExpansion.XINGCHEN_GROUP,
                Items.PURIFIER_L1,
                Recipes.getRecipe(Items.PURIFIER_L1).type,
                Recipes.getRecipe(Items.PURIFIER_L1).recipe,
                (XingchenExpansion) plugin
        ));

    }
    private static void registerItem(JavaPlugin plugin, SlimefunItemStack item, String id) {
        if (item == null || Recipes.getRecipe(item) == null) {
            plugin.getLogger().warning("Invalid item or recipe for: " + id);
            return;
        }
        try {
            new SlimefunItem(
                    XingchenExpansion.XINGCHEN_GROUP,
                    item,
                    Recipes.getRecipe(item).type,
                    Recipes.getRecipe(item).recipe,
                    Recipes.getRecipe(item).output
            ).register(XingchenExpansion.getInstance());
            plugin.getLogger().info("Registered item: " + id);
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering item " + id + ": " + e.getMessage());
        }
    }
    private static void registerGenerator(JavaPlugin plugin, SlimefunItemStack item, SlimefunItem generator) {
        if (item == null || generator == null) {
            plugin.getLogger().warning("Invalid item or generator for: " + (item != null ? item.getItemId() : "null"));
            return;
        }
        try {
            generator.register(XingchenExpansion.getInstance());
            plugin.getLogger().info("Registered generator: " + item.getItemId());
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering generator " + item.getItemId() + ": " + e.getMessage());
        }
    }
    private static void registerMachine(JavaPlugin plugin, SlimefunItemStack item, SlimefunItem machine) {
        if (item == null || machine == null) {
            plugin.getLogger().warning("Invalid item or machine for: " + (item != null ? item.getItemId() : "null"));
            return;
        }
        try {
            machine.register(XingchenExpansion.getInstance());
            plugin.getLogger().info("Registered machine: " + item.getItemId());
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering machine " + item.getItemId() + ": " + e.getMessage());
        }
    }
}
