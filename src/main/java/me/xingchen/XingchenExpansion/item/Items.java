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
            "&7粉碎紫水晶碎片获取",
            "&7蕴含微量星辰能量"
    );
    //星辰锭
    public static final SlimefunItemStack STARS_INGOT = new SlimefunItemStack(
            "STARS_INGOT",
            Material.IRON_INGOT,
            "&5星辰锭",
            "&7星辰原矿精炼后的产物" ,
            "蕴含少量星辰能量"
    );
    //星辰水晶
    public static final SlimefunItemStack STARS_CRYSTAL = new SlimefunItemStack(
            "STARS_CRYSTAL",
            Material.PRISMARINE_CRYSTALS,
            "&6星辰水晶",
            "&7星辰材料进一步提炼的产物" ,
            "&7蕴含大量星辰能量"
    );
    //星辰废料
    public static final SlimefunItemStack STARS_WASTE = new SlimefunItemStack(
            "STARS_WASTE",
            Material.GUNPOWDER,
            "&7星辰废料",
            "&7星辰衰变后的产物",
            "&7由星辰发电机发电时产出"

    );
    //星辰源质
    public static final SlimefunItemStack STARS_SOURCE_QUALITY = new SlimefunItemStack(
            "STARS_SOURCE_QUALITY",
            Material.NETHER_STAR,
            "&6星辰源质",
            "&7灰烬中重生",
            "&7蕴含极高浓度的星辰能量",
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
            Material.POTION,
            "&6流形",
            "&7右键使用,激活流形效果",
            "&7流形效果:腾空时按下shift键可向前冲刺",
            "&7流形效果持续 &6 15 &7 分钟"
    );
    //编织
    public static final SlimefunItemStack WEAVING = new SlimefunItemStack(
            "WEAVING",
            Material.POTION,
            "&6编织",
            "&7右键使用,激活编织效果",
            "&7编织效果: 每隔15秒，对玩家周围5格半径内的敌对生物施加5秒的缓慢X",
            "&7编织效果持续 &6 15 &7 分钟"
    );
    //钻石模板
    public static final SlimefunItemStack DIAMOND_TEMPLATE = new SlimefunItemStack(
            "DIAMOND_TEMPLATE",
            Material.DIAMOND_BLOCK,
            "&5钻石模板",
            "&7用于制作星辰矿物生成机"
    );

    //发电机

    //星辰太阳能发电机L1
    public static final SlimefunItemStack STARS_SOLAR_GENERATOR_L1 = new SlimefunItemStack(
            "STARS_SOLAR_GENERATOR_L1",
            Material.DAYLIGHT_DETECTOR,
            "&6星辰太阳能发电机 &4 I",
            "&7白天:&6 25j/tick",
            "&7夜晚:&6 10j/tick"
    );
    //星辰太阳能发电机L2
    public static final SlimefunItemStack STARS_SOLAR_GENERATOR_L2 = new SlimefunItemStack(
            "STARS_SOLAR_GENERATOR_L2",
            Material.DAYLIGHT_DETECTOR,
            "&6星辰太阳能发电机 &4 II",
            "&7白天:&6 55j/tick",
            "&7夜晚:&6 25j/tick"
    );
    //星辰太阳能发电机L3
    public static final SlimefunItemStack STARS_SOLAR_GENERATOR_L3 = new SlimefunItemStack(
            "STARS_SOLAR_GENERATOR_L3",
            Material.DAYLIGHT_DETECTOR,
            "&6星辰太阳能发电机 &4 III",
            "&7白天:&6 120j/tick",
            "&7夜晚:&6 60j/tick"
    );
    //星辰太阳能发电机L4
    public static final SlimefunItemStack STARS_SOLAR_GENERATOR_L4 = new SlimefunItemStack(
            "STARS_SOLAR_GENERATOR_L4",
            Material.DAYLIGHT_DETECTOR,
            "&6星辰太阳能发电机 &4 IV",
            "&7白天:&6 245j/tick",
            "&7夜晚:&6 135j/tick"
    );
    //星辰太阳能发电机L5
    public static final SlimefunItemStack STARS_SOLAR_GENERATOR_L5 = new SlimefunItemStack(
            "STARS_SOLAR_GENERATOR_L5",
            Material.DAYLIGHT_DETECTOR,
            "&6星辰太阳能发电机 &4 V",
            "&7白天:&6 520j/tick",
            "&7夜晚:&6 255j/tick"
    );
    //星辰太阳能发电机-终极
    public static final SlimefunItemStack STARS_SOLAR_GENERATOR_ULTIMATE = new SlimefunItemStack(
            "STARS_SOLAR_GENERATOR_ULTIMATE",
            Material.DAYLIGHT_DETECTOR,
            "&6星辰太阳能发电机 &4 终极",
            "&7白天:&6 1024j/tick",
            "&7夜晚:&6 855j/tick"
    );
    //星辰发电机L1
    public static final SlimefunItemStack STARS_GENERATOR_L1 = new SlimefunItemStack(
            "STARS_GENERATOR_L1",
            Material.AMETHYST_BLOCK,
            "&6星辰发电机 &4 I"
    );
    //星辰发电机L2
    public static final SlimefunItemStack STARS_GENERATOR_L2 = new SlimefunItemStack(
            "STARS_GENERATOR_L2",
            Material.AMETHYST_BLOCK,
            "&6星辰发电机 &4 II"
    );
    //星辰发电机-终极
    public static final SlimefunItemStack STARS_GENERATOR_ULTIMATE = new SlimefunItemStack(
            "STARS_GENERATOR_ULTIMATE",
            Material.AMETHYST_BLOCK,
            "&6星辰发电机 &4 终极"
    );

    //机器

    //星辰矿机L1
    public static final SlimefunItemStack STARS_QUARRY_L1 = new SlimefunItemStack(
            "STARS_QUARRY_L1",
            Material.IRON_BLOCK,
            "&6星辰矿机 &4 I",
            "&7自动挖矿,可自动挖取星辰原矿"
    );
    //星辰矿机L2
    public static final SlimefunItemStack STARS_QUARRY_L2 = new SlimefunItemStack(
            "STARS_QUARRY_L2",
            Material.IRON_BLOCK,
            "&6星辰矿机 &4 II",
            "&7自动挖矿,可自动挖取星辰原矿"
    );
    //星辰矿机-终极
    public static final SlimefunItemStack STARS_QUARRY_ULTIMATE = new SlimefunItemStack(
            "STARS_QUARRY_ULTIMATE",
            Material.IRON_BLOCK,
            "&6星辰矿机 &4 终极",
            "&7自动挖矿,可自动挖取星辰原矿"
    );
    //星辰净化装置L1
    public static final SlimefunItemStack STARS_PURIFIER_L1 = new SlimefunItemStack(
            "STARS_PURIFIER_L1",
            Material.QUARTZ_BLOCK,
            "&6星辰净化装置 &4 I",
            "&7净化星辰废料"
    );
    //星辰净化装置L2
    public static final SlimefunItemStack STARS_PURIFIER_L2 = new SlimefunItemStack(
            "STARS_PURIFIER_L2",
            Material.QUARTZ_BLOCK,
            "&6星辰净化装置 &4 II",
            "&7净化星辰废料"
    );
    //星辰净化装置-终极
    public static final SlimefunItemStack STARS_PURIFIER_ULTIMATE = new SlimefunItemStack(
            "STARS_PURIFIER_ULTIMATE",
            Material.QUARTZ_BLOCK,
            "&6星辰净化装置 &4 终极",
            "&7净化星辰废料"
    );
    //星辰钻石生成器L1
    public static final SlimefunItemStack STARS_DIAMOND_QUARRY_L1 = new SlimefunItemStack(
            "STARS_DIAMOND_QUARRY_L1",
            Material.DIAMOND_BLOCK,
            "&6星辰钻石生成器 &4 I",
            "&7通过重组星辰源质置换钻石"
    );
    //星辰钻石生成器L2
    public static final SlimefunItemStack STARS_DIAMOND_QUARRY_L2 = new SlimefunItemStack(
            "STARS_DIAMOND_QUARRY_L2",
            Material.DIAMOND_BLOCK,
            "&6星辰钻石生成器 &4 II",
            "&7通过重组星辰源质置换钻石"
    );
    //星辰钻石生成器-终极
    public static final SlimefunItemStack STARS_DIAMOND_QUARRY_ULTIMATE = new SlimefunItemStack(
            "STARS_DIAMOND_QUARRY_ULTIMATE",
            Material.DIAMOND_BLOCK,
            "&6星辰钻石生成器 &4 终极",
            "&7通过重组星辰源质置换钻石"
    );
}