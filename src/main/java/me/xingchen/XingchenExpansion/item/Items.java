package me.xingchen.XingchenExpansion.item;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import org.bukkit.Material;

public class Items {
    public static final SlimefunItemStack STARS_ORE = new SlimefunItemStack(
            "STARS_ORE",
            Material.AMETHYST_SHARD,
            "&6星辰矿石",
            "&7蕴含星辰之力"
    );
    public static final SlimefunItemStack STARS_INGOT = new SlimefunItemStack(
            "STARS_INGOT",
            Material.IRON_INGOT,
            "&6星辰锭",
            "&7星辰矿石精炼产物"
    );
    public static final SlimefunItemStack STARS_CRYSTAL = new SlimefunItemStack(
            "STARS_CRYSTAL",
            Material.PRISMARINE_CRYSTALS,
            "&6星辰水晶",
            "&7散发微弱光芒"
    );
    public static final SlimefunItemStack STARS_GENERATOR = new SlimefunItemStack(
            "STARS_GENERATOR",
            Material.AMETHYST_BLOCK,
            "&6星辰发电机",
            "&7使用星辰材料发电",
            "&7副产物: 星辰废料"
    );
    public static final SlimefunItemStack STARS_WASTE = new SlimefunItemStack(
            "STARS_WASTE",
            Material.GUNPOWDER,
            "&7星辰废料",
            "&7发电副产物"
    );
}