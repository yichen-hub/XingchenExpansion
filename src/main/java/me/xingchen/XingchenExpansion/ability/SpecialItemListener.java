package me.xingchen.XingchenExpansion.ability;

import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpecialItemListener implements Listener {
    private final JavaPlugin plugin;
    private final BuffManager buffManager;
    private final Map<UUID, Long> useCooldowns = new HashMap<>();
    private final Map<String, SpecialItem> specialItems = new HashMap<>();

    // 特殊物品配置
    private record SpecialItem(String buffId, long buffDuration) {}

    public SpecialItemListener(JavaPlugin plugin, BuffManager buffManager) {
        this.plugin = plugin;
        this.buffManager = buffManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // 注册物品
        specialItems.put("MANIFOLD", new SpecialItem("MANIFOLD", 15 * 60 * 20L)); // 15 分钟 Buff
        specialItems.put("WEAVING", new SpecialItem("WEAVING", 15 * 60 * 20L));
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getAmount() < 1) {
            return;
        }

        SlimefunItem slimefunItem = SlimefunItem.getByItem(item);
        if (slimefunItem == null || !specialItems.containsKey(slimefunItem.getId())) {
            return;
        }

        String itemId = slimefunItem.getId();
        SpecialItem specialItem = specialItems.get(itemId);
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // 检查使用冷却（1 秒）
        if (useCooldowns.containsKey(uuid) && currentTime - useCooldowns.get(uuid) < 1000) {
            player.sendMessage("§c请稍后再使用 " + itemId + "！");
            return;
        }

        // 检查 Buff 是否已存在
        if (buffManager.hasBuff(player, specialItem.buffId)) {
            player.sendMessage("§c" + specialItem.buffId + " 效果已在激活中！");
            return;
        }

        // 消耗物品，添加 Buff
        item.setAmount(item.getAmount() - 1);
        useCooldowns.put(uuid, currentTime);
        buffManager.addBuff(player, specialItem.buffId, specialItem.buffDuration);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.5F, 1.0F);
        event.cancel();
    }
}