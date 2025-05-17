package me.xingchen.XingchenExpansion.item;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
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
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        new ItemStack(Material.EMERALD),null,                                       new ItemStack(Material.DIAMOND),
                        null,                           null,                                       null,
                        null,                           null,                                       null
                },
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
                new ItemStack[]{Items.STARS_ORE, Items.STARS_INGOT},
                Items.STARS_CRYSTAL
        ));

        // 星辰废料
        addRecipe(Items.STARS_WASTE, new RecipeEntry(
                XingchenExpansionRecipeTypes.STARS_GENERATOR,
                new ItemStack[]{Items.STARS_INGOT},
                Items.STARS_WASTE
        ));
        addRecipe(Items.STARS_WASTE, new RecipeEntry(
                XingchenExpansionRecipeTypes.STARS_GENERATOR,
                new ItemStack[]{Items.STARS_ORE},
                Items.STARS_WASTE
        ));
        addRecipe(Items.STARS_WASTE, new RecipeEntry(
                XingchenExpansionRecipeTypes.STARS_GENERATOR,
                new ItemStack[]{Items.STARS_CRYSTAL},
                Items.STARS_WASTE
        ));

        // 星辰源质
        addRecipe(Items.STARS_SOURCE_QUALITY, new RecipeEntry(
                RecipeType.MULTIBLOCK,
                new ItemStack[]{Items.STARS_WASTE, Items.PURIFIER_L1},
                Items.STARS_SOURCE_QUALITY
        ));
        //普通锻造石
            addRecipe(Items.FORGE_STONE, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT,
                        Items.STARS_CRYSTAL,            new ItemStack(Material.EMERALD),            Items.STARS_CRYSTAL,
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT
                },
                Items.FORGE_STONE
        ));
        //高级锻造石
            addRecipe(Items.FORGE_STONE_PLUS, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT,
                        Items.STARS_CRYSTAL,            new ItemStack(Material.DIAMOND),            Items.STARS_CRYSTAL,
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                         Items.STARS_INGOT
                },
                Items.FORGE_STONE_PLUS
        ));
        //锻造护符
        addRecipe(Items.FORGE_TALISMAN, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT,
                        Items.STARS_CRYSTAL,            new ItemStack(Material.NETHERITE_INGOT),    Items.STARS_CRYSTAL,
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                         Items.STARS_INGOT
                },
                Items.FORGE_TALISMAN
        ));


        //  流形
        addRecipe(Items.MANIFOLD, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT,
                        Items.STARS_CRYSTAL,            new ItemStack(Material.SHIELD),             Items.STARS_CRYSTAL,
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT
                },
                Items.MANIFOLD
        ));
        // 编织
        addRecipe(Items.WEAVING, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT,
                        Items.STARS_CRYSTAL,            new ItemStack(Material.SHIELD),             Items.STARS_CRYSTAL,
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT
                },
                Items.WEAVING
        ));

        //发电机

        // 星辰发电机
        addRecipe(Items.STARS_GENERATOR, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT,
                        new ItemStack(Material.FURNACE), new ItemStack(Material.REDSTONE_BLOCK),    new ItemStack(Material.FURNACE),
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT
                },
                Items.STARS_GENERATOR
        ));

        // 星辰太阳能发电机Ⅰ
        addRecipe(Items.SOLAR_GENERATOR_L1, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              new ItemStack(Material.DAYLIGHT_DETECTOR),  Items.STARS_INGOT,
                        Items.STARS_CRYSTAL,            new ItemStack(Material.REDSTONE_BLOCK),     Items.STARS_CRYSTAL,
                        Items.STARS_INGOT,              new ItemStack(Material.FURNACE),            Items.STARS_INGOT
                },
                Items.SOLAR_GENERATOR_L1
        ));

        // 星辰太阳能发电机Ⅱ
        addRecipe(Items.SOLAR_GENERATOR_L2, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              Items.SOLAR_GENERATOR_L1,                   Items.STARS_INGOT,
                        Items.STARS_CRYSTAL,            new ItemStack(Material.REDSTONE_BLOCK),     Items.STARS_CRYSTAL,
                        Items.STARS_INGOT,              new ItemStack(Material.FURNACE),            Items.STARS_INGOT
                },
                Items.SOLAR_GENERATOR_L2
        ));
        //  星辰太阳能发电机Ⅲ
        addRecipe(Items.SOLAR_GENERATOR_L3, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              Items.SOLAR_GENERATOR_L2,                   Items.STARS_INGOT,
                        Items.STARS_CRYSTAL,            new ItemStack(Material.REDSTONE_BLOCK),     Items.STARS_CRYSTAL,
                        Items.STARS_INGOT,              new ItemStack(Material.FURNACE),            Items.STARS_INGOT
                },
                Items.SOLAR_GENERATOR_L3
        ));

        //机器

        // 星辰净化器Ⅰ
        addRecipe(Items.PURIFIER_L1, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT,
                        Items.STARS_CRYSTAL,            new ItemStack(Material.SMITHING_TABLE),     Items.STARS_CRYSTAL,
                        Items.STARS_INGOT,              new ItemStack(Material.REDSTONE_BLOCK),     Items.STARS_INGOT
                },
                Items.PURIFIER_L1
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