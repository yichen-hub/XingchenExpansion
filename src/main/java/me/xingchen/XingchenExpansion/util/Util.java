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

    @Nullable
    public static String getSlimefunId(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.get(SLIMEFUN_KEY, PersistentDataType.STRING);
    }
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

            if (!isBlockMenu || !Arrays.stream(slots).anyMatch(s -> s == rawSlot)) {
                return false;
            }

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
                }

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    for (int outputSlot : slots) {
                        ItemStack outputItem = menu.getItemInSlot(outputSlot);
                        if (outputItem != null && outputItem.getType() != Material.AIR &&
                                !isValidOutput(outputItem, validOutputId)) {
                            returnItem(player, outputItem.clone());
                            menu.replaceExistingItem(outputSlot, null);
                            player.sendMessage("§c输出槽不支持存入！");
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
            if (dragEvent.getRawSlots().stream().noneMatch(slot -> Arrays.stream(slots).anyMatch(s -> s == slot))) {
                return false;
            }

            dragEvent.setCancelled(true);
            ItemStack cursor = dragEvent.getOldCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                returnItem(player, cursor.clone());
                dragEvent.setCursor(null);
                player.sendMessage("§c输出槽不支持存入！");
            }
            return true;
        } else {
            throw new IllegalArgumentException("不支持的事件类型: " + event.getClass().getName());
        }
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