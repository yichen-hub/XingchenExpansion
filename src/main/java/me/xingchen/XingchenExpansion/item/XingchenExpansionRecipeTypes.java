package me.xingchen.XingchenExpansion.item;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;


public class XingchenExpansionRecipeTypes {
    public static final RecipeType STARS_GENERATOR_SHOW = new RecipeType(
            new NamespacedKey(XingchenExpansion.instance, "stars_generator_show"),
            new SlimefunItemStack("STARS_GENERATOR_SHOW", Material.AMETHYST_BLOCK, "&e星辰发电机"),
            "&7使用&3星辰原矿&7,&5星辰锭&7,&6星辰水晶发电&7",
            "&7发电机运行时可产出副产物",
            "&7副产物: 星辰废料"
    );
    public static final RecipeType STARS_PURIFIER_SHOW = new RecipeType(
            new NamespacedKey(XingchenExpansion.instance, "stars_purifier_show"),
            new SlimefunItemStack("STARS_PURIFIER_SHOW", Material.QUARTZ_BLOCK, "&e星辰净化装置"),
            "&7净化&3星辰废料&7产出&6星辰源质"
    );
    public static final RecipeType DUNGEON_SHOW = new RecipeType(
            new NamespacedKey(XingchenExpansion.instance, "dungeon_show"),
            new SlimefunItemStack("DUNGEON_SHOW", Material.DIAMOND_SWORD, "&e副本获取"),
            "&7副本击杀怪物获取",
            "&7运营活动获取"
            );
}