package me.xingchen.XingchenExpansion.item;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import org.bukkit.Material;

public class Items {
    //物品
    public static final SlimefunItemStack STARS_ORE = new SlimefunItemStack(
            "STARS_ORE",
            Material.AMETHYST_SHARD,
            "&3星辰矿石",
            "&7蕴含星辰之力"
    );
    public static final SlimefunItemStack STARS_INGOT = new SlimefunItemStack(
            "STARS_INGOT",
            Material.IRON_INGOT,
            "&5星辰锭",
            "&7星辰矿石精炼产物"
    );
    public static final SlimefunItemStack STARS_CRYSTAL = new SlimefunItemStack(
            "STARS_CRYSTAL",
            Material.PRISMARINE_CRYSTALS,
            "&6星辰水晶",
            "&7散发微弱光芒"
    );
    public static final SlimefunItemStack STARS_WASTE = new SlimefunItemStack(
            "STARS_WASTE",
            Material.GUNPOWDER,
            "&7星辰废料",
            "&7发电副产物"

    );
    //机器
    public static final SlimefunItemStack STARS_GENERATOR = new SlimefunItemStack(
            "STARS_GENERATOR",
            Material.AMETHYST_BLOCK,
            "&6星辰发电机",
            "&7使用星辰材料发电",
            "&7星辰原矿:20j/tick,10秒",
            "&7星辰锭:30j/tick,30秒",
            "&7星辰水晶:50j/tick,60秒",
            "&7副产物: 星辰废料"
    );

    public static final SlimefunItemStack SOLAR_GENERATOR_L1 = new SlimefunItemStack(
            "STARS_SOLAR_GENERATOR_L1",
            Material.DAYLIGHT_DETECTOR,
            "&6星辰太阳能发电机 &4Ⅰ",
            "&7白天: 20 J/tick",
            "&7晚上: 10 J/tick"
    );
    public static final SlimefunItemStack SOLAR_GENERATOR_L2 = new SlimefunItemStack(
            "STARS_SOLAR_GENERATOR_L2",
            Material.DAYLIGHT_DETECTOR,
            "&6星辰太阳能发电机 &4Ⅱ",
            "&7白天: 45 J/tick",
            "&7晚上: 25 J/tick"
    );
    public static final SlimefunItemStack SOLAR_GENERATOR_L3 = new SlimefunItemStack(
            "STARS_SOLAR_GENERATOR_L3",
            Material.DAYLIGHT_DETECTOR,
            "&6星辰太阳能发电机 &4Ⅲ",
            "&7白天: 100 J/tick",
            "&7晚上: 50 J/tick"
    );
}