package me.xingchen.XingchenExpansion;

import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.xingchen.XingchenExpansion.item.ItemRegistry;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;

public class XingchenExpansion extends JavaPlugin implements SlimefunAddon {
    public static XingchenExpansion instance;
    public static ItemGroup XINGCHEN_GROUP;
    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Enabling XingchenExpansion v1.0.0");
        try {
            // 初始化物品组
            XINGCHEN_GROUP = new ItemGroup(
                    new NamespacedKey(this, "xingchen"),
                    new CustomItemStack(Material.AMETHYST_BLOCK, "&6星辰拓展"),
                    1
            );
            // 注册所有物品
            ItemRegistry.register(this);
        } catch (Exception e) {
            getLogger().severe("Failed to enable XingchenExpansion: " + e.getMessage());
            e.printStackTrace();
            setEnabled(false);
        }
    }
    @Override
    public void onDisable() {
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
