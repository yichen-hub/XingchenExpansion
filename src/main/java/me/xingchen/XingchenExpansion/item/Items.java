package me.xingchen.XingchenExpansion.item;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import org.bukkit.Material;

public class Items {

    //物品

    //星辰原矿
    public static final SlimefunItemStack STARS_ORE = new SlimefunItemStack(
            "STARS_ORE",
            Material.AMETHYST_SHARD,
            "&3星辰原矿",
            "&7蕴含星辰之力"
    );
    //星辰锭
    public static final SlimefunItemStack STARS_INGOT = new SlimefunItemStack(
            "STARS_INGOT",
            Material.IRON_INGOT,
            "&5星辰锭",
            "&7星辰矿石精炼产物"
    );
    //星辰水晶
    public static final SlimefunItemStack STARS_CRYSTAL = new SlimefunItemStack(
            "STARS_CRYSTAL",
            Material.PRISMARINE_CRYSTALS,
            "&6星辰水晶",
            "&7散发微弱光芒"
    );
    //星辰废料
    public static final SlimefunItemStack STARS_WASTE = new SlimefunItemStack(
            "STARS_WASTE",
            Material.GUNPOWDER,
            "&7星辰废料",
            "&7星辰发电机的副产物"

    );
    //星辰源质
    public static final SlimefunItemStack STARS_SOURCE_QUALITY = new SlimefunItemStack(
            "STARS_SOURCE_QUALITY",
            Material.NETHER_STAR,
            "&6星辰源质",
            "&7提炼后的星辰材料",
            "&7由星辰废料在星辰净化器净化获得"
    );

    //发电机

    //星辰发电机
    public static final SlimefunItemStack STARS_GENERATOR = new SlimefunItemStack(
            "STARS_GENERATOR",
            Material.AMETHYST_BLOCK,
            "&6星辰发电机",
            "&7使用星辰材料发电",
            "&7星辰原矿:&6 20 j &7 /tick,10秒",
            "&7星辰锭:&6 30 j &7 /tick,30秒",
            "&7星辰水晶:&6 50 j &7 /tick,60秒",
            "&7副产物: 星辰废料"
    );

    //星辰太阳能发电机L1
    public static final SlimefunItemStack SOLAR_GENERATOR_L1 = new SlimefunItemStack(
            "STARS_SOLAR_GENERATOR_L1",
            Material.DAYLIGHT_DETECTOR,
            "&6星辰太阳能发电机 &4 I",
            "&7白天: 20 J/tick",
            "&7晚上: 10 J/tick"
    );
    //星辰太阳能发电机L2
    public static final SlimefunItemStack SOLAR_GENERATOR_L2 = new SlimefunItemStack(
            "STARS_SOLAR_GENERATOR_L2",
            Material.DAYLIGHT_DETECTOR,
            "&6星辰太阳能发电机 &4 II",
            "&7白天: &6 45 J &7/tick",
            "&7晚上: &6 25 J &7/tick"
    );
    //星辰太阳能发电机L3
    public static final SlimefunItemStack SOLAR_GENERATOR_L3 = new SlimefunItemStack(
            "STARS_SOLAR_GENERATOR_L3",
            Material.DAYLIGHT_DETECTOR,
            "&6星辰太阳能发电机 &4 III",
            "&7白天: &6 100 J &7 /tick",
            "&7晚上: &6 50 J &7 /tick"
    );

    //机器

    //星辰净化器
    public static final SlimefunItemStack PURIFIER_L1 = new SlimefunItemStack(
            "STARS_PURIFIER_L1",
            Material.QUARTZ_BLOCK,
            "&6星辰净化器 &4I",
            "&7将星辰废料转化为星辰源质",
            "&7耗电: &6 50 J &7 /tick",
            "&7转化时间: &6 5 &7 分钟"
    );
}