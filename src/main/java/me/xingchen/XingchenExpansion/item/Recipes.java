package me.xingchen.XingchenExpansion.item;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

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

    private static final Map<SlimefunItemStack, RecipeEntry> RECIPES = new HashMap<>();

    static {
        // 星辰原矿
        RECIPES.put(Items.STARS_ORE, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        new ItemStack(Material.EMERALD), null, new ItemStack(Material.DIAMOND),
                        null, null, null,
                        null, null, null
                },
                Items.STARS_ORE
        ));

        // 星辰锭
        RECIPES.put(Items.STARS_INGOT, new RecipeEntry(
                RecipeType.SMELTERY,
                new ItemStack[]{Items.STARS_ORE},
                Items.STARS_INGOT
        ));

        // 星辰结晶
        RECIPES.put(Items.STARS_CRYSTAL, new RecipeEntry(
                RecipeType.SMELTERY,
                new ItemStack[]{Items.STARS_ORE, Items.STARS_INGOT},
                Items.STARS_CRYSTAL
        ));

        // 星辰发电机
        RECIPES.put(Items.STARS_GENERATOR, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT,
                        new ItemStack(Material.FURNACE), new ItemStack(Material.REDSTONE_BLOCK),    new ItemStack(Material.FURNACE),
                        Items.STARS_INGOT,              Items.STARS_CRYSTAL,                        Items.STARS_INGOT
                },
                Items.STARS_GENERATOR
        ));

        // 星辰太阳能发电机Ⅰ
        RECIPES.put(Items.SOLAR_GENERATOR_L1, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              new ItemStack(Material.DAYLIGHT_DETECTOR),  Items.STARS_INGOT,
                        Items.STARS_CRYSTAL,            new ItemStack(Material.REDSTONE_BLOCK),     Items.STARS_CRYSTAL,
                        Items.STARS_INGOT,              new ItemStack(Material.FURNACE),            Items.STARS_INGOT
                },
                Items.SOLAR_GENERATOR_L1
        ));

        // 星辰太阳能发电机Ⅱ
        RECIPES.put(Items.SOLAR_GENERATOR_L2, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              Items.SOLAR_GENERATOR_L1,                   Items.STARS_INGOT,
                        Items.STARS_CRYSTAL,            new ItemStack(Material.REDSTONE_BLOCK),     Items.STARS_CRYSTAL,
                        Items.STARS_INGOT,              new ItemStack(Material.FURNACE),            Items.STARS_INGOT
                },
                Items.SOLAR_GENERATOR_L2
        ));
        //  星辰太阳能发电机Ⅲ
        RECIPES.put(Items.SOLAR_GENERATOR_L3, new RecipeEntry(
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[]{
                        Items.STARS_INGOT,              Items.SOLAR_GENERATOR_L2,                   Items.STARS_INGOT,
                        Items.STARS_CRYSTAL,            new ItemStack(Material.REDSTONE_BLOCK),     Items.STARS_CRYSTAL,
                        Items.STARS_INGOT,              new ItemStack(Material.FURNACE),            Items.STARS_INGOT
                },
                Items.SOLAR_GENERATOR_L3
        ));
    }

    public static RecipeEntry getRecipe(SlimefunItemStack item) {
        return RECIPES.get(item);
    }
}