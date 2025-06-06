package me.xingchen.XingchenExpansion.item;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import me.xingchen.XingchenExpansion.generator.StarsGenerator;
import me.xingchen.XingchenExpansion.generator.StarsSolarGenerator;
import me.xingchen.XingchenExpansion.machine.StarsOreQuarry;
import me.xingchen.XingchenExpansion.machine.StarsPurifier;
import me.xingchen.XingchenExpansion.machine.StarsQuarry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;

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

        // 注册技能物品
        registerAbilityItem(plugin, Items.MANIFOLD, "MANIFOLD");
        registerAbilityItem(plugin, Items.WEAVING, "WEAVING");

        //普通发电机
        registerGenerator(plugin, Items.STARS_GENERATOR_L1, StarsGenerator.StarsGeneratorL1::new);
        registerGenerator(plugin, Items.STARS_GENERATOR_L2, StarsGenerator.StarsGeneratorL2::new);
        registerGenerator(plugin, Items.STARS_SOLAR_GENERATOR_L1, StarsSolarGenerator.StarsSolarGeneratorL1::new);
        registerGenerator(plugin, Items.STARS_SOLAR_GENERATOR_L2, StarsSolarGenerator.StarsSolarGeneratorL2::new);
        registerGenerator(plugin, Items.STARS_SOLAR_GENERATOR_L3, StarsSolarGenerator.StarsSolarGeneratorL3::new);
        registerGenerator(plugin, Items.STARS_SOLAR_GENERATOR_L4, StarsSolarGenerator.StarsSolarGeneratorL4::new);
        registerGenerator(plugin, Items.STARS_SOLAR_GENERATOR_L5, StarsSolarGenerator.StarsSolarGeneratorL5::new);

        //普通机器
        registerMachine(plugin, Items.STARS_QUARRY_L1, StarsQuarry.StarsQuarryL1::new);
        registerMachine(plugin, Items.STARS_QUARRY_L2, StarsQuarry.StarsQuarryL2::new);
        registerMachine(plugin, Items.STARS_PURIFIER_L1, StarsPurifier.StarsPurifierL1::new);
        registerMachine(plugin, Items.STARS_PURIFIER_L2, StarsPurifier.StarsPurifierL2::new);
        registerMachine(plugin, Items.STARS_DIAMOND_QUARRY_L1, StarsOreQuarry.StarsDiamondQuarryL1::new);
        registerMachine(plugin, Items.STARS_DIAMOND_QUARRY_L2, StarsOreQuarry.StarsDiamondQuarryL2::new);

        //终极机器
        registerUltimateMachine(plugin, Items.STARS_QUARRY_ULTIMATE, StarsQuarry.StarsQuarryUltimate::new);
        registerUltimateMachine(plugin, Items.STARS_PURIFIER_ULTIMATE, StarsPurifier.StarsPurifierUltimate::new);
        registerUltimateMachine(plugin, Items.STARS_DIAMOND_QUARRY_ULTIMATE, StarsOreQuarry.StarsDiamondQuarryUltimate::new);

        //终极发电机
        registerUltimateGenerator(plugin, Items.STARS_SOLAR_GENERATOR_ULTIMATE, StarsSolarGenerator.StarsSolarGeneratorUltimate::new);
        registerUltimateGenerator(plugin, Items.STARS_GENERATOR_ULTIMATE, StarsGenerator.StarsGeneratorUltimate::new);
    }

    // 注册普通物品，仅使用第一个配方注册 SlimefunItem
    private static void registerItem(JavaPlugin plugin, SlimefunItemStack item, String id) {
        List<Recipes.RecipeEntry> entries = Recipes.getRecipes(item);
        if (item == null || entries == null || entries.isEmpty()) {
            plugin.getLogger().warning("Invalid item or recipe for: " + id);
            return;
        }
        try {
            // 使用第一个配方注册 SlimefunItem
            Recipes.RecipeEntry entry = entries.get(0);
            Logger logger = plugin.getLogger();
            logger.info("Registering item: " + id + " with recipe type: " + entry.type.getKey());
            SlimefunItem slimefunItem = new SlimefunItem(
                    XingchenExpansion.MATERIAL_GROUP,
                    item,
                    entry.type,
                    entry.recipe,
                    entry.output
            );
            slimefunItem.register(XingchenExpansion.getInstance());
            if (entries.size() > 1) {
                logger.info("Item " + id + " has " + entries.size() + " recipes, only the first is used for registration. Additional recipes available via Recipes.getRecipes()");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering item " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 注册技能物品，使用第一个配方
    private static void registerAbilityItem(JavaPlugin plugin, SlimefunItemStack item, String id) {
        Recipes.RecipeEntry entry = Recipes.getFirstRecipe(item);
        if (item == null || entry == null) {
            plugin.getLogger().warning("Invalid ability item or recipe for: " + id);
            return;
        }
        try {
            Logger logger = plugin.getLogger();
            logger.info("Registering ability item: " + id + " with recipe type: " + entry.type.getKey());
            SlimefunItem slimefunItem = new SlimefunItem(
                    XingchenExpansion.ABILITY_ITEM_GROUP,
                    item,
                    entry.type,
                    entry.recipe,
                    entry.output
            );
            slimefunItem.register(XingchenExpansion.getInstance());
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering ability item " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 注册发电机，仅使用第一个配方注册
    private static void registerGenerator(JavaPlugin plugin, SlimefunItemStack item, GeneratorFactory factory) {
        List<Recipes.RecipeEntry> entries = Recipes.getRecipes(item);
        if (item == null || entries == null || entries.isEmpty()) {
            plugin.getLogger().warning("Invalid item or generator for: " + (item != null ? item.getItemId() : "null"));
            return;
        }
        try {
            // 使用第一个配方注册发电机
            Recipes.RecipeEntry entry = entries.get(0);
            Logger logger = plugin.getLogger();
            logger.info("Registering generator: " + item.getItemId() + " with recipe type: " + entry.type.getKey());
            SlimefunItem generator = factory.create(
                    XingchenExpansion.MACHINE_GROUP,
                    item,
                    entry.type,
                    entry.recipe,
                    (XingchenExpansion) XingchenExpansion.getInstance()
            );
            generator.register(XingchenExpansion.getInstance());
            if (entries.size() > 1) {
                logger.info("Generator " + item.getItemId() + " has " + entries.size() + " recipes, only the first is used for registration. Additional recipes available via Recipes.getRecipes()");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering generator " + item.getItemId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    // 注册终极发电机，仅使用第一个配方注册
    private static void registerUltimateGenerator(JavaPlugin plugin, SlimefunItemStack item, GeneratorFactory factory) {
        List<Recipes.RecipeEntry> entries = Recipes.getRecipes(item);
        if (item == null || entries == null || entries.isEmpty()) {
            plugin.getLogger().warning("Invalid item or generator for: " + (item != null ? item.getItemId() : "null"));
            return;
        }
        try {
            // 使用第一个配方注册发电机
            Recipes.RecipeEntry entry = entries.get(0);
            Logger logger = plugin.getLogger();
            logger.info("Registering generator: " + item.getItemId() + " with recipe type: " + entry.type.getKey());
            SlimefunItem generator = factory.create(
                    XingchenExpansion.ULTIMATE_MACHINE_GROUP,
                    item,
                    entry.type,
                    entry.recipe,
                    (XingchenExpansion) XingchenExpansion.getInstance()
            );
            generator.register(XingchenExpansion.getInstance());
            if (entries.size() > 1) {
                logger.info("Generator " + item.getItemId() + " has " + entries.size() + " recipes, only the first is used for registration. Additional recipes available via Recipes.getRecipes()");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering generator " + item.getItemId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 注册机器，仅使用第一个配方注册
    private static void registerMachine(JavaPlugin plugin, SlimefunItemStack item, MachineFactory factory) {
        List<Recipes.RecipeEntry> entries = Recipes.getRecipes(item);
        if (item == null || entries == null || entries.isEmpty()) {
            plugin.getLogger().warning("Invalid item or machine for: " + (item != null ? item.getItemId() : "null"));
            return;
        }
        try {
            // 使用第一个配方注册机器
            Recipes.RecipeEntry entry = entries.get(0);
            Logger logger = plugin.getLogger();
            logger.info("Registering machine: " + item.getItemId() + " with recipe type: " + entry.type.getKey());
            SlimefunItem machine = factory.create(
                    XingchenExpansion.MACHINE_GROUP,
                    item,
                    entry.type,
                    entry.recipe,
                    (XingchenExpansion) XingchenExpansion.getInstance()
            );
            machine.register(XingchenExpansion.getInstance());
            if (entries.size() > 1) {
                logger.info("Machine " + item.getItemId() + " has " + entries.size() + " recipes, only the first is used for registration. Additional recipes available via Recipes.getRecipes()");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering machine " + item.getItemId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    // 注册终极机器，仅使用第一个配方注册
    private static void registerUltimateMachine(JavaPlugin plugin, SlimefunItemStack item, MachineFactory factory) {
        List<Recipes.RecipeEntry> entries = Recipes.getRecipes(item);
        if (item == null || entries == null || entries.isEmpty()) {
            plugin.getLogger().warning("Invalid item or machine for: " + (item != null ? item.getItemId() : "null"));
            return;
        }
        try {
            // 使用第一个配方注册机器
            Recipes.RecipeEntry entry = entries.get(0);
            Logger logger = plugin.getLogger();
            logger.info("Registering machine: " + item.getItemId() + " with recipe type: " + entry.type.getKey());
            SlimefunItem machine = factory.create(
                    XingchenExpansion.ULTIMATE_MACHINE_GROUP,
                    item,
                    entry.type,
                    entry.recipe,
                    (XingchenExpansion) XingchenExpansion.getInstance()
            );
            machine.register(XingchenExpansion.getInstance());
            if (entries.size() > 1) {
                logger.info("Machine " + item.getItemId() + " has " + entries.size() + " recipes, only the first is used for registration. Additional recipes available via Recipes.getRecipes()");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering machine " + item.getItemId() + ": " + e.getMessage());
            e.printStackTrace();
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