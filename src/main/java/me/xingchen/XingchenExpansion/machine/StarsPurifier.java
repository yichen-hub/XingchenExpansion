package me.xingchen.XingchenExpansion.machine;

import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.interfaces.InventoryBlock;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import me.xingchen.XingchenExpansion.item.Items;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 星辰净化器基类，将星辰废料转化为星辰源质。
 * 支持耗电量、转化时间、产出量的配置，预留系列扩展，兼容货运系统。
 */
public class StarsPurifier extends SlimefunItem implements EnergyNetComponent, InventoryBlock, Listener {
    private static final int INPUT_SLOT = 10;
    private static final int OUTPUT_SLOT = 16;
    private static final int STATUS_SLOT = 13;
    private static final int[] PROTECTED_SLOTS = {INPUT_SLOT, OUTPUT_SLOT, STATUS_SLOT};
    private static final ItemStack BACKGROUND_ITEM = new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "&7 ");
    private static final Set<Location> ACTIVE_PURIFIERS = ConcurrentHashMap.newKeySet();

    protected final int energyConsumption;
    protected final int processingTime;
    protected final int outputAmount;
    protected final XingchenExpansion plugin;
    protected final String purifierId;
    private BlockMenuPreset preset;

    public StarsPurifier(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, XingchenExpansion plugin, String purifierId) {
        super(itemGroup, item, recipeType, recipe);
        this.plugin = plugin;
        this.purifierId = purifierId;

        // 注册事件
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // 加载配置文件
        File file = new File(plugin.getDataFolder(), "purifier.yml");
        if (!file.exists()) plugin.saveResource("purifier.yml", false);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        var purifierSection = config.getConfigurationSection("purifiers." + purifierId);
        if (purifierSection == null) {
            plugin.getLogger().severe("未找到 purifiers." + purifierId + " 配置，净化器禁用！");
            this.energyConsumption = 0;
            this.processingTime = 0;
            this.outputAmount = 0;
            disable();
            return;
        }
        this.energyConsumption = purifierSection.getInt("energy_consumption", 50);
        this.processingTime = purifierSection.getInt("processing_time", 6000); // 5 分钟
        this.outputAmount = purifierSection.getInt("output_amount", 1);

        if (energyConsumption <= 0 || processingTime <= 0 || outputAmount <= 0) {
            plugin.getLogger().severe("无效的净化器配置，净化器禁用！");
            disable();
            return;
        }

        setupMenuPreset();
        addItemHandler(
                new BlockPlaceHandler(false) {
                    @Override
                    public void onPlayerPlace(BlockPlaceEvent event) {
                        Block block = event.getBlock();
                        Location loc = block.getLocation();
                        try {
                            if (BlockStorage.hasBlockInfo(loc)) {
                                String existingId = BlockStorage.getLocationInfo(loc, "id");
                                plugin.getLogger().info("检测到位置 " + loc + " 的现有 ID: " + existingId);
                                BlockStorage.clearBlockInfo(loc);
                                plugin.getLogger().info("清理位置 " + loc + " 的旧数据（原 ID: " + existingId + "）");
                            }
                            BlockStorage.addBlockInfo(block, "id", purifierId);
                            BlockStorage.addBlockInfo(block, "progress", "0");
                            BlockStorage.check(loc);
                            ACTIVE_PURIFIERS.add(loc.clone());
                            plugin.getLogger().info("成功放置净化器 " + purifierId + " 在 " + loc);
                        } catch (IllegalStateException e) {
                            plugin.getLogger().warning("无法注册 BlockStorage 数据，位置: " + loc + ", 错误: " + e.getMessage());
                            event.setCancelled(true);
                            event.getPlayer().sendMessage("§c无法放置净化器：位置冲突！");
                        }
                    }
                },
                new BlockUseHandler() {
                    @Override
                    public void onRightClick(PlayerRightClickEvent event) {
                        if (event.getPlayer().isSneaking()) return;
                        event.cancel();
                        BlockMenu menu = BlockStorage.getInventory(event.getClickedBlock().orElse(null));
                        if (menu != null) {
                            updateStatus(menu);
                            event.getPlayer().openInventory(menu.getInventory());
                        } else {
                            event.getClickedBlock().ifPresent(block ->
                                    plugin.getLogger().warning("无法打开 UI: " + block.getLocation() + ", 无 BlockMenu")
                            );
                        }
                    }
                },
                new SimpleBlockBreakHandler() {
                    @Override
                    public void onBlockBreak(@Nonnull Block block) {
                        BlockMenu menu = BlockStorage.getInventory(block);
                        if (menu != null) {
                            for (int slot : new int[]{INPUT_SLOT, OUTPUT_SLOT}) {
                                ItemStack content = menu.getItemInSlot(slot);
                                if (content != null) {
                                    block.getWorld().dropItemNaturally(block.getLocation(), content.clone());
                                }
                            }
                            menu.dropItems(block.getLocation(), INPUT_SLOT, OUTPUT_SLOT);
                        }
                        BlockStorage.clearBlockInfo(block);
                        ACTIVE_PURIFIERS.remove(block.getLocation());
                        plugin.getLogger().info("破坏净化器 " + purifierId + " 在 " + block.getLocation());
                    }
                }
        );

        // 定时任务
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Location loc : ACTIVE_PURIFIERS) {
                Block b = loc.getBlock();
                if (b.getType() != Material.SMITHING_TABLE || !BlockStorage.hasBlockInfo(loc)) {
                    ACTIVE_PURIFIERS.remove(loc);
                    BlockStorage.clearBlockInfo(loc);
                    plugin.getLogger().info("移除无效净化器位置: " + loc);
                    continue;
                }
                tick(b);
            }
        }, 0L, 1L);
    }

    protected void setupMenuPreset() {
        preset = new BlockMenuPreset(purifierId, "星辰净化器") {
            @Override
            public void init() {
                for (int i = 0; i < 27; i++) {
                    int finalI = i;
                    if (!Arrays.stream(PROTECTED_SLOTS).anyMatch(s -> s == finalI)) {
                        addItem(i, BACKGROUND_ITEM, (p, s, item, action) -> false);
                    }
                }
                addMenuClickHandler(INPUT_SLOT, (p, s, item, action) -> true);
                addMenuClickHandler(OUTPUT_SLOT, (p, s, item, action) -> true);
                addMenuClickHandler(STATUS_SLOT, (p, s, item, action) -> false);
            }

            @Override
            public boolean canOpen(Block b, Player p) {
                return true;
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                return flow == ItemTransportFlow.INSERT ? new int[]{INPUT_SLOT} : new int[]{OUTPUT_SLOT};
            }
        };
        preset.setPlayerInventoryClickable(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BlockMenu menu) || !menu.getPreset().getID().equals(purifierId)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        int rawSlot = event.getRawSlot();
        org.bukkit.event.inventory.ClickType click = event.getClick();
        Inventory clickedInventory = event.getClickedInventory();
        boolean isBlockMenu = clickedInventory != null && clickedInventory.equals(menu.toInventory());
        boolean isPlayerInventory = clickedInventory != null && clickedInventory.equals(player.getInventory());

        // 背包 Shift+左键：只允许 STARS_WASTE 进入输入槽
        if (isPlayerInventory && click == ClickType.SHIFT_LEFT && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            ItemStack current = event.getCurrentItem();
            if (current != null && current.getType() != Material.AIR) {
                ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
                boolean canInsertInput = inputItem == null || inputItem.getType() == Material.AIR ||
                        (inputItem.isSimilar(Items.STARS_WASTE) && inputItem.getAmount() < inputItem.getMaxStackSize());
                if (!canInsertInput || !current.isSimilar(Items.STARS_WASTE)) {
                    event.setCancelled(true);
                    player.sendMessage("§c输入槽只能存入星辰废料！");
                    return;
                }
            }
        }

        // 输入槽：只允许 STARS_WASTE
        if (rawSlot == INPUT_SLOT && isBlockMenu) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR && !cursor.isSimilar(Items.STARS_WASTE)) {
                event.setCancelled(true);
                player.sendMessage("§c输入槽只能存入星辰废料！");
            }
            return;
        }

        // 输出槽：阻止存入
        if (rawSlot == OUTPUT_SLOT && isBlockMenu) {
            ItemStack cursor = event.getCursor();
            boolean isInsert = (cursor != null && cursor.getType() != Material.AIR) ||
                    (click == ClickType.SHIFT_LEFT && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) ||
                    click.isKeyboardClick();
            if (isInsert) {
                event.setCancelled(true);
                player.sendMessage("§c输出槽不支持存入！");
            }
        }
    }

    protected void updateStatus(BlockMenu menu) {
        Location loc = menu.getLocation();
        ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
        ItemStack outputItem = menu.getItemInSlot(OUTPUT_SLOT);
        String statusText = "&c等待输入";
        int progress = BlockStorage.getLocationInfo(loc, "progress") != null
                ? Integer.parseInt(BlockStorage.getLocationInfo(loc, "progress"))
                : 0;

        if (inputItem != null && inputItem.isSimilar(Items.STARS_WASTE) &&
                (outputItem == null || (outputItem.isSimilar(Items.STARS_SOURCE_QUALITY) && outputItem.getAmount() < outputItem.getMaxStackSize())) &&
                getCharge(loc) >= energyConsumption) {
            statusText = "&a运行中";
        }

        ItemStack status = new ItemStack(Material.REDSTONE);
        ItemMeta meta = status.getItemMeta();
        meta.setDisplayName("§c耗电: " + energyConsumption + " J/tick");
        meta.setLore(Arrays.asList(
                "§7状态: " + statusText,
                "§7进度: " + (progress * 100 / processingTime) + "%"
        ));
        status.setItemMeta(meta);
        menu.replaceExistingItem(STATUS_SLOT, status);
    }

    protected void tick(Block b) {
        Location loc = b.getLocation();
        BlockMenu menu = BlockStorage.getInventory(b);
        if (menu == null) {
            plugin.getLogger().warning("无效 BlockMenu: " + loc);
            return;
        }

        ItemStack input = menu.getItemInSlot(INPUT_SLOT);
        ItemStack output = menu.getItemInSlot(OUTPUT_SLOT);
        int charge = getCharge(loc);

        if (input == null || !input.isSimilar(Items.STARS_WASTE) ||
                (output != null && (!output.isSimilar(Items.STARS_SOURCE_QUALITY) || output.getAmount() >= output.getMaxStackSize())) ||
                charge < energyConsumption) {
            if (BlockStorage.getLocationInfo(loc, "progress") != null && !BlockStorage.getLocationInfo(loc, "progress").equals("0")) {
                BlockStorage.addBlockInfo(loc, "progress", "0");
                updateStatus(menu);
            }
            return;
        }

        removeCharge(loc, energyConsumption);
        int progress = BlockStorage.getLocationInfo(loc, "progress") != null
                ? Integer.parseInt(BlockStorage.getLocationInfo(loc, "progress"))
                : 0;
        progress++;

        if (progress >= processingTime) {
            input.setAmount(input.getAmount() - 1);
            if (output == null) {
                menu.replaceExistingItem(OUTPUT_SLOT, new CustomItemStack(Items.STARS_SOURCE_QUALITY, outputAmount));
            } else {
                output.setAmount(output.getAmount() + outputAmount);
            }
            progress = 0;
        }

        BlockStorage.addBlockInfo(loc, "progress", String.valueOf(progress));
        updateStatus(menu);
    }

    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        return energyConsumption * 2;
    }

    @Override
    public int[] getInputSlots() {
        return new int[]{INPUT_SLOT};
    }

    @Override
    public int[] getOutputSlots() {
        return new int[]{OUTPUT_SLOT};
    }
}