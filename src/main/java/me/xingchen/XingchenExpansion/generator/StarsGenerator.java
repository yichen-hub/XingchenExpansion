package me.xingchen.XingchenExpansion.generator;


import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.interfaces.InventoryBlock;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import me.xingchen.XingchenExpansion.item.Items;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StarsGenerator extends SlimefunItem implements EnergyNetProvider, InventoryBlock, EnergyNetComponent, Listener {
    private static final int INPUT_SLOT = 10, STATUS_SLOT = 13;
    private static final int[] OUTPUT_SLOTS = {14, 15, 16}, PROTECTED_SLOTS = {INPUT_SLOT, STATUS_SLOT, 14, 15, 16};
    private static final ItemStack DECORATION = new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "&7 ");
    private static final ItemStack OUTPUT_PLACEHOLDER = new CustomItemStack(Material.GREEN_STAINED_GLASS_PANE, "&a输出槽", "&7仅限取出物品");
    private static final Set<String> FUEL_IDS = Set.of("STARS_ORE", "STARS_INGOT", "STARS_CRYSTAL");
    private static final Map<String, SlimefunItemStack> WASTE_ITEMS = Map.of("STARS_WASTE", Items.STARS_WASTE);
    private static final NamespacedKey SLIMEFUN_KEY = new NamespacedKey("slimefun", "slimefun_item");
    private static final String BURNING_TIME_KEY = "burning_time";
    private static final String FUEL_ID_KEY = "fuel_id";

    private final int energyCapacity;
    private final Map<String, FuelConfig> fuels;
    private final XingchenExpansion plugin;
    private BlockMenuPreset preset;
    private final Set<Location> generatorLocations = ConcurrentHashMap.newKeySet();

    private static class FuelConfig {
        final int energyPerTick;
        final int burnTime;
        final int wasteAmount;

        FuelConfig(int energyPerTick, int burnTime, int wasteAmount) {
            this.energyPerTick = energyPerTick;
            this.burnTime = burnTime;
            this.wasteAmount = wasteAmount;
        }
    }

    public StarsGenerator(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, XingchenExpansion plugin) {
        super(itemGroup, item, recipeType, recipe);
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        File file = new File(plugin.getDataFolder(), "generator.yml");
        if (!file.exists()) plugin.saveResource("generator.yml", false);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection generatorSection = config.getConfigurationSection("generators.STARS_GENERATOR");
        if (generatorSection == null) {
            plugin.getLogger().severe("未找到 generators.STARS_GENERATOR 配置，发电机禁用！");
            this.energyCapacity = 0;
            this.fuels = new HashMap<>();
            disable();
            return;
        }
        this.energyCapacity = generatorSection.getInt("energy_capacity", 1000);

        this.fuels = new HashMap<>();
        ConfigurationSection fuelsSection = config.getConfigurationSection("fuels");
        if (fuelsSection == null) {
            plugin.getLogger().severe("未找到 fuels 配置，发电机禁用！");
            disable();
            return;
        }
        for (String fuelId : fuelsSection.getKeys(false)) {
            int energy = fuelsSection.getInt(fuelId + ".energy", 0);
            int burnTime = fuelsSection.getInt(fuelId + ".burn_time", 200);
            int waste = fuelsSection.getInt(fuelId + ".waste.STARS_WASTE", 0);
            if (energy > 0 && burnTime > 0) {
                fuels.put(fuelId, new FuelConfig(energy, burnTime, waste));
            }
        }
        if (fuels.isEmpty()) {
            plugin.getLogger().severe("未加载任何有效燃料配置，发电机禁用！");
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
                                plugin.getLogger().warning("位置 " + loc + " 已存在 Slimefun 数据: " + existingId);
                                if ("STARS_GENERATOR".equals(existingId)) {
                                    generatorLocations.add(loc.clone());
                                    return;
                                } else {
                                    event.setCancelled(true);
                                    event.getPlayer().sendMessage("§c此位置已被其他 Slimefun 方块占用！");
                                    return;
                                }
                            }
                            BlockStorage.addBlockInfo(block, "id", "STARS_GENERATOR");
                            generatorLocations.add(loc.clone());
                            plugin.getLogger().info("发电机放置: " + loc);
                        } catch (IllegalStateException e) {
                            plugin.getLogger().warning("无法注册 BlockStorage 数据，位置: " + loc + ", 错误: " + e.getMessage());
                            event.setCancelled(true);
                            event.getPlayer().sendMessage("§c无法放置发电机：位置冲突！");
                        }
                    }
                },
                new BlockUseHandler() {
                    @Override
                    public void onRightClick(PlayerRightClickEvent event) {
                        Optional<Block> blockOpt = event.getClickedBlock();
                        if (blockOpt.isEmpty()) return;
                        BlockMenu menu = BlockStorage.getInventory(blockOpt.get());
                        if (menu == null) return;
                        updateStatus(menu);
                        event.getPlayer().openInventory(menu.getInventory());
                    }
                },
                new SimpleBlockBreakHandler() {
                    @Override
                    public void onBlockBreak(@Nonnull Block block) {
                        BlockMenu menu = BlockStorage.getInventory(block);
                        if (menu != null) {
                            for (int slot : new int[]{INPUT_SLOT, 14, 15, 16}) {
                                ItemStack content = menu.getItemInSlot(slot);
                                if (content != null && content.getType() != Material.GREEN_STAINED_GLASS_PANE) {
                                    block.getWorld().dropItemNaturally(block.getLocation(), content.clone());
                                }
                            }
                        }
                        BlockStorage.clearBlockInfo(block);
                        generatorLocations.remove(block.getLocation());
                        plugin.getLogger().info("发电机破坏: " + block.getLocation());
                    }
                }
        );
    }

    private void setupMenuPreset() {
        preset = new BlockMenuPreset("STARS_GENERATOR", "星辰发电机") {
            @Override
            public void init() {
                for (int i = 0; i < 27; i++) {
                    int finalI = i;
                    if (!Arrays.stream(PROTECTED_SLOTS).anyMatch(s -> s == finalI)) {
                        addItem(i, DECORATION, (p, s, item, action) -> false);
                    }
                }
                for (int slot : OUTPUT_SLOTS) {
                    addItem(slot, OUTPUT_PLACEHOLDER, (p, s, item, action) -> false);
                }
                addMenuClickHandler(STATUS_SLOT, (p, s, item, action) -> false);
            }

            @Override
            public boolean canOpen(Block b, Player p) {
                return true;
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                if (flow == ItemTransportFlow.INSERT) return new int[]{INPUT_SLOT};
                plugin.getLogger().info("货运访问，发电机位置数量: " + generatorLocations.size());
                List<Integer> validSlots = new ArrayList<>();
                for (Location loc : generatorLocations) {
                    plugin.getLogger().info("检查发电机位置: " + loc);
                    BlockMenu menu = BlockStorage.getInventory(loc.getBlock());
                    if (menu != null && menu.getPreset().getID().equals("STARS_GENERATOR")) {
                        for (int slot : OUTPUT_SLOTS) {
                            ItemStack item = menu.getItemInSlot(slot);
                            if (item != null && item.getType() != Material.GREEN_STAINED_GLASS_PANE) {
                                validSlots.add(slot);
                            }
                            // 检查货运后槽位状态
                            if (item == null || item.getType() == Material.AIR || item.getAmount() == 0) {
                                plugin.getLogger().info("货运清空槽位 " + slot + "，恢复占位玻璃板");
                                menu.replaceExistingItem(slot, OUTPUT_PLACEHOLDER.clone());
                            }
                        }
                        plugin.getLogger().info("货运有效槽位: " + validSlots);
                        return validSlots.stream().mapToInt(Integer::intValue).toArray();
                    } else {
                        plugin.getLogger().warning("BlockMenu 获取失败，位置: " + loc + ", BlockStorage ID: " + BlockStorage.getLocationInfo(loc, "id"));
                    }
                }
                plugin.getLogger().warning("无法找到匹配的发电机 BlockMenu，货运无法访问输出槽");
                return new int[0];
            }
        };
        preset.setPlayerInventoryClickable(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BlockMenu menu) || !menu.getPreset().getID().equals("STARS_GENERATOR")) {
            return;
        }
        if (event.getClickedInventory() != menu.toInventory()) {
            return;
        }
        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();

        // 输入槽：只允许燃料
        if (slot == INPUT_SLOT) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                String fuelId = getFuelId(cursor);
                if (fuelId == null) {
                    event.setCancelled(true);
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        player.setItemOnCursor(null);
                        returnItem(player, cursor.clone());
                        player.sendMessage("§c只能放入星辰燃料！");
                    });
                }
            }
            return;
        }

        // 输出槽：手动处理取出
        if (Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == slot)) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item == null || item.getType() == Material.GREEN_STAINED_GLASS_PANE) {
                event.setCancelled(true);
                plugin.getLogger().info("玩家尝试点击空槽或占位玻璃板，槽位: " + slot);
                return;
            }

            // 允许的点击类型：LEFT, SHIFT_LEFT, NUMBER_KEY, DROP
            org.bukkit.event.inventory.ClickType click = event.getClick();
            if (click != org.bukkit.event.inventory.ClickType.LEFT &&
                    click != org.bukkit.event.inventory.ClickType.SHIFT_LEFT &&
                    click != org.bukkit.event.inventory.ClickType.NUMBER_KEY &&
                    click != org.bukkit.event.inventory.ClickType.DROP) {
                event.setCancelled(true);
                plugin.getLogger().info("玩家使用无效点击类型: " + click + "，槽位: " + slot);
                player.sendMessage("§c输出槽只能左键、Shift+左键、数字键或丢弃键取出！");
                return;
            }

            // 记录点击详情
            plugin.getLogger().info("玩家点击输出槽，槽位: " + slot + "，点击类型: " + click + "，物品: " + item.getType());

            // 手动移动物品到玩家背包
            event.setCancelled(true); // 防止 Slimefun 拦截
            ItemStack itemClone = item.clone();
            HashMap<Integer, ItemStack> unadded = player.getInventory().addItem(itemClone);
            if (unadded.isEmpty()) {
                // 成功添加到背包，清空槽位
                menu.replaceExistingItem(slot, OUTPUT_PLACEHOLDER.clone());
                plugin.getLogger().info("玩家成功取出物品，槽位: " + slot + "，恢复占位玻璃板");
            } else {
                // 背包满，掉落物品
                player.getWorld().dropItemNaturally(player.getLocation(), itemClone);
                menu.replaceExistingItem(slot, OUTPUT_PLACEHOLDER.clone());
                plugin.getLogger().info("玩家背包满，物品掉落，槽位: " + slot + "，恢复占位玻璃板");
            }
        }
    }

    private void returnItem(Player player, ItemStack item) {
        if (player.getInventory().addItem(item).isEmpty()) return;
        player.getWorld().dropItemNaturally(player.getLocation(), item);
    }

    private String getFuelId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String slimefunId = pdc.get(SLIMEFUN_KEY, PersistentDataType.STRING);
        return slimefunId != null && FUEL_IDS.contains(slimefunId) ? slimefunId : null;
    }

    private void updateStatus(BlockMenu menu) {
        ItemStack item = menu.getItemInSlot(INPUT_SLOT);
        int energy = 0;
        String fuelId = getFuelId(item);
        if (fuelId != null) {
            FuelConfig config = fuels.get(fuelId);
            if (config != null) energy = config.energyPerTick;
        }

        ItemStack status = menu.getItemInSlot(STATUS_SLOT);
        if (status == null) {
            status = new ItemStack(Material.REDSTONE);
            ItemMeta meta = status.getItemMeta();
            meta.setDisplayName("§c能量: 0 J/tick");
            status.setItemMeta(meta);
            menu.addItem(STATUS_SLOT, status, (p, s, i, a) -> false);
        }
        ItemMeta meta = status.getItemMeta();
        meta.setDisplayName("§c能量: " + energy + " J/tick");
        status.setItemMeta(meta);
        menu.replaceExistingItem(STATUS_SLOT, status);
    }

    @Override
    public int getGeneratedOutput(@Nonnull Location location, @Nonnull Config data) {
        BlockMenu menu = BlockStorage.getInventory(location.getBlock());
        if (menu == null) {
            try {
                BlockStorage.addBlockInfo(location, "id", "STARS_GENERATOR");
                menu = new BlockMenu(preset, location);
                ItemStack status = new ItemStack(Material.REDSTONE);
                ItemMeta meta = status.getItemMeta();
                meta.setDisplayName("§c能量: 0 J/tick");
                status.setItemMeta(meta);
                menu.addItem(STATUS_SLOT, status, (p, s, i, a) -> false);
                for (int slot : OUTPUT_SLOTS) {
                    menu.replaceExistingItem(slot, OUTPUT_PLACEHOLDER.clone());
                }
                generatorLocations.add(location.clone());
                plugin.getLogger().info("发电机初始化: " + location);
            } catch (IllegalStateException e) {
                plugin.getLogger().warning("无法初始化 BlockStorage 数据，位置: " + location + ", 错误: " + e.getMessage());
                return 0;
            }
        }

        String burningTimeStr = BlockStorage.getLocationInfo(location, BURNING_TIME_KEY);
        int burningTime = burningTimeStr != null ? Integer.parseInt(burningTimeStr) : 0;

        if (burningTime > 0) {
            burningTime--;
            BlockStorage.addBlockInfo(location, BURNING_TIME_KEY, String.valueOf(burningTime));
            String fuelId = BlockStorage.getLocationInfo(location, FUEL_ID_KEY);
            FuelConfig config = fuelId != null ? fuels.get(fuelId) : null;
            updateStatus(menu);
            return config != null ? config.energyPerTick : 0;
        }

        ItemStack item = menu.getItemInSlot(INPUT_SLOT);
        if (item == null) {
            BlockStorage.addBlockInfo(location, BURNING_TIME_KEY, null);
            BlockStorage.addBlockInfo(location, FUEL_ID_KEY, null);
            updateStatus(menu);
            return 0;
        }

        String fuelId = getFuelId(item);
        if (fuelId == null) {
            updateStatus(menu);
            return 0;
        }

        FuelConfig fuelConfig = fuels.get(fuelId);
        if (fuelConfig == null) {
            updateStatus(menu);
            return 0;
        }

        int newAmount = item.getAmount() - 1;
        ItemStack newItem = newAmount > 0 ? item.clone() : null;
        if (newItem != null) newItem.setAmount(newAmount);
        menu.replaceExistingItem(INPUT_SLOT, newItem);

        BlockStorage.addBlockInfo(location, BURNING_TIME_KEY, String.valueOf(fuelConfig.burnTime));
        BlockStorage.addBlockInfo(location, FUEL_ID_KEY, fuelId);

        if (fuelConfig.wasteAmount > 0) {
            ItemStack waste = WASTE_ITEMS.get("STARS_WASTE").clone();
            waste.setAmount(fuelConfig.wasteAmount);
            for (int slot : OUTPUT_SLOTS) {
                ItemStack existing = menu.getItemInSlot(slot);
                if (existing == null || existing.getType() == Material.GREEN_STAINED_GLASS_PANE) {
                    menu.replaceExistingItem(slot, waste.clone());
                    break;
                } else if (existing.isSimilar(waste)) {
                    int totalAmount = existing.getAmount() + waste.getAmount();
                    if (totalAmount <= existing.getMaxStackSize()) {
                        existing.setAmount(totalAmount);
                        menu.replaceExistingItem(slot, existing);
                        break;
                    } else {
                        existing.setAmount(existing.getMaxStackSize());
                        waste.setAmount(totalAmount - existing.getMaxStackSize());
                        menu.replaceExistingItem(slot, existing);
                    }
                }
            }
        }

        updateStatus(menu);
        return fuelConfig.energyPerTick;
    }

    @Override
    public int getCapacity() {
        return energyCapacity;
    }

    @Override
    public int[] getInputSlots() {
        return new int[]{INPUT_SLOT};
    }

    @Override
    public int[] getOutputSlots() {
        return OUTPUT_SLOTS;
    }

    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.GENERATOR;
    }

    @Override
    public boolean isChargeable() {
        return true;
    }
}