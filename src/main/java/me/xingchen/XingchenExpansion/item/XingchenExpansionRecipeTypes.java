package me.xingchen.XingchenExpansion.item;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;


public class XingchenExpansionRecipeTypes {
    public static final RecipeType STARS_GENERATOR = new RecipeType(
            new NamespacedKey(XingchenExpansion.instance, "stars_generator"),
            new SlimefunItemStack("STARS_GENERATOR", Material.BEACON, "&e星辰发电机"),
            "&7使用&3星辰原矿&7,&5星辰锭&7,&6星辰水晶发电&7",
            "&7发电机运行时可产出副产物",
            "&7副产物: 星辰废料"
    );
}