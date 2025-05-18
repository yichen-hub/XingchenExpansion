package me.xingchen.XingchenExpansion.generator.abstractGenerator;


import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import me.xingchen.XingchenExpansion.util.ConversionUtil;
import me.xingchen.XingchenExpansion.util.Util;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class StarsGenerator extends SlimefunItem implements EnergyNetProvider, EnergyNetComponent, Listener {
    protected static final int INPUT_SLOT = 10;
    protected static final int STATUS_SLOT = 13;
    protected static final int[] OUTPUT_SLOTS = {14, 15, 16};
    protected static final int[] PROTECTED_SLOTS = {INPUT_SLOT, STATUS_SLOT, 14, 15, 16};
    protected static final String WASTE_ID = "STARS_WASTE";
    protected final BlockMenuPreset preset;
    protected final int energyCapacity;
    protected final JavaPlugin plugin;
    protected final String generatorId;
    protected final ConversionUtil.ConversionProcessor processor;
    protected final Set<Location> generatorLocations = ConcurrentHashMap.newKeySet();
    protected final Map<String, FuelConfig> fuels;
    protected final List<ConversionUtil.ConversionRule> rules;

    protected static class FuelConfig {
        final int energyPerTick;
        final int burnTime;
        final int wasteAmount;

        FuelConfig(int energyPerTick, int burnTime, int wasteAmount) {
            this.energyPerTick = energyPerTick;
            this.burnTime = burnTime;
            this.wasteAmount = wasteAmount;
        }
    }

    public StarsGenerator(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, XingchenExpansion plugin, String generatorId) {
        super(itemGroup, item, recipeType, recipe);
        this.plugin = plugin;
        this.generatorId = generatorId;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        File configFile = new File(plugin.getDataFolder(), "stars_generator.yml");
        if (!configFile.exists()) {
            plugin.saveResource("stars_generator.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        var section = config.getConfigurationSection("generators." + generatorId);
        if (section == null) {
            plugin.getLogger().severe("未找到 generators." + generatorId + " 的配置，发电机将被禁用！");
            this.energyCapacity = 0;
            this.preset = null;
            this.processor = null;
            this.fuels = new HashMap<>();
            this.rules = new ArrayList<>();
            disable();
            return;
        }

        int energyCapacity = section.getInt("energy_capacity", 1000);
        var fuelsSection = section.getConfigurationSection("fuels");
        if (fuelsSection == null) {
            plugin.getLogger().severe("未找到 fuels 配置，发电机将被禁用！");
            this.energyCapacity = 0;
            this.preset = null;
            this.processor = null;
            this.fuels = new HashMap<>();
            this.rules = new ArrayList<>();
            disable();
            return;
        }

        List<ConversionUtil.ConversionRule> rules = new ArrayList<>();
        Map<String, FuelConfig> fuels = new HashMap<>();
        for (String fuelId : fuelsSection.getKeys(false)) {
            int energy = fuelsSection.getInt(fuelId + ".energy_per_tick", 0);
            int burnTime = fuelsSection.getInt(fuelId + ".burn_time", 0);
            int wasteAmount = fuelsSection.getInt(fuelId + ".waste_amount", 0);
            if (energy > 0 && burnTime > 0) {
                fuels.put(fuelId, new FuelConfig(energy, burnTime, wasteAmount));
                ConversionUtil.ConversionRule rule = new ConversionUtil.ConversionRule(
                        List.of(fuelId),
                        wasteAmount > 0 ? List.of(WASTE_ID) : List.of(),
                        List.of(1),
                        wasteAmount > 0 ? List.of(wasteAmount) : List.of(),
                        wasteAmount > 0 ? ConversionUtil.OutputMode.ALL : ConversionUtil.OutputMode.NONE,
                        burnTime
                );
                rules.add(rule);
            }
        }

        if (rules.isEmpty()) {
            plugin.getLogger().severe("未加载任何有效燃料配置，发电机将被禁用！");
            this.energyCapacity = 0;
            this.preset = null;
            this.processor = null;
            this.fuels = new HashMap<>();
            this.rules = new ArrayList<>();
            disable();
            return;
        }

        this.energyCapacity = energyCapacity;
        this.fuels = fuels;
        this.rules = rules;
        this.processor = new ConversionUtil.SingleInputProcessor(rules);

        this.preset = new BlockMenuPreset(getId(), item.getDisplayName()) {
            @Override
            public void init() {
                setupMenuPreset();
            }

            @Override
            public void newInstance(BlockMenu menu, Block b) {
                newItemMenu(menu);
            }

            @Override
            public boolean canOpen(Block b, Player p) {
                return p.hasPermission("slimefun.inventory.bypass") || Slimefun.getPermissionsService().hasPermission(p, StarsGenerator.this);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                if (flow == ItemTransportFlow.INSERT) {
                    return new int[]{INPUT_SLOT};
                } else {
                    return OUTPUT_SLOTS;
                }
            }

            @Override
            public int getSize() {
                return 27;
            }
        };

        addItemHandler(
                new BlockPlaceHandler(false) {
                    @Override
                    public void onPlayerPlace(BlockPlaceEvent event) {
                        Block block = event.getBlock();
                        Location loc = block.getLocation();
                        generatorLocations.add(loc.clone());
                    }
                },
                new BlockBreakHandler(false, false) {
                    @Override
                    public void onPlayerBreak(BlockBreakEvent e, ItemStack item, List<ItemStack> drops) {
                        Block b = e.getBlock();
                        BlockMenu inv = BlockStorage.getInventory(b);
                        if (inv != null) {
                            inv.dropItems(b.getLocation(), INPUT_SLOT);
                            inv.dropItems(b.getLocation(), OUTPUT_SLOTS);
                        }
                        BlockStorage.clearBlockInfo(b);
                        generatorLocations.remove(b.getLocation());
                    }
                }
        );

        plugin.getLogger().info("注册发电机: " + generatorId);
    }

    public BlockMenuPreset getPreset() {
        return preset;
    }

    @Override
    public int getGeneratedOutput(@Nonnull Location location, @Nonnull Config data) {
        if (preset == null || processor == null) return 0;

        BlockMenu menu = BlockStorage.getInventory(location.getBlock());
        if (menu == null) {
            try {
                BlockStorage.addBlockInfo(location, "id", getId());
                menu = new BlockMenu(preset, location);
                newItemMenu(menu);
                generatorLocations.add(location.clone());
            } catch (IllegalStateException e) {
                plugin.getLogger().warning("无法初始化 BlockStorage: 位置=" + location + ", 错误=" + e.getMessage());
                return 0;
            }
        }

        ConversionUtil.ProgressInfo progress = processor.getProgressInfo(location, plugin);
        if (progress.isProcessing()) {
            String ruleId = progress.getRuleId();
            for (ConversionUtil.ConversionRule rule : rules) {
                if (rule.getRuleId().equals(ruleId)) {
                    String fuelId = rule.getInputItems().get(0);
                    FuelConfig config = fuels.get(fuelId);
                    updateStatus(menu);
                    return config != null ? config.energyPerTick : 0;
                }
            }
        }

        boolean processed = processor.process(menu, location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
        updateStatus(menu);
        return processed ? getCurrentFuelEnergy(menu) : 0;
    }

    protected int getCurrentFuelEnergy(BlockMenu menu) {
        ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
        String fuelId = Util.getSlimefunId(inputItem);
        FuelConfig config = fuelId != null ? fuels.get(fuelId) : null;
        return config != null ? config.energyPerTick : 0;
    }

    protected void updateStatus(BlockMenu menu) {
        ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
        Location location = menu.getLocation();
        ConversionUtil.ProgressInfo progress = processor.getProgressInfo(location, plugin);
        String statusText = "§c待机";
        int energy = getCurrentFuelEnergy(menu);

        if (progress.isProcessing()) {
            statusText = "§a运行中 (§e剩余: " + String.format("%.1f", progress.getRemainingSeconds()) + "秒)";
        } else if (Util.getSlimefunId(inputItem) != null && fuels.containsKey(Util.getSlimefunId(inputItem))) {
            statusText = "§a准备运行";
        }

        ItemStack status = new CustomItemStack(Material.REDSTONE, "§c状态: " + statusText, "§7能量: " + energy + " J/tick");
        menu.replaceExistingItem(STATUS_SLOT, status);
        menu.addMenuClickHandler(STATUS_SLOT, (p, s, item, action) -> false);
    }

    protected void setupMenuPreset() {
        if (preset == null) return;
        for (int i = 0; i < 27; i++) {
            int finalI = i;
            if (i == INPUT_SLOT || i == STATUS_SLOT || Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == finalI)) continue;
            preset.addItem(i, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " "), (p, s, item, action) -> false);
        }
        preset.addMenuClickHandler(INPUT_SLOT, (p, s, item, action) -> true);
        for (int slot : OUTPUT_SLOTS) {
            preset.addMenuClickHandler(slot, (p, s, item, action) -> true);
        }
        preset.addMenuClickHandler(STATUS_SLOT, (p, s, item, action) -> false);
    }

    protected void newItemMenu(BlockMenu menu) {
        for (int i = 0; i < 27; i++) {
            int finalI = i;
            if (i == INPUT_SLOT || i == STATUS_SLOT || Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == finalI)) continue;
            ItemStack existing = menu.getItemInSlot(i);
            if (existing == null || existing.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                menu.replaceExistingItem(i, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " "));
                menu.addMenuClickHandler(i, (p, s, item, action) -> false);
            }
        }
        menu.replaceExistingItem(INPUT_SLOT, null);
        menu.addMenuClickHandler(INPUT_SLOT, (p, s, item, action) -> true);
        ItemStack status = new CustomItemStack(Material.REDSTONE, "§c状态: 待机");
        menu.replaceExistingItem(STATUS_SLOT, status);
        menu.addMenuClickHandler(STATUS_SLOT, (p, s, item, action) -> false);
        for (int slot : OUTPUT_SLOTS) {
            menu.replaceExistingItem(slot, null);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (Util.protectOutputSlots(event, plugin, OUTPUT_SLOTS, WASTE_ID, menu -> menu.getPreset().getID().equals(getId()))) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof BlockMenu menu) || !menu.getPreset().getID().equals(getId())) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory()) &&
                event.getClick() == org.bukkit.event.inventory.ClickType.SHIFT_LEFT &&
                event.getAction() == org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY) {
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
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getRawSlots().contains(INPUT_SLOT)) {
            return;
        }
        Util.protectOutputSlots(event, plugin, OUTPUT_SLOTS, WASTE_ID, menu -> menu.getPreset().getID().equals(getId()));
    }

    @Override
    public int getCapacity() {
        return energyCapacity;
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