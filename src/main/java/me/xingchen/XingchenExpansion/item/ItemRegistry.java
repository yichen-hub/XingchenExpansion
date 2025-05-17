package me.xingchen.XingchenExpansion.item;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import me.xingchen.XingchenExpansion.generator.StarsGenerator;
import me.xingchen.XingchenExpansion.generator.StarsSolarGeneratorL1;
import me.xingchen.XingchenExpansion.generator.StarsSolarGeneratorL2;
import me.xingchen.XingchenExpansion.generator.StarsSolarGeneratorL3;
import me.xingchen.XingchenExpansion.machine.StarsPurifierL1;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;


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
        registerItem(plugin, Items.FORGE_STONE, "FORGE_STONE");
        registerItem(plugin, Items.FORGE_STONE_PLUS, "FORGE_STONE_PLUS");
        registerItem(plugin, Items.FORGE_TALISMAN, "FORGE_TALISMAN");

        //注册技能物品
        registerAbilityItem(plugin, Items.MANIFOLD, "MANIFOLD");
        registerAbilityItem(plugin, Items.WEAVING, "WEAVING");

        // 注册发电机（自动适配多配方）
        registerGenerator(plugin, Items.STARS_GENERATOR, StarsGenerator::new);
        registerGenerator(plugin, Items.SOLAR_GENERATOR_L1, StarsSolarGeneratorL1::new);
        registerGenerator(plugin, Items.SOLAR_GENERATOR_L2, StarsSolarGeneratorL2::new);
        registerGenerator(plugin, Items.SOLAR_GENERATOR_L3, StarsSolarGeneratorL3::new);

        // 注册机器
        registerMachine(plugin, Items.PURIFIER_L1, StarsPurifierL1::new);
    }

    // 注册普通物品，兼容多配方
    private static void registerItem(JavaPlugin plugin, SlimefunItemStack item, String id) {
        List<Recipes.RecipeEntry> entries = Recipes.getRecipes(item);
        if (item == null || entries == null || entries.isEmpty()) {
            plugin.getLogger().warning("Invalid item or recipe for: " + id);
            return;
        }
        try {
            for (Recipes.RecipeEntry entry : entries) {
                new SlimefunItem(
                        XingchenExpansion.MATERIAL_GROUP,
                        item,
                        entry.type,
                        entry.recipe,
                        entry.output
                ).register(XingchenExpansion.getInstance());
            }
            plugin.getLogger().info("Registered item: " + id);
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering item " + id + ": " + e.getMessage());
        }
    }
    //注册技能物品,兼容多配方
    private static void registerAbilityItem(JavaPlugin plugin, SlimefunItemStack item, String id) {
        Recipes.RecipeEntry entry = Recipes.getFirstRecipe(item);
        if (item == null || entry == null) {
            plugin.getLogger().warning("Invalid ability item or recipe for: " + id);
            return;
        }
        try {
            new SlimefunItem(
                    XingchenExpansion.ABILITY_ITEM_GROUP,
                    item,
                    entry.type,
                    entry.recipe,
                    entry.output
            ).register(XingchenExpansion.getInstance());
            plugin.getLogger().info("Registered ability item: " + id);
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering ability item " + id + ": " + e.getMessage());
        }
    }

    // 注册发电机，自动适配多配方
    private static void registerGenerator(JavaPlugin plugin, SlimefunItemStack item,
                                          GeneratorFactory factory) {
        List<Recipes.RecipeEntry> entries = Recipes.getRecipes(item);
        if (item == null || entries == null || entries.isEmpty()) {
            plugin.getLogger().warning("Invalid item or generator for: " + (item != null ? item.getItemId() : "null"));
            return;
        }
        try {
            for (Recipes.RecipeEntry entry : entries) {
                SlimefunItem generator = factory.create(
                        XingchenExpansion.MACHINE_GROUP,
                        item,
                        entry.type,
                        entry.recipe,
                        (XingchenExpansion) XingchenExpansion.getInstance()
                );
                generator.register(XingchenExpansion.getInstance());
            }
            plugin.getLogger().info("Registered generator: " + item.getItemId());
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering generator " + item.getItemId() + ": " + e.getMessage());
        }
    }

    // 注册机器，自动适配多配方
    private static void registerMachine(JavaPlugin plugin, SlimefunItemStack item,
                                        MachineFactory factory) {
        List<Recipes.RecipeEntry> entries = Recipes.getRecipes(item);
        if (item == null || entries == null || entries.isEmpty()) {
            plugin.getLogger().warning("Invalid item or machine for: " + (item != null ? item.getItemId() : "null"));
            return;
        }
        try {
            for (Recipes.RecipeEntry entry : entries) {
                SlimefunItem machine = factory.create(
                        XingchenExpansion.MACHINE_GROUP,
                        item,
                        entry.type,
                        entry.recipe,
                        (XingchenExpansion) XingchenExpansion.getInstance()
                );
                machine.register(XingchenExpansion.getInstance());
            }
            plugin.getLogger().info("Registered machine: " + item.getItemId());
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering machine " + item.getItemId() + ": " + e.getMessage());
        }
    }

    // 用于兼容不同构造器的函数式接口
    @FunctionalInterface
    private interface GeneratorFactory {
        SlimefunItem create(ItemGroup group, SlimefunItemStack item, RecipeType type, ItemStack[] recipe, XingchenExpansion plugin);
    }
    @FunctionalInterface
    private interface MachineFactory {
        SlimefunItem create(ItemGroup group, SlimefunItemStack item, RecipeType type, ItemStack[] recipe, XingchenExpansion plugin);
    }
}