package me.xingchen.XingchenExpansion.util;

import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Util {
    private static final NamespacedKey SLIMEFUN_KEY = new NamespacedKey("slimefun", "slimefun_item");

    @Nullable
    public static String getSlimefunId(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.get(SLIMEFUN_KEY, PersistentDataType.STRING);
    }

    public static boolean protectOutputSlots(InventoryClickEvent event, Plugin plugin, int[] outputSlots, String validOutputId, Predicate<BlockMenu> menuValidator) {
        if (!(event.getInventory().getHolder() instanceof BlockMenu menu) || (menuValidator != null && !menuValidator.test(menu))) {
            return false;
        }

        int rawSlot = event.getRawSlot();
        if (!Arrays.stream(outputSlots).anyMatch(s -> s == rawSlot)) {
            return false;
        }

        String action = event.getAction().toString();
        XingchenExpansion.instance.getLogger().info("protectOutputSlots: slot=" + rawSlot + ", action=" + action);

        // 允许取出动作
        if (action.startsWith("PICKUP_") || action.startsWith("DROP_") || action.equals("MOVE_TO_OTHER_INVENTORY")) {
            return false; // 不取消取出
        }

        // 阻止非法存入
        ItemStack cursor = event.getCursor();
        String itemId = getSlimefunId(cursor);
        if (action.startsWith("PLACE_") || action.equals("SWAP_WITH_CURSOR")) {
            if (validOutputId == null || (itemId != null && !itemId.equals(validOutputId))) {
                event.setCancelled(true);
                return true; // 触发保存
            }
        }

        return false;
    }
    public static boolean protectOutputSlots(InventoryDragEvent event, Plugin plugin, int[] outputSlots, String validOutputId, BlockMenu menu) {
        if (menu == null || !(menu.getInventory().getHolder() instanceof BlockMenu) || !event.getInventory().equals(menu.getInventory())) {
            return false;
        }

        boolean shouldCancel = false;

        // 检查拖入输出槽的物品
        for (Map.Entry<Integer, ItemStack> entry : event.getNewItems().entrySet()) {
            int rawSlot = entry.getKey();
            ItemStack item = entry.getValue();

            // 仅处理输出槽
            if (!Arrays.stream(outputSlots).anyMatch(s -> s == rawSlot)) {
                continue;
            }

            String itemId = getSlimefunId(item);
            plugin.getLogger().info("protectOutputSlots (drag): slot=" + rawSlot + ", item=" + item + ", itemId=" + itemId);

            // 阻止非法物品（非 STARS_WASTE）
            if (validOutputId == null || (itemId != null && !itemId.equals(validOutputId))) {
                shouldCancel = true;
                plugin.getLogger().info("protectOutputSlots: 阻止拖入非法物品到输出槽: slot=" + rawSlot + ", itemId=" + itemId + ", expected=" + validOutputId);
            }
        }

        return shouldCancel;
    }
    private static int[] normalizeSlots(@Nonnull Object outputSlots) {
        if (outputSlots instanceof Integer) {
            return new int[]{(Integer) outputSlots};
        } else if (outputSlots instanceof int[]) {
            return (int[]) outputSlots;
        } else {
            throw new IllegalArgumentException("outputSlots 必须是 int 或 int[] 类型");
        }
    }

    private static boolean isValidOutput(@Nullable ItemStack item, @Nullable String validOutputId) {
        if (item == null || !item.hasItemMeta()) return false;
        if (validOutputId == null) return true;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String slimefunId = pdc.get(SLIMEFUN_KEY, PersistentDataType.STRING);
        return slimefunId != null && slimefunId.equals(validOutputId);
    }

    private static void returnItem(@Nonnull Player player, @Nonnull ItemStack item) {
        HashMap<Integer, ItemStack> unadded = player.getInventory().addItem(item);
        if (!unadded.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }
}