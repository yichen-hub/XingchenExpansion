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
        //钻石模板
        addRecipe(Items.DIAMOND_TEMPLATE, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.STRING),              null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.DIAMOND_TEMPLATE
        ));

        //发电机
        //星辰太阳能发电机L1
        addRecipe(Items.STARS_SOLAR_GENERATOR_L1, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.STRING),              null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_SOLAR_GENERATOR_L1
        ));
        //星辰太阳能发电机L2
        addRecipe(Items.STARS_SOLAR_GENERATOR_L2, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.STRING),              null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_SOLAR_GENERATOR_L3
        ));
        //星辰太阳能发电机L3
        addRecipe(Items.STARS_SOLAR_GENERATOR_L3, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.STRING),              null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_SOLAR_GENERATOR_L3
        ));
        //星辰太阳能发电机L4
        addRecipe(Items.STARS_SOLAR_GENERATOR_L4, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.STRING),              null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_SOLAR_GENERATOR_L4
        ));
        //星辰太阳能发电机L5
        addRecipe(Items.STARS_SOLAR_GENERATOR_L5, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.STRING),              null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_SOLAR_GENERATOR_L5
        ));
        //星辰太阳能发电机-终极
        addRecipe(Items.STARS_SOLAR_GENERATOR_ULTIMATE, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.STRING),              null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_SOLAR_GENERATOR_ULTIMATE
        ));

        //星辰发电机L1
        addRecipe(Items.STARS_GENERATOR_L1, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.STRING),              null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_GENERATOR_L1
        ));
        //星辰发电机L2
        addRecipe(Items.STARS_GENERATOR_L2, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.STRING),              null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_GENERATOR_L2
        ));
        //星辰发电机-终极
        addRecipe(Items.STARS_GENERATOR_ULTIMATE, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.STRING),              null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_GENERATOR_ULTIMATE
        ));

        //机器

        //星辰矿机L1
        addRecipe(Items.STARS_QUARRY_L1, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.IRON_PICKAXE),        null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_QUARRY_L1
        ));
        //星辰矿机L2
        addRecipe(Items.STARS_QUARRY_L2, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.IRON_PICKAXE),        null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_QUARRY_L2
        ));
        //星辰矿机-终极
        addRecipe(Items.STARS_QUARRY_ULTIMATE, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.IRON_PICKAXE),        null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_QUARRY_ULTIMATE
        ));

        //星辰净化装置L1
        addRecipe(Items.STARS_PURIFIER_L1, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.IRON_PICKAXE),        null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_PURIFIER_L1
        ));
        //星辰净化装置L2
        addRecipe(Items.STARS_PURIFIER_L2, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.IRON_PICKAXE),        null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_PURIFIER_L2
        ));
        //星辰净化装置-终极
        addRecipe(Items.STARS_PURIFIER_ULTIMATE, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.IRON_PICKAXE),        null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_PURIFIER_ULTIMATE
        ));

        //星辰钻石生成器
        addRecipe(Items.STARS_DIAMOND_QUARRY_L1, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.IRON_PICKAXE),        null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_DIAMOND_QUARRY_L1
        ));
        //星辰钻石生成器L2
        addRecipe(Items.STARS_DIAMOND_QUARRY_L2, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.IRON_PICKAXE),        null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_DIAMOND_QUARRY_L2
        ));
        //星辰钻石生成器-终极
        addRecipe(Items.STARS_DIAMOND_QUARRY_ULTIMATE, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE,
                        null,                         new ItemStack(Material.IRON_PICKAXE),        null,
                        Items.STARS_ORE,              null,                                        Items.STARS_ORE
                },
                Items.STARS_DIAMOND_QUARRY_ULTIMATE
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