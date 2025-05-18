package me.xingchen.XingchenExpansion.item;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Recipes {
    public static class RecipeEntry {
        public final RecipeType type;
        public final ItemStack[] recipe;
        public final ItemStack output;

        public RecipeEntry(RecipeType type, ItemStack[] recipe, ItemStack output) {
            this.type = type;
            this.recipe = recipe;
            this.output = output;
        }
    }

    // 改成 List<RecipeEntry>
    private static final Map<SlimefunItemStack, List<RecipeEntry>> RECIPES = new HashMap<>();

    // 新增方法，支持多配方
    public static void addRecipe(SlimefunItemStack output, RecipeEntry entry) {
        RECIPES.computeIfAbsent(output, k -> new ArrayList<>()).add(entry);
    }

    static {

        //物品

        // 星辰原矿
        addRecipe(Items.STARS_ORE, new RecipeEntry(
                RecipeType.ORE_CRUSHER,
                new ItemStack[]{new ItemStack(Material.AMETHYST_SHARD)},
                Items.STARS_ORE
        ));

        // 星辰锭
        addRecipe(Items.STARS_INGOT, new RecipeEntry(
                RecipeType.SMELTERY,
                new ItemStack[]{Items.STARS_ORE},
                Items.STARS_INGOT
        ));

        // 星辰结晶
        addRecipe(Items.STARS_CRYSTAL, new RecipeEntry(
                RecipeType.SMELTERY,
                new ItemStack[]{Items.STARS_ORE, Items.STARS_INGOT,new ItemStack(Material.DIAMOND)},
                Items.STARS_CRYSTAL
        ));

        // 星辰废料
        addRecipe(Items.STARS_WASTE, new RecipeEntry(
                XingchenExpansionRecipeTypes.STARS_GENERATOR_SHOW,
                new ItemStack[]{Items.STARS_INGOT},
                Items.STARS_WASTE
        ));
        // 星辰源质
        addRecipe(Items.STARS_SOURCE_QUALITY, new RecipeEntry(
                XingchenExpansionRecipeTypes.STARS_PURIFIER_SHOW,
                new ItemStack[]{Items.STARS_WASTE},
                Items.STARS_SOURCE_QUALITY
        ));
        //普通锻造石
            addRecipe(Items.FORGE_STONE, new RecipeEntry(
                XingchenExpansionRecipeTypes.DUNGEON_SHOW,
                new ItemStack[]{},
                Items.FORGE_STONE
        ));
        //高级锻造石
            addRecipe(Items.FORGE_STONE_PLUS, new RecipeEntry(
                XingchenExpansionRecipeTypes.DUNGEON_SHOW,
                new ItemStack[]{},
                Items.FORGE_STONE_PLUS
        ));
        //锻造护符
        addRecipe(Items.FORGE_TALISMAN, new RecipeEntry(
                XingchenExpansionRecipeTypes.DUNGEON_SHOW,
                new ItemStack[]{},
                Items.FORGE_TALISMAN
        ));


        //  流形
        addRecipe(Items.MANIFOLD, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.ENDER_PEARL),         null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.MANIFOLD
        ));
        // 编织
        addRecipe(Items.WEAVING, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.STRING),              null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.WEAVING
        ));

        //发电机

        // 星辰发电机Ⅰ
        addRecipe(Items.STARS_GENERATOR_L1, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              SlimefunItems.STEEL_INGOT,                  Items.STARS_INGOT,
                        SlimefunItems.STEEL_INGOT,      SlimefunItems.ELECTRO_MAGNET,               SlimefunItems.STEEL_INGOT,
                        Items.STARS_INGOT,              SlimefunItems.STEEL_INGOT,                  Items.STARS_INGOT
                },
                Items.STARS_GENERATOR_L1
        ));
        // 星辰发电机Ⅱ
        addRecipe(Items.STARS_GENERATOR_L2, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              SlimefunItems.STEEL_INGOT,                  Items.STARS_INGOT,
                        SlimefunItems.REINFORCED_ALLOY_INGOT,Items.STARS_GENERATOR_L1,              SlimefunItems.REINFORCED_ALLOY_INGOT,
                        Items.STARS_INGOT,              SlimefunItems.STEEL_INGOT,                  Items.STARS_INGOT
                },
                Items.STARS_GENERATOR_L2
        ));

        // 星辰太阳能发电机Ⅰ
        addRecipe(Items.SOLAR_GENERATOR_L1, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              SlimefunItems.STEEL_INGOT,                  Items.STARS_INGOT,
                        null,                           SlimefunItems.ELECTRO_MAGNET,               null,
                        Items.STARS_INGOT,              SlimefunItems.STEEL_INGOT,                  Items.STARS_INGOT
                },
                Items.SOLAR_GENERATOR_L1
        ));

        // 星辰太阳能发电机Ⅱ
        addRecipe(Items.SOLAR_GENERATOR_L2, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              SlimefunItems.STEEL_INGOT,                  Items.STARS_INGOT,
                        null,                           Items.SOLAR_GENERATOR_L1,                   null,
                        Items.STARS_INGOT,              SlimefunItems.STEEL_INGOT,                  Items.STARS_INGOT
                },
                Items.SOLAR_GENERATOR_L2
        ));
        //  星辰太阳能发电机Ⅲ
        addRecipe(Items.SOLAR_GENERATOR_L3, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              SlimefunItems.STEEL_INGOT,                  Items.STARS_INGOT,
                        null,                           Items.SOLAR_GENERATOR_L2,                   null,
                        Items.STARS_INGOT,              SlimefunItems.STEEL_INGOT,                  Items.STARS_INGOT
                },
                Items.SOLAR_GENERATOR_L3
        ));

        //机器

        // 星辰净化器Ⅰ
        addRecipe(Items.PURIFIER_L1, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT,
                        SlimefunItems.GOLD_24K,         SlimefunItems.ELECTRO_MAGNET,               SlimefunItems.GOLD_24K,
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT
                },
                Items.PURIFIER_L1
        ));
        // 星辰净化器Ⅱ
        addRecipe(Items.PURIFIER_L2, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT,
                        SlimefunItems.BLISTERING_INGOT, Items.PURIFIER_L1,                          SlimefunItems.BLISTERING_INGOT,
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT
                },
                Items.PURIFIER_L2
        ));
        // 星辰矿机Ⅰ
        addRecipe(Items.STARS_QUARRY_L1, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              Items.STARS_ORE,                            Items.STARS_INGOT,
                        SlimefunItems.GOLD_8K,          SlimefunItems.ELECTRO_MAGNET,               SlimefunItems.GOLD_8K,
                        Items.STARS_INGOT,              Items.STARS_ORE,                            Items.STARS_INGOT
                },
                Items.STARS_QUARRY_L1
        ));
        // 星辰矿机Ⅱ
        addRecipe(Items.STARS_QUARRY_L2, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              Items.STARS_ORE,                            Items.STARS_INGOT,
                        SlimefunItems.GOLD_24K,         Items.STARS_QUARRY_L1,                      SlimefunItems.GOLD_24K,
                        Items.STARS_INGOT,              Items.STARS_ORE,                            Items.STARS_INGOT
                },
                Items.STARS_QUARRY_L2
        ));
    }

    public static List<RecipeEntry> getRecipes(SlimefunItemStack item) {
        return RECIPES.getOrDefault(item, Collections.emptyList());
    }
    public static RecipeEntry getFirstRecipe(SlimefunItemStack item) {
        List<RecipeEntry> list = getRecipes(item);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }
}