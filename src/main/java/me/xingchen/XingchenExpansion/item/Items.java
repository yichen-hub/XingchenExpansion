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
    //锻造石
    public static final SlimefunItemStack FORGE_STONE = new SlimefunItemStack(
            "FORGE_STONE",
            Material.EMERALD,
            "&6普通锻造石",
            "&7用于装备打造,可提升装备打造等级"
    );
    //高级锻造石
    public static final SlimefunItemStack FORGE_STONE_PLUS = new SlimefunItemStack(
            "FORGE_STONE_PLUS",
            Material.DIAMOND,
            "&7高级锻造石",
            "&7用于装备打造,相比于普通锻造石打造成功概率更高"
    );
    //锻造护符
    public static final SlimefunItemStack FORGE_TALISMAN = new SlimefunItemStack(
            "FORGE_TALISMAN",
            Material.PAPER,
            "&7锻造护符",
            "&7用于装备打造,打造时放入可大概率避免打造失败时的掉级惩罚"
    );
    //流形
    public static final SlimefunItemStack MANIFOLD = new SlimefunItemStack(
            "MANIFOLD",
            Material.ECHO_SHARD,
            "&6流形",
            "&7右键使用,激活流形效果",
            "&7流形效果:腾空时按下shift键可向前冲刺",
            "&7流形效果持续 &6 15 &7 分钟"
    );
    //编织
    public static final SlimefunItemStack WEAVING = new SlimefunItemStack(
            "WEAVING",
            Material.ECHO_SHARD,
            "&6编织",
            "&7右键使用,激活编织效果",
            "&7编织效果: 每隔15秒，对玩家周围5格半径内的敌对生物施加5秒的缓慢X",
            "&7编织效果持续 &6 15 &7 分钟"
    );


    //发电机

    //星辰发电机
    public static final SlimefunItemStack STARS_GENERATOR = new SlimefunItemStack(
            "STARS_GENERATOR",
            Material.AMETHYST_BLOCK,
            "&6星辰发电机",
            "&7使用星辰材料发电",
            "&7星辰原矿:&620j&7 /tick--发电时长&6 10秒",
            "&7星辰锭:&630j&7 /tick--发电时长&6 30秒",
            "&7星辰水晶:&650j&7 /tick--发电时长&6 60秒",
            "&7副产物: 星辰废料"
    );

    //星辰太阳能发电机L1
    public static final SlimefunItemStack SOLAR_GENERATOR_L1 = new SlimefunItemStack(
            "STARS_SOLAR_GENERATOR_L1",
            Material.DAYLIGHT_DETECTOR,
            "&6星辰太阳能发电机 &4 I",
            "&7白天: &620J&7/tick",
            "&7晚上: &610J&7/tick"
    );
    //星辰太阳能发电机L2
    public static final SlimefunItemStack SOLAR_GENERATOR_L2 = new SlimefunItemStack(
            "STARS_SOLAR_GENERATOR_L2",
            Material.DAYLIGHT_DETECTOR,
            "&6星辰太阳能发电机 &4 II",
            "&7白天: &645J&7/tick",
            "&7晚上: &625J&7/tick"
    );
    //星辰太阳能发电机L3
    public static final SlimefunItemStack SOLAR_GENERATOR_L3 = new SlimefunItemStack(
            "STARS_SOLAR_GENERATOR_L3",
            Material.DAYLIGHT_DETECTOR,
            "&6星辰太阳能发电机 &4 III",
            "&7白天: &6100J &7/tick",
            "&7晚上: &650J&7/tick"
    );

    //机器

    //星辰净化器
    public static final SlimefunItemStack PURIFIER_L1 = new SlimefunItemStack(
            "STARS_PURIFIER_L1",
            Material.QUARTZ_BLOCK,
            "&6星辰净化装置 &4I",
            "&7将星辰废料转化为星辰源质",
            "&7耗电: &650J&7/tick",
            "&7转化时间: &65&7 分钟"
    );
}