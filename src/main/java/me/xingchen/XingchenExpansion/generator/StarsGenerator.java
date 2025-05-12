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
    private static final int INPUT_SLOT = 10, STATUS_SLOT = 13;
    private static final int[] OUTPUT_SLOTS = {14, 15, 16}, PROTECTED_SLOTS = {INPUT_SLOT, STATUS_SLOT, 14, 15, 16};
    private static final ItemStack BACKGROUND_ITEM = new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "&7 ");
    private static final Set<String> FUEL_IDS = Set.of("STARS_ORE", "STARS_INGOT", "STARS_CRYSTAL");
    private static final String WASTE_ID = "STARS_WASTE";
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
                                if ("STARS_GENERATOR".equals(existingId)) {
                                    generatorLocations.add(loc.clone());
                                    return;
                                }
                                event.setCancelled(true);
                                event.getPlayer().sendMessage("§c此位置已被其他 Slimefun 方块占用！");
                                return;
                            }
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
                            return; // 潜行右键：允许默认交互
                        }
                        // 非潜行右键：依赖 PlayerRightClickEvent
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRightClick(PlayerRightClickEvent event) {
        Optional<Block> blockOpt = event.getClickedBlock();
        if (blockOpt.isEmpty()) return;

        Block block = blockOpt.get();
        String blockId = BlockStorage.getLocationInfo(block.getLocation(), "id");
        if (!"STARS_GENERATOR".equals(blockId)) return;

        Player player = event.getPlayer();
        if (player.isSneaking()) return; // 潜行右键：允许默认交互

        event.cancel(); // 非潜行右键：打开 UI，阻止其他交互
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
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        int rawSlot = event.getRawSlot();
        org.bukkit.event.inventory.ClickType click = event.getClick();
        Inventory clickedInventory = event.getClickedInventory();
        boolean isBlockMenu = clickedInventory != null && clickedInventory.equals(menu.toInventory());
        boolean isPlayerInventory = clickedInventory != null && clickedInventory.equals(player.getInventory());

        // 背包 Shift+左键：只允许转移到输入槽
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
                    return;
                }
            }
        }

        // 输入槽：允许存入和取出
        if (rawSlot == INPUT_SLOT && isBlockMenu) {
            return;
        }

        // 输出槽：允许特定取出，阻止存入
        if (Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == rawSlot) && isBlockMenu) {
            ItemStack cursor = event.getCursor();
            boolean isInsert = (cursor != null && cursor.getType() != Material.AIR) ||
                    (click == ClickType.SHIFT_LEFT && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) ||
                    click.isKeyboardClick();

            if (isInsert) {
                event.setCancelled(true);
                ItemStack item = cursor != null && cursor.getType() != Material.AIR ? cursor :
                        click.isKeyboardClick() ? player.getInventory().getItem(event.getHotbarButton()) : null;

                if (item != null && item.getType() != Material.AIR) {
                    returnItem(player, item.clone());
                    if (cursor != null && cursor.getType() != Material.AIR) {
                        player.setItemOnCursor(null);
                    } else if (click.isKeyboardClick()) {
                        player.getInventory().setItem(event.getHotbarButton(), null);
                    }
                    player.sendMessage("§c输出槽不支持存入！");
                    plugin.getLogger().info("阻止存入输出槽: 物品=" + item.getType() + ", 槽位=" + rawSlot);
                }

                // 延迟检查输出槽
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    for (int outputSlot : OUTPUT_SLOTS) {
                        ItemStack outputItem = menu.getItemInSlot(outputSlot);
                        if (outputItem != null && outputItem.getType() != Material.AIR && getWasteId(outputItem) == null) {
                            returnItem(player, outputItem.clone());
                            menu.replaceExistingItem(outputSlot, null);
                            player.sendMessage("§c输出槽不支持存入！");
                            plugin.getLogger().info("延迟检查移除非法物品: 输出槽=" + outputSlot + ", 物品=" + outputItem.getType());
                        }
                    }
                }, 2L);
            } else if (click != ClickType.LEFT && click != ClickType.SHIFT_LEFT &&
                    click != ClickType.NUMBER_KEY && click != ClickType.DROP) {
                event.setCancelled(true);
                player.sendMessage("§c输出槽只能左键、Shift+左键、数字键或丢弃键取出！");
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof BlockMenu menu) || !menu.getPreset().getID().equals("STARS_GENERATOR")) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        ItemStack cursor = event.getOldCursor();

        // 输入槽：允许拖拽存入
        if (event.getRawSlots().contains(INPUT_SLOT)) {
            return;
        }

        // 输出槽：阻止拖拽存入
        if (event.getRawSlots().stream().anyMatch(slot -> Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == slot))) {
            event.setCancelled(true);
            if (cursor != null && cursor.getType() != Material.AIR) {
                returnItem(player, cursor.clone());
                event.setCursor(null);
                player.sendMessage("§c输出槽不支持存入！");
                plugin.getLogger().info("阻止拖拽存入输出槽: 物品=" + cursor.getType());
            }
        }
    }

    private String getWasteId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String slimefunId = pdc.get(SLIMEFUN_KEY, PersistentDataType.STRING);
        return slimefunId != null && slimefunId.equals(WASTE_ID) ? slimefunId : null;
    }

    private void returnItem(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> unadded = player.getInventory().addItem(item);
        if (!unadded.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }

    private String getFuelId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String slimefunId = pdc.get(SLIMEFUN_KEY, PersistentDataType.STRING);
        return slimefunId != null && FUEL_IDS.contains(slimefunId) ? slimefunId : null;
    }

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