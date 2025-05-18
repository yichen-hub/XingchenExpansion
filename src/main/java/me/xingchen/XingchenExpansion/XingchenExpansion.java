package me.xingchen.XingchenExpansion;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.groups.NestedItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup;
import me.xingchen.XingchenExpansion.ability.BuffManager;
import me.xingchen.XingchenExpansion.ability.buff.ManifoldBuff;
import me.xingchen.XingchenExpansion.ability.SpecialItemListener;
import me.xingchen.XingchenExpansion.ability.buff.WeavingBuff;
import me.xingchen.XingchenExpansion.item.ItemRegistry;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;

public class XingchenExpansion extends JavaPlugin implements SlimefunAddon {
    public static XingchenExpansion instance;
    public static ItemGroup XINGCHEN_GROUP;
    private BuffManager buffManager;
    //物品组声明
    public static NestedItemGroup PARENT_GROUP;
    public static ItemGroup MACHINE_GROUP;
    public static ItemGroup MATERIAL_GROUP;
    public static ItemGroup ABILITY_ITEM_GROUP;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Enabling XingchenExpansion v1.0.0");
        try {

            // 创建物品组
            PARENT_GROUP = new NestedItemGroup(
                    new NamespacedKey(this, "xingchenexpansion"),
                    new SlimefunItemStack("XINGCHEN_PARENT", Material.AMETHYST_BLOCK, "&b星辰拓展"),
                    2
            );
            MACHINE_GROUP = new SubItemGroup(
                    new NamespacedKey(this, "xingchen_machines"),
                    PARENT_GROUP,
                    new SlimefunItemStack("XINGCHEN_MACHINES", Material.CHISELED_QUARTZ_BLOCK, "&e机器","包含星辰拓展的所有机器")
            );
            MATERIAL_GROUP = new SubItemGroup(
                    new NamespacedKey(this, "xingchen_materials"),
                    PARENT_GROUP,
                    new SlimefunItemStack("XINGCHEN_MATERIALS", Material.AMETHYST_SHARD, "&a材料","包含星辰拓展的所有材料")
            );
            ABILITY_ITEM_GROUP = new SubItemGroup(
                    new NamespacedKey(this, "xingchen_ability_items"),
                    PARENT_GROUP,
                    new SlimefunItemStack("XINGCHEN_ABILITIES_ITEMS", Material.ECHO_SHARD, "&6技能物品","包含星辰拓展的所有技能物品")
            );

            // 注册所有物品
            ItemRegistry.register(this);
            // buff管理系统注册
            buffManager = new BuffManager(this);
            new SpecialItemListener(this, buffManager);
            // buff事件注册
            new ManifoldBuff(this, buffManager);
            new WeavingBuff(this, buffManager);

        } catch (Exception e) {
            getLogger().severe("出现错误,XingchenExpansion取消加载!: " + e.getMessage());
            e.printStackTrace();
            setEnabled(false);
        }
    }
    @Override
    public void onDisable() {
        buffManager.saveAllBuffs();
        // 禁用插件的逻辑...
    }
    public static XingchenExpansion getInstance() {
        if (instance == null) {
            throw new IllegalStateException("XingchenExpansion 实例未初始化！");
        }
        return instance;
    }

    @Override
    public String getBugTrackerURL() {
        // 你可以在这里返回你的问题追踪器的网址，而不是 null
        return null;
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        /*
         * 你需要返回对你插件的引用。
         * 如果这是你插件的主类，只需要返回 "this" 即可。
         */
        return this;
    }
}
