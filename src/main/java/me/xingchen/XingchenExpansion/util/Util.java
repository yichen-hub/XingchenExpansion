package me.xingchen.XingchenExpansion.util;

import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;

public class Util {
    private static final NamespacedKey SLIMEFUN_KEY = new NamespacedKey("slimefun", "slimefun_item");

    //获取物品的 Slimefun ID。
    @Nullable
    public static String getSlimefunId(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.get(SLIMEFUN_KEY, PersistentDataType.STRING);
    }
    /* 保护输出槽，处理 InventoryClickEvent 和 InventoryDragEvent，阻止非法存入。
     * @param event        库存事件（InventoryClickEvent 或 InventoryDragEvent）
     * @param plugin       插件实例，用于调度任务和日志
     * @param outputSlots  输出槽位（int 或 int[]）
     * @param validOutputId 有效输出物品的 Slimefun ID（可为 null，表示无限制）
     * @param isValidMenu  判断菜单是否为目标机器的 Predicate
     * @return 是否处理了事件（true 表示事件已处理，需取消默认行为）
     * @throws IllegalArgumentException 如果事件类型不支持*/
    public static boolean protectOutputSlots(@Nonnull InventoryEvent event, @Nonnull JavaPlugin plugin,
                                             @Nonnull Object outputSlots, @Nullable String validOutputId,
                                             @Nonnull Predicate<BlockMenu> isValidMenu) {
        if (!(event.getInventory().getHolder() instanceof BlockMenu menu) || !isValidMenu.test(menu)) {
            return false;
        }

        int[] slots = normalizeSlots(outputSlots);
        Player player = (Player) event.getView().getPlayer();

        if (event instanceof InventoryClickEvent clickEvent) {
            int rawSlot = clickEvent.getRawSlot();
            org.bukkit.event.inventory.ClickType click = clickEvent.getClick();
            Inventory clickedInventory = clickEvent.getClickedInventory();
            boolean isBlockMenu = clickedInventory != null && clickedInventory.equals(menu.toInventory());

            // 检查是否为输出槽
            if (!isBlockMenu || !Arrays.stream(slots).anyMatch(s -> s == rawSlot)) {
                return false;
            }

            // 处理点击交互
            ItemStack cursor = clickEvent.getCursor();
            boolean isInsert = (cursor != null && cursor.getType() != Material.AIR) ||
                    (click == ClickType.SHIFT_LEFT && clickEvent.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) ||
                    click.isKeyboardClick();

            if (isInsert) {
                clickEvent.setCancelled(true);
                ItemStack item = cursor != null && cursor.getType() != Material.AIR ? cursor :
                        click.isKeyboardClick() ? player.getInventory().getItem(clickEvent.getHotbarButton()) : null;

                if (item != null && item.getType() != Material.AIR) {
                    returnItem(player, item.clone());
                    if (cursor != null && cursor.getType() != Material.AIR) {
                        player.setItemOnCursor(null);
                    } else if (click.isKeyboardClick()) {
                        player.getInventory().setItem(clickEvent.getHotbarButton(), null);
                    }
                    player.sendMessage("§c输出槽不支持存入！");
                    plugin.getLogger().info("阻止存入输出槽: 物品=" + item.getType() + ", 槽位=" + rawSlot);
                }

                // 延迟检查输出槽中的非法物品
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    for (int outputSlot : slots) {
                        ItemStack outputItem = menu.getItemInSlot(outputSlot);
                        if (outputItem != null && outputItem.getType() != Material.AIR &&
                                !isValidOutput(outputItem, validOutputId)) {
                            returnItem(player, outputItem.clone());
                            menu.replaceExistingItem(outputSlot, null);
                            player.sendMessage("§c输出槽不支持存入！");
                            plugin.getLogger().info("延迟检查移除非法物品: 输出槽=" + outputSlot + ", 物品=" + outputItem.getType());
                        }
                    }
                }, 2L);
                return true;
            } else if (click != ClickType.LEFT && click != ClickType.SHIFT_LEFT &&
                    click != ClickType.NUMBER_KEY && click != ClickType.DROP) {
                clickEvent.setCancelled(true);
                player.sendMessage("§c输出槽只能左键、Shift+左键、数字键或丢弃键取出！");
                return true;
            }
            return false;
        } else if (event instanceof InventoryDragEvent dragEvent) {
            // 检查是否涉及输出槽
            if (dragEvent.getRawSlots().stream().noneMatch(slot -> Arrays.stream(slots).anyMatch(s -> s == slot))) {
                return false;
            }

            dragEvent.setCancelled(true);
            ItemStack cursor = dragEvent.getOldCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                returnItem(player, cursor.clone());
                dragEvent.setCursor(null);
                player.sendMessage("§c输出槽不支持存入！");
                plugin.getLogger().info("阻止拖拽存入输出槽: 物品=" + cursor.getType());
            }
            return true;
        } else {
            throw new IllegalArgumentException("不支持的事件类型: " + event.getClass().getName());
        }
    }

    /**
     * 规范化输出槽位参数，转换为 int[]。
     *
     * @param outputSlots 输出槽位（int 或 int[]）
     * @return 规范化后的槽位数组
     * @throws IllegalArgumentException 如果参数类型无效
     */
    private static int[] normalizeSlots(@Nonnull Object outputSlots) {
        if (outputSlots instanceof Integer) {
            return new int[]{(Integer) outputSlots};
        } else if (outputSlots instanceof int[]) {
            return (int[]) outputSlots;
        } else {
            throw new IllegalArgumentException("outputSlots 必须是 int 或 int[] 类型");
        }
    }

    /**
     * 检查物品是否为有效输出物品。
     *
     * @param item         物品
     * @param validOutputId 有效输出物品的 Slimefun ID（可为 null，表示无限制）
     * @return 是否为有效输出物品
     */
    private static boolean isValidOutput(@Nullable ItemStack item, @Nullable String validOutputId) {
        if (item == null || !item.hasItemMeta()) return false;
        if (validOutputId == null) return true;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String slimefunId = pdc.get(SLIMEFUN_KEY, PersistentDataType.STRING);
        return slimefunId != null && slimefunId.equals(validOutputId);
    }

    /**
     * 将物品返回给玩家背包，若背包满则掉落在玩家位置。
     *
     * @param player 玩家
     * @param item   物品
     */
    private static void returnItem(@Nonnull Player player, @Nonnull ItemStack item) {
        HashMap<Integer, ItemStack> unadded = player.getInventory().addItem(item);
        if (!unadded.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }
}