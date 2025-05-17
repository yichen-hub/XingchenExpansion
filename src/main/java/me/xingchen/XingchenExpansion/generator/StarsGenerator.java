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
import me.xingchen.XingchenExpansion.util.Util;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StarsGenerator extends SlimefunItem implements EnergyNetProvider, InventoryBlock, EnergyNetComponent, Listener {
    // 定义输入槽和状态槽的索引
    private static final int INPUT_SLOT = 10, STATUS_SLOT = 13;
    // 定义输出槽的数组和受保护槽位（不可随意交互）
    private static final int[] OUTPUT_SLOTS = {14, 15, 16}, PROTECTED_SLOTS = {INPUT_SLOT, STATUS_SLOT, 14, 15, 16};
    // 定义背景物品，用于填充GUI的空白槽位
    private static final ItemStack BACKGROUND_ITEM = new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "&7 ");
    // 定义有效燃料的ID集合
    private static final Set<String> FUEL_IDS = Set.of("STARS_ORE", "STARS_INGOT", "STARS_CRYSTAL");
    // 定义废料的ID
    private static final String WASTE_ID = "STARS_WASTE";
    // 定义Slimefun物品的命名空间键，用于持久化数据
    private static final NamespacedKey SLIMEFUN_KEY = new NamespacedKey("slimefun", "slimefun_item");
    // 定义存储燃烧时间和燃料ID的键
    private static final String BURNING_TIME_KEY = "burning_time";
    private static final String FUEL_ID_KEY = "fuel_id";

    // 发电机的能量容量
    private final int energyCapacity;
    // 存储燃料配置的映射，键为燃料ID
    private final Map<String, FuelConfig> fuels;
    // 插件实例，用于访问插件功能
    private final XingchenExpansion plugin;
    // 方块菜单预设，用于定义GUI
    private BlockMenuPreset preset;
    // 存储所有发电~~发电机位置的线程安全集合
    private final Set<Location> generatorLocations = ConcurrentHashMap.newKeySet();

    // 内部类，用于存储燃料的配置信息
    private static class FuelConfig {
        final int energyPerTick; // 每tick产生的能量
        final int burnTime;      // 燃烧持续时间（tick）
        final int wasteAmount;   // 产生的废料数量

        FuelConfig(int energyPerTick, int burnTime, int wasteAmount) {
            this.energyPerTick = energyPerTick;
            this.burnTime = burnTime;
            this.wasteAmount = wasteAmount;
        }
    }

    // 构造函数，初始化发电机物品并加载配置
    public StarsGenerator(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, XingchenExpansion plugin) {
        super(itemGroup, item, recipeType, recipe);
        this.plugin = plugin;

        // 注册事件监听器
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // 加载配置文件
        File file = new File(plugin.getDataFolder(), "generator.yml");
        if (!file.exists()) plugin.saveResource("generator.yml", false);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // 获取发电机配置
        ConfigurationSection generatorSection = config.getConfigurationSection("generators.STARS_GENERATOR");
        if (generatorSection == null) {
            plugin.getLogger().severe("未找到 generators.STARS_GENERATOR 配置，发电机禁用！");
            this.energyCapacity = 0;
            this.fuels = new HashMap<>();
            disable(); // 禁用发电机
            return;
        }
        this.energyCapacity = generatorSection.getInt("energy_capacity", 1000);

        // 加载燃料配置
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

        // 设置GUI菜单
        setupMenuPreset();
        // 添加方块交互处理器
        addItemHandler(
                new BlockPlaceHandler(false) {
                    @Override
                    public void onPlayerPlace(BlockPlaceEvent event) {
                        Block block = event.getBlock();
                        Location loc = block.getLocation();
                        try {
                            // 检查位置是否被其他Slimefun方块占用
                            if (BlockStorage.hasBlockInfo(loc)) {
                                String existingId = BlockStorage.getLocationInfo(loc, "id");
                                if ("STARS_GENERATOR".equals(existingId)) {
                                    generatorLocations.add(loc.clone());
                                    return;
                                }
                                event.setCancelled(true);
                                event.getPlayer().sendMessage("§c此位置已被其他 Slimefun 方块占用！");
                                return;
                            }
                            // 记录方块信息
                            BlockStorage.addBlockInfo(block, "id", "STARS_GENERATOR");
                            generatorLocations.add(loc.clone());
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
                        if (event.getPlayer().isSneaking()) {
                        }
                    }
                },
                new SimpleBlockBreakHandler() {
                    @Override
                    public void onBlockBreak(@Nonnull Block block) {
                        BlockMenu menu = BlockStorage.getInventory(block);
                        if (menu != null) {
                            for (int slot : new int[]{INPUT_SLOT, 14, 15, 16}) {
                                ItemStack content = menu.getItemInSlot(slot);
                                if (content != null) {
                                    block.getWorld().dropItemNaturally(block.getLocation(), content.clone());
                                }
                            }
                        }
                        BlockStorage.clearBlockInfo(block);
                        generatorLocations.remove(block.getLocation());
                    }
                }
        );
    }

    // 设置GUI菜单预设
    private void setupMenuPreset() {
        preset = new BlockMenuPreset("STARS_GENERATOR", "星辰发电机") {
            @Override
            public void init() {
                for (int i = 0; i < 27; i++) {
                    int finalI = i;
                    if (!Arrays.stream(PROTECTED_SLOTS).anyMatch(s -> s == finalI)) {
                        addItem(i, BACKGROUND_ITEM, (p, s, item, action) -> false);
                    }
                }
                addMenuClickHandler(INPUT_SLOT, (p, s, item, action) -> true);
                addMenuClickHandler(STATUS_SLOT, (p, s, item, action) -> false);
                for (int slot : OUTPUT_SLOTS) {
                    addMenuClickHandler(slot, (p, s, item, action) -> true);
                }
            }

            @Override
            public boolean canOpen(Block b, Player p) {
                return true;
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                return flow == ItemTransportFlow.INSERT ? new int[]{INPUT_SLOT} : OUTPUT_SLOTS;
            }
        };
        preset.setPlayerInventoryClickable(true);
    }

    // 处理玩家右键点击事件
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRightClick(PlayerRightClickEvent event) {
        Optional<Block> blockOpt = event.getClickedBlock();
        if (blockOpt.isEmpty()) return;

        Block block = blockOpt.get();
        String blockId = BlockStorage.getLocationInfo(block.getLocation(), "id");
        if (!"STARS_GENERATOR".equals(blockId)) return;

        Player player = event.getPlayer();
        if (player.isSneaking()) return;

        event.cancel();
        BlockMenu menu = BlockStorage.getInventory(block);
        if (menu == null) return;
        updateStatus(menu);
        player.openInventory(menu.getInventory());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BlockMenu menu) || !menu.getPreset().getID().equals("STARS_GENERATOR")) {
            return;
        }

        if (Util.protectOutputSlots(event, plugin, OUTPUT_SLOTS, WASTE_ID, m -> m.getPreset().getID().equals("STARS_GENERATOR"))) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        int rawSlot = event.getRawSlot();
        org.bukkit.event.inventory.ClickType click = event.getClick();
        Inventory clickedInventory = event.getClickedInventory();
        boolean isPlayerInventory = clickedInventory != null && clickedInventory.equals(player.getInventory());

        // 处理从玩家背包 Shift+左键 转移物品
        if (isPlayerInventory && click == ClickType.SHIFT_LEFT && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            ItemStack current = event.getCurrentItem();
            if (current != null && current.getType() != Material.AIR) {
                ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
                boolean canInsertInput = inputItem == null || inputItem.getType() == Material.AIR ||
                        (inputItem.isSimilar(current) && inputItem.getAmount() < inputItem.getMaxStackSize());

                if (!canInsertInput) {
                    event.setCancelled(true);
                    player.sendMessage("§c输入槽已满或不可存入！");
                    plugin.getLogger().info("阻止 Shift+左键: 输入槽不可用, 物品=" + current.getType() + ", 槽位=" + slot);
                }
            }
        }
        if (rawSlot == INPUT_SLOT && clickedInventory != null && clickedInventory.equals(menu.toInventory())) {
            return;
        }
    }

    // 处理GUI拖拽事件，控制物品在槽位间的拖拽行为
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof BlockMenu menu) || !menu.getPreset().getID().equals("STARS_GENERATOR")) {
            return;
        }

        if (Util.protectOutputSlots(event, plugin, OUTPUT_SLOTS, WASTE_ID, m -> m.getPreset().getID().equals("STARS_GENERATOR"))) {
            return;
        }

        if (event.getRawSlots().contains(INPUT_SLOT)) {
            return;
        }
    }

    // 获取物品的废料ID，检查是否为有效废料
    private String getWasteId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String slimefunId = pdc.get(SLIMEFUN_KEY, PersistentDataType.STRING);
        return slimefunId != null && slimefunId.equals(WASTE_ID) ? slimefunId : null;
    }

    // 将物品返回给玩家背包，若背包满则掉落在玩家位置
    private void returnItem(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> unadded = player.getInventory().addItem(item);
        if (!unadded.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }

    // 获取物品的燃料ID，检查是否为有效燃料
    private String getFuelId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String slimefunId = pdc.get(SLIMEFUN_KEY, PersistentDataType.STRING);
        return slimefunId != null && FUEL_IDS.contains(slimefunId) ? slimefunId : null;
    }

    // 更新GUI状态槽，显示当前能量输出
    private void updateStatus(BlockMenu menu) {
        ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
        int energy = 0;
        String fuelId = getFuelId(inputItem);
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
            menu.addItem(STATUS_SLOT, status, (p, s, clickedItem, action) -> false);
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
                menu.addItem(STATUS_SLOT, status, (p, s, clickedItem, action) -> false);
                generatorLocations.add(location.clone());
            } catch (IllegalStateException e) {
                plugin.getLogger().warning("无法初始化 BlockStorage 数据，位置: " + location);
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

        ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
        if (inputItem == null) {
            BlockStorage.addBlockInfo(location, BURNING_TIME_KEY, null);
            BlockStorage.addBlockInfo(location, FUEL_ID_KEY, null);
            updateStatus(menu);
            return 0;
        }

        String fuelId = getFuelId(inputItem);
        if (fuelId == null) {
            updateStatus(menu);
            return 0;
        }

        FuelConfig fuelConfig = fuels.get(fuelId);
        if (fuelConfig == null) {
            updateStatus(menu);
            return 0;
        }

        int newAmount = inputItem.getAmount() - 1;
        ItemStack newItem = newAmount > 0 ? inputItem.clone() : null;
        if (newItem != null) newItem.setAmount(newAmount);
        menu.replaceExistingItem(INPUT_SLOT, newItem);
        BlockStorage.addBlockInfo(location, BURNING_TIME_KEY, String.valueOf(fuelConfig.burnTime));
        BlockStorage.addBlockInfo(location, FUEL_ID_KEY, fuelId);
        if (fuelConfig.wasteAmount > 0) {
            ItemStack waste = Items.STARS_WASTE.clone();
            waste.setAmount(fuelConfig.wasteAmount);
            for (int slot : OUTPUT_SLOTS) {
                ItemStack existing = menu.getItemInSlot(slot);
                if (existing == null) {
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