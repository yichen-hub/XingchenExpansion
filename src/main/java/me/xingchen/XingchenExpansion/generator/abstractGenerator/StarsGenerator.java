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
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.LoreBuilder;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

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
    protected final Set<Location> generatorLocations = ConcurrentHashMap.newKeySet();
    protected final Map<String, FuelConfig> fuels;
    protected final Map<String, ConversionUtil.AbstractConversionProcessor> processors;
    protected final Map<String, ConversionUtil.ConversionRule> rules;
    protected final ConversionUtil.StateItemProvider stateItemProvider;
    protected final ConversionUtil.SpecialCaseHandler specialCaseHandler;

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

    public StarsGenerator(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe,
                          XingchenExpansion plugin, String generatorId) {
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
            this.fuels = new HashMap<>();
            this.processors = new HashMap<>();
            this.rules = new HashMap<>();
            this.stateItemProvider = null;
            this.specialCaseHandler = null;
            disable();
            return;
        }

        int energyCapacity = section.getInt("energy_capacity", 1000);
        var fuelsSection = section.getConfigurationSection("fuels");
        if (fuelsSection == null) {
            plugin.getLogger().severe("未找到 fuels 配置，发电机将被禁用！");
            this.energyCapacity = 0;
            this.preset = null;
            this.fuels = new HashMap<>();
            this.processors = new HashMap<>();
            this.rules = new HashMap<>();
            this.stateItemProvider = null;
            this.specialCaseHandler = null;
            disable();
            return;
        }

        Map<String, ConversionUtil.AbstractConversionProcessor> processors = new HashMap<>();
        Map<String, FuelConfig> fuels = new HashMap<>();
        Map<String, ConversionUtil.ConversionRule> rules = new HashMap<>();
        for (String fuelId : fuelsSection.getKeys(false)) {
            int energy = fuelsSection.getInt(fuelId + ".energy_per_tick", 0);
            int burnTime = fuelsSection.getInt(fuelId + ".burn_time", 0);
            int wasteAmount = fuelsSection.getInt(fuelId + ".waste_amount", 0);
            if (energy > 0 && burnTime > 0) {
                fuels.put(fuelId, new FuelConfig(energy, burnTime, wasteAmount));
                ConversionUtil.ConversionRule rule = new ConversionUtil.ConversionRule(
                        List.of(new ConversionUtil.SlimefunItemMatcherImpl(fuelId)),
                        wasteAmount > 0 ? List.of(new ConversionUtil.SlimefunItemMatcherImpl(WASTE_ID)) : Collections.emptyList(),
                        List.of(1),
                        wasteAmount > 0 ? List.of(wasteAmount) : Collections.emptyList(),
                        wasteAmount > 0 ? ConversionUtil.OutputMode.ALL : ConversionUtil.OutputMode.NONE,
                        burnTime,
                        STATUS_SLOT,
                        true
                );
                rules.put(fuelId, rule);
                processors.put(fuelId, new ConversionUtil.SingleInputProcessor(
                        List.of(rule),
                        new GeneratorStateItemProvider(),
                        new GeneratorSpecialCaseHandler()
                ));
            }
        }

        if (processors.isEmpty()) {
            plugin.getLogger().severe("未加载任何有效燃料配置，发电机将被禁用！");
            this.energyCapacity = 0;
            this.preset = null;
            this.fuels = new HashMap<>();
            this.processors = new HashMap<>();
            this.rules = new HashMap<>();
            this.stateItemProvider = null;
            this.specialCaseHandler = null;
            disable();
            return;
        }

        this.energyCapacity = energyCapacity;
        this.fuels = fuels;
        this.processors = processors;
        this.rules = rules;
        this.stateItemProvider = new GeneratorStateItemProvider();
        this.specialCaseHandler = new GeneratorSpecialCaseHandler();

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
                return p.hasPermission("slimefun.inventory.bypass") ||
                        Slimefun.getPermissionsService().hasPermission(p, StarsGenerator.this);
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
                new BlockTicker() {
                    @Override
                    public void tick(Block b, SlimefunItem sf, me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config data) {
                        StarsGenerator.this.tick(b);
                    }

                    @Override
                    public boolean isSynchronized() {
                        return true;
                    }
                },
                new BlockPlaceHandler(false) {
                    @Override
                    public void onPlayerPlace(BlockPlaceEvent event) {
                        Block block = event.getBlock();
                        generatorLocations.add(normalizeLocation(block.getLocation()));
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
                            String fuelId = BlockStorage.getLocationInfo(b.getLocation(), "fuel_id");
                            ConversionUtil.AbstractConversionProcessor processor = fuelId != null ? processors.get(fuelId) : null;
                            if (processor != null) {
                                processor.terminateWithReturn(inv, b.getLocation(), plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
                            }
                        }
                        BlockStorage.clearBlockInfo(b);
                        generatorLocations.remove(normalizeLocation(b.getLocation()));
                    }
                }
        );

        plugin.getLogger().info("注册发电机: " + generatorId);
    }

    private class GeneratorStateItemProvider implements ConversionUtil.StateItemProvider {
        @Override
        @Nonnull
        public Map<ConversionUtil.MachineState, ItemStack> getStateItems() {
            Map<ConversionUtil.MachineState, ItemStack> items = new HashMap<>();
            items.put(ConversionUtil.MachineState.CONVERTING, new CustomItemStack(
                    Material.LIME_DYE, "§a运行中",
                    "§7状态: 运行中", "§7" + LoreBuilder.powerPerSecond(0)
            ));
            items.put(ConversionUtil.MachineState.OUTPUT_FULL, new CustomItemStack(
                    Material.REDSTONE, "§c暂停",
                    "§7状态: 输出槽已满", "§7" + LoreBuilder.powerPerSecond(0)
            ));
            items.put(ConversionUtil.MachineState.IDLE, new CustomItemStack(
                    Material.REDSTONE, "§c待机",
                    "§7状态: 待机", "§7" + LoreBuilder.powerPerSecond(0)
            ));
            items.put(ConversionUtil.MachineState.NO_INPUT, new CustomItemStack(
                    Material.REDSTONE, "§c无燃料",
                    "§7状态: 缺少燃料", "§7" + LoreBuilder.powerPerSecond(0)
            ));
            items.put(ConversionUtil.MachineState.NO_ENERGY, new CustomItemStack(
                    Material.REDSTONE, "§c能量网络故障",
                    "§7状态: 能量网络故障", "§7" + LoreBuilder.powerPerSecond(0)
            ));
            return items;
        }
    }

    private class GeneratorSpecialCaseHandler implements ConversionUtil.SpecialCaseHandler {
        @Override
        @Nonnull
        public Optional<ItemStack> handleSpecialCase(@Nonnull ConversionUtil.ProgressInfo info) {
            return Optional.empty();
        }
    }

    protected void tick(Block b) {
        if (preset == null || processors.isEmpty()) return;
        process(b.getLocation());
    }

    protected void process(@Nonnull Location location) {
        if (preset == null || processors.isEmpty()) return;

        BlockMenu menu = BlockStorage.getInventory(location.getBlock());
        if (menu == null) {
            try {
                if (BlockStorage.getLocationInfo(location, "id") == null) {
                    BlockStorage.addBlockInfo(location, "id", getId());
                }
                menu = new BlockMenu(preset, location);
                newItemMenu(menu);
                generatorLocations.add(normalizeLocation(location));
                menu.save(menu.getLocation());
            } catch (IllegalStateException e) {
                plugin.getLogger().log(Level.WARNING, "无法初始化 BlockStorage: 位置=" + location + ", 错误=" + e.getMessage());
                return;
            }
        }

        ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
        String fuelId = Util.getSlimefunId(inputItem);
        ConversionUtil.AbstractConversionProcessor processor = fuelId != null ? processors.get(fuelId) : null;
        boolean shouldSave = false;
        String saveReason = null;

        // 重置状态：输入槽清空或燃料变化
        String currentFuelId = BlockStorage.getLocationInfo(location, "fuel_id");
        if (inputItem == null || inputItem.getType() == Material.AIR || (currentFuelId != null && fuelId != null && !currentFuelId.equals(fuelId))) {
            if (currentFuelId != null) {
                ConversionUtil.AbstractConversionProcessor oldProcessor = processors.get(currentFuelId);
                if (oldProcessor != null) {
                    oldProcessor.pause(menu, location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
                }
                BlockStorage.addBlockInfo(location, "fuel_id", null);
                BlockStorage.addBlockInfo(location, "rule_id", null);
                BlockStorage.addBlockInfo(location, "energy_per_tick", null);
                BlockStorage.addBlockInfo(location, "task_state", null);
                for (ConversionUtil.ConversionRule rule : rules.values()) {
                    BlockStorage.addBlockInfo(location, "processing_time_" + rule.getRuleId(), null);
                }
                saveReason = "燃料切换或输入清空";
                menu.save(menu.getLocation());
            }
            updateStatus(menu, location);
            if (inputItem == null || inputItem.getType() == Material.AIR) {
                return;
            }
        }

        if (processor == null) {
            updateStatus(menu, location);
            return;
        }

        // 获取规则和状态
        String ruleId = BlockStorage.getLocationInfo(location, "rule_id");
        ConversionUtil.TaskState taskState = processor.getTaskState(location);
        ConversionUtil.ConversionRule activeRule = null;

        // 初始化规则
        if (ruleId == null) {
            activeRule = rules.get(fuelId);
            if (activeRule != null) {
                ruleId = activeRule.getRuleId();
                BlockStorage.addBlockInfo(location, "rule_id", ruleId);
                BlockStorage.addBlockInfo(location, "task_state", ConversionUtil.TaskState.IDLE.name());
                BlockStorage.addBlockInfo(location, "processing_time_" + ruleId, "0");
                shouldSave = true;
                saveReason = "规则初始化";
                menu.save(menu.getLocation());
            } else {
                updateStatus(menu, location);
                return;
            }
        } else {
            activeRule = rules.get(fuelId);
            if (activeRule == null || !activeRule.getRuleId().equals(ruleId)) {
                updateStatus(menu, location);
                return;
            }
        }

        // 检查输入和输出
        boolean hasValidInput = inputItem != null && activeRule.getInputItems().stream()
                .anyMatch(matcher -> matcher.matches(inputItem));
        if (!hasValidInput) {
            if (taskState == ConversionUtil.TaskState.RUNNING) {
                processor.pause(menu, location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
            }
            updateStatus(menu, location);
            return;
        }

        boolean canOutput = processor.canOutput(menu, OUTPUT_SLOTS, activeRule, plugin, location);
        if (!canOutput) {
            if (taskState == ConversionUtil.TaskState.RUNNING) {
                processor.pause(menu, location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
            }
            updateStatus(menu, location);
            return;
        }

        // 恢复运行
        if (taskState == ConversionUtil.TaskState.PAUSED && activeRule != null) {
            processor.resume(menu, location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
        }

        // 执行转化
        boolean processed = processor.process(menu, location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
        if (processed) {
            shouldSave = true;
            saveReason = "转化完成";
            FuelConfig config = fuels.get(fuelId);
            if (config != null) {
                BlockStorage.addBlockInfo(location, "fuel_id", fuelId);
                BlockStorage.addBlockInfo(location, "rule_id", ruleId);
                BlockStorage.addBlockInfo(location, "energy_per_tick", String.valueOf(config.energyPerTick));
            }
        }

        updateStatus(menu, location);
        if (shouldSave) {
            menu.save(menu.getLocation());
        }
    }

    @Override
    public int getGeneratedOutput(@Nonnull Location location, @Nonnull me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config data) {
        if (preset == null || processors.isEmpty()) return 0;

        BlockMenu menu = BlockStorage.getInventory(location.getBlock());
        if (menu == null) return 0;

        ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
        String fuelId = Util.getSlimefunId(inputItem);
        ConversionUtil.AbstractConversionProcessor processor = fuelId != null ? processors.get(fuelId) : null;
        if (processor == null) return 0;

        ConversionUtil.TaskState taskState = processor.getTaskState(location);
        if (taskState != ConversionUtil.TaskState.RUNNING) return 0;

        ConversionUtil.ProgressInfo progress = processor.getProgressInfo(location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
        if (!progress.isProcessing()) return 0;

        String ruleId = progress.getRuleId();
        ConversionUtil.ConversionRule activeRule = null;
        if (ruleId != null) {
            activeRule = processor.getRules().stream()
                    .filter(r -> r.getRuleId().equals(ruleId))
                    .findFirst()
                    .orElse(null);
        }
        if (!processor.canOutput(menu, OUTPUT_SLOTS, activeRule, plugin, location)) return 0;

        String energyStr = BlockStorage.getLocationInfo(location, "energy_per_tick");
        try {
            return energyStr != null ? Integer.parseInt(energyStr) : 0;
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("能量格式错误: 值=" + energyStr + ", 位置=" + location);
            return 0;
        }
    }

    protected void updateStatus(@Nonnull BlockMenu menu, @Nonnull Location location) {
        ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
        String fuelId = Util.getSlimefunId(inputItem);
        ConversionUtil.AbstractConversionProcessor processor = fuelId != null ? processors.get(fuelId) : null;
        ConversionUtil.ProgressInfo progress = processor != null
                ? processor.getProgressInfo(location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS)
                : new ConversionUtil.ProgressInfo(ConversionUtil.MachineState.IDLE, null, 0, 0, false);
        int energy = getCurrentFuelEnergy(menu);

        ItemStack statusItem = specialCaseHandler.handleSpecialCase(progress).orElseGet(() -> {
            if (inputItem != null && inputItem.getType() != Material.AIR && (fuelId == null || !fuels.containsKey(fuelId))) {
                return new CustomItemStack(
                        Material.REDSTONE, "§c无效燃料",
                        "§7状态: 无效燃料",
                        "§7能量: 0 J/tick"
                );
            }
            Map<ConversionUtil.MachineState, ItemStack> stateItems = stateItemProvider.getStateItems();
            ItemStack template = stateItems.getOrDefault(progress.getState(),
                    new ConversionUtil.DefaultStateItemProvider().getStateItems().get(progress.getState()));
            if (template == null) {
                plugin.getLogger().warning("状态模板缺失: state=" + progress.getState() + ", location=" + location);
                template = new CustomItemStack(Material.REDSTONE, "§c状态: " + progress.getState().getDisplayName());
            }
            ItemStack result = template.clone();
            ItemMeta meta = result.getItemMeta();
            if (meta == null) {
                meta = plugin.getServer().getItemFactory().getItemMeta(result.getType());
                result.setItemMeta(meta);
            }
            List<String> lore = new ArrayList<>(meta.hasLore() ? meta.getLore() : Collections.emptyList());
            int statusIndex = -1;
            for (int i = 0; i < lore.size(); i++) {
                if (lore.get(i).startsWith("§7状态:")) {
                    statusIndex = i;
                    break;
                }
            }
            lore.removeIf(line -> line.startsWith("§7能量:"));
            lore.add(statusIndex >= 0 ? statusIndex + 1 : lore.size(), "§7能量: " + energy + " J/tick");
            if (progress.isProcessing() && progress.getState() == ConversionUtil.MachineState.CONVERTING) {
                lore.add(statusIndex >= 0 ? statusIndex + 2 : lore.size(), "§7进度: " + progress.getProgressBar(10));
                lore.add(statusIndex >= 0 ? statusIndex + 3 : lore.size(), "§7剩余: " + String.format("%.1fs", progress.getRemainingSeconds()));
            }
            meta.setLore(lore);
            result.setItemMeta(meta);
            return result;
        });

        menu.replaceExistingItem(STATUS_SLOT, statusItem);
        menu.addMenuClickHandler(STATUS_SLOT, ChestMenuUtils.getEmptyClickHandler());
    }

    protected int getCurrentFuelEnergy(@Nonnull BlockMenu menu) {
        ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
        String fuelId = Util.getSlimefunId(inputItem);
        FuelConfig config = fuelId != null ? fuels.get(fuelId) : null;
        return config != null ? config.energyPerTick : 0;
    }

    protected void setupMenuPreset() {
        if (preset == null) return;
        for (int i = 0; i < 27; i++) {
            int finalI = i;
            if (i == INPUT_SLOT || i == STATUS_SLOT || Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == finalI)) continue;
            preset.addItem(i, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " "), ChestMenuUtils.getEmptyClickHandler());
            preset.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addMenuClickHandler(INPUT_SLOT, (p, s, item, action) -> true);
        for (int slot : OUTPUT_SLOTS) {
            preset.addMenuClickHandler(slot, (p, slotNum, clickedItem, action) -> {
                if (action.toString().startsWith("PICKUP_") || action.toString().startsWith("DROP_")) {
                    return true;
                }
                return false;
            });
        }
        preset.addMenuClickHandler(STATUS_SLOT, ChestMenuUtils.getEmptyClickHandler());
    }

    protected void newItemMenu(@Nonnull BlockMenu menu) {
        for (int i = 0; i < 27; i++) {
            int finalI = i;
            if (i == INPUT_SLOT || i == STATUS_SLOT || Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == finalI)) continue;
            ItemStack existing = menu.getItemInSlot(i);
            if (existing == null || existing.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                menu.replaceExistingItem(i, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " "));
            }
            menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
        }
        menu.addMenuClickHandler(INPUT_SLOT, (p, s, item, action) -> true);
        if (menu.getItemInSlot(STATUS_SLOT) == null) {
            menu.replaceExistingItem(STATUS_SLOT, new CustomItemStack(Material.REDSTONE, "§c状态: 待机"));
        }
        menu.addMenuClickHandler(STATUS_SLOT, ChestMenuUtils.getEmptyClickHandler());

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BlockMenu menu) || !menu.getPreset().getID().equals(getId())) {
            return;
        }

        int rawSlot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();
        boolean shouldSave = false;
        String saveReason = null;

        // 处理 BlockMenu 槽位（rawSlot < 27）
        if (rawSlot < menu.toInventory().getSize()) {
            // 输出槽
            if (Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == rawSlot)) {
                String action = event.getAction().toString();
                if (action.startsWith("PICKUP_") || action.startsWith("DROP_") || action.equals("MOVE_TO_OTHER_INVENTORY")) {
                    menu.save(menu.getLocation());
                }
                return;
            }

            // 输入槽
            if (rawSlot == INPUT_SLOT) {
                ItemStack cursor = event.getCursor();
                String fuelId = Util.getSlimefunId(cursor);
                menu.save(menu.getLocation());
                saveReason = "输入槽修改";

                // 检查燃料切换
                String currentFuelId = BlockStorage.getLocationInfo(menu.getLocation(), "fuel_id");
                if (fuelId != null && currentFuelId != null && !fuelId.equals(currentFuelId)) {
                    ConversionUtil.AbstractConversionProcessor oldProcessor = processors.get(currentFuelId);
                    if (oldProcessor != null) {
                        oldProcessor.pause(menu, menu.getLocation(), plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
                    }
                    BlockStorage.addBlockInfo(menu.getLocation(), "fuel_id", null);
                    BlockStorage.addBlockInfo(menu.getLocation(), "rule_id", null);
                    BlockStorage.addBlockInfo(menu.getLocation(), "energy_per_tick", null);
                    BlockStorage.addBlockInfo(menu.getLocation(), "task_state", null);
                    for (ConversionUtil.ConversionRule rule : rules.values()) {
                        BlockStorage.addBlockInfo(menu.getLocation(), "processing_time_" + rule.getRuleId(), null);
                    }
                    saveReason = "输入槽修改（燃料切换）";
                    menu.save(menu.getLocation());
                }
                return;
            }
        }

        // 处理背包栏位（rawSlot >= 27）
        // Shift+左键插入输入槽
        if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory()) &&
                event.getClick() == org.bukkit.event.inventory.ClickType.SHIFT_LEFT &&
                event.getAction() == org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            ItemStack current = event.getCurrentItem();
            if (current != null && current.getType() != Material.AIR) {
                ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
                String fuelId = Util.getSlimefunId(current);
                boolean matchesFuel = fuelId != null && fuels.containsKey(fuelId);
                boolean canInsertInput = inputItem == null || inputItem.getType() == Material.AIR ||
                        (inputItem.isSimilar(current) && inputItem.getAmount() < inputItem.getMaxStackSize() && matchesFuel);

                // 检查燃料切换
                String currentFuelId = BlockStorage.getLocationInfo(menu.getLocation(), "fuel_id");
                if (fuelId != null && currentFuelId != null && !fuelId.equals(currentFuelId)) {
                    ConversionUtil.AbstractConversionProcessor oldProcessor = processors.get(currentFuelId);
                    if (oldProcessor != null) {
                        oldProcessor.pause(menu, menu.getLocation(), plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
                    }
                    BlockStorage.addBlockInfo(menu.getLocation(), "fuel_id", null);
                    BlockStorage.addBlockInfo(menu.getLocation(), "rule_id", null);
                    BlockStorage.addBlockInfo(menu.getLocation(), "energy_per_tick", null);
                    BlockStorage.addBlockInfo(menu.getLocation(), "task_state", null);
                    for (ConversionUtil.ConversionRule rule : rules.values()) {
                        BlockStorage.addBlockInfo(menu.getLocation(), "processing_time_" + rule.getRuleId(), null);
                    }
                    saveReason = "输入槽修改（燃料切换，Shift+左键）";
                    menu.save(menu.getLocation());
                }
            }
        }
    }
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event ) {
        if (!(event.getInventory().getHolder() instanceof BlockMenu menu) || !menu.getPreset().getID().equals(getId())) {
            return;
        }
        menu.save(menu.getLocation());
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

    private Location normalizeLocation(Location location) {
        return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}