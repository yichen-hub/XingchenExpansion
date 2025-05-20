package me.xingchen.XingchenExpansion.machine.abstractMachine;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
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
import me.xingchen.XingchenExpansion.XingchenExpansion;
import me.xingchen.XingchenExpansion.util.ConversionUtil;
import me.xingchen.XingchenExpansion.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.logging.Level;


public abstract class StarsPurifier extends SlimefunItem implements EnergyNetComponent, Listener {
    protected static final int INPUT_SLOT = 10;
    protected static final int STATUS_SLOT = 13;
    protected static final int[] OUTPUT_SLOTS = {14, 15, 16};
    protected static final int[] PROTECTED_SLOTS = {INPUT_SLOT, STATUS_SLOT, 14, 15, 16};
    protected final BlockMenuPreset preset;
    protected final int energyConsumption;
    protected final Set<Location> purifierLocations = new HashSet<>();
    protected final JavaPlugin plugin;
    protected final String purifierId;
    protected final ConversionUtil.AbstractConversionProcessor processor;
    protected final String outputId;
    protected final String inputId;
    protected final ConversionUtil.StateItemProvider stateItemProvider;
    protected final ConversionUtil.SpecialCaseHandler specialCaseHandler;

    public StarsPurifier(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe,
                         XingchenExpansion plugin, String purifierId) {
        super(itemGroup, item, recipeType, recipe);
        this.plugin = plugin;
        this.purifierId = purifierId;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        File configFile = new File(plugin.getDataFolder(), "purifiers.yml");
        if (!configFile.exists()) {
            plugin.saveResource("purifiers.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        var section = config.getConfigurationSection("purifiers." + purifierId);
        if (section == null) {
            plugin.getLogger().severe("未找到 purifiers." + purifierId + " 的配置，净化器将被禁用！");
            this.energyConsumption = 0;
            this.preset = null;
            this.processor = null;
            this.outputId = null;
            this.inputId = null;
            this.stateItemProvider = null;
            this.specialCaseHandler = null;
            return;
        }

        int energyConsumption = section.getInt("energy_consumption");
        int processingTime = section.getInt("processing_time");
        String inputId = section.getString("input_id");
        String outputId = section.getString("output_id");
        int outputAmount = section.getInt("output_amount");

        if (energyConsumption <= 0 || processingTime <= 0 || inputId == null || outputId == null || outputAmount <= 0) {
            plugin.getLogger().severe("无效的净化器配置，ID: " + purifierId + "，净化器将被禁用！");
            this.energyConsumption = 0;
            this.preset = null;
            this.processor = null;
            this.outputId = null;
            this.inputId = null;
            this.stateItemProvider = null;
            this.specialCaseHandler = null;
            return;
        }

        if (SlimefunItem.getById(inputId) == null) {
            plugin.getLogger().severe("无效输入ID: " + inputId + "，净化器将被禁用！");
            this.energyConsumption = 0;
            this.preset = null;
            this.processor = null;
            this.outputId = null;
            this.inputId = null;
            this.stateItemProvider = null;
            this.specialCaseHandler = null;
            return;
        }
        if (SlimefunItem.getById(outputId) == null) {
            plugin.getLogger().severe("无效输出ID: " + outputId + "，净化器将被禁用！");
            this.energyConsumption = 0;
            this.preset = null;
            this.processor = null;
            this.outputId = null;
            this.inputId = null;
            this.stateItemProvider = null;
            this.specialCaseHandler = null;
            return;
        }

        this.energyConsumption = energyConsumption;
        this.outputId = outputId;
        this.inputId = inputId;
        this.stateItemProvider = new PurifierStateItemProvider();
        this.specialCaseHandler = new PurifierSpecialCaseHandler();

        ConversionUtil.ConversionRule rule = new ConversionUtil.ConversionRule(
                List.of(new ConversionUtil.SlimefunItemMatcherImpl(inputId)),
                List.of(new ConversionUtil.SlimefunItemMatcherImpl(outputId)),
                List.of(1),
                List.of(outputAmount),
                ConversionUtil.OutputMode.ALL,
                processingTime,
                STATUS_SLOT,
                true
        );
        this.processor = new ConversionUtil.SingleInputProcessor(
                List.of(rule),
                stateItemProvider,
                specialCaseHandler
        );

        this.preset = new BlockMenuPreset(getId(), item.getDisplayName()) {
            @Override
            public void init() {
                setupMenuPreset();
            }

            @Override
            public void newInstance(BlockMenu menu, Block b) {
                StarsPurifier.this.newItemMenu(menu);
            }

            @Override
            public boolean canOpen(Block b, Player p) {
                return p.hasPermission("slimefun.inventory.bypass") ||
                        Slimefun.getPermissionsService().hasPermission(p, StarsPurifier.this);
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

        addItemHandler(new BlockTicker() {
            @Override
            public void tick(Block b, SlimefunItem sf, me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config data) {
                StarsPurifier.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return true;
            }
        });

        addItemHandler(new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(BlockBreakEvent e, ItemStack item, List<ItemStack> drops) {
                Block b = e.getBlock();
                BlockMenu inv = BlockStorage.getInventory(b);
                if (inv != null) {
                    inv.dropItems(b.getLocation(), INPUT_SLOT);
                    inv.dropItems(b.getLocation(), OUTPUT_SLOTS);
                    processor.terminateWithReturn(inv, b.getLocation(), plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
                }
                BlockStorage.clearBlockInfo(b);
                purifierLocations.remove(normalizeLocation(b.getLocation()));
            }
        });
    }

    private class PurifierStateItemProvider implements ConversionUtil.StateItemProvider {
        @Override
        @Nonnull
        public Map<ConversionUtil.MachineState, ItemStack> getStateItems() {
            Map<ConversionUtil.MachineState, ItemStack> items = new HashMap<>();
            items.put(ConversionUtil.MachineState.CONVERTING, new CustomItemStack(
                    Material.LIME_DYE, "§a运行中",
                    "§7状态: 运行中", "§7" + LoreBuilder.powerPerSecond(energyConsumption)+"&7"
            ));
            items.put(ConversionUtil.MachineState.OUTPUT_FULL, new CustomItemStack(
                    Material.REDSTONE, "§c暂停",
                    "§7状态: 输出槽已满", "&7" + LoreBuilder.powerPerSecond(energyConsumption)+"&7"
            ));
            items.put(ConversionUtil.MachineState.IDLE, new CustomItemStack(
                    Material.REDSTONE, "§c待机",
                    "§7状态: 待机", "&7" + LoreBuilder.powerPerSecond(energyConsumption)+"&7"
            ));
            items.put(ConversionUtil.MachineState.NO_INPUT, new CustomItemStack(
                    Material.REDSTONE, "§c无输入",
                    "§7状态: 缺少输入", "&7" + LoreBuilder.powerPerSecond(energyConsumption)+"&7"
            ));
            items.put(ConversionUtil.MachineState.NO_ENERGY, new CustomItemStack(
                    Material.REDSTONE, "§c能量不足",
                    "§7状态: 能量不足", "&7" + LoreBuilder.powerPerSecond(energyConsumption)+"&7"
            ));
            return items;
        }
    }

    private class PurifierSpecialCaseHandler implements ConversionUtil.SpecialCaseHandler {
        @Override
        @Nonnull
        public Optional<ItemStack> handleSpecialCase(@Nonnull ConversionUtil.ProgressInfo info) {
            return Optional.empty();
        }
    }

    public BlockMenuPreset getPreset() {
        return preset;
    }

    protected void tick(Block b) {
        if (preset == null || processor == null) return;
        process(b.getLocation());
    }

    protected void process(@Nonnull Location location) {
        if (preset == null || processor == null) return;

        if (BlockStorage.getLocationInfo(location, "id") == null) {
            BlockStorage.addBlockInfo(location, "id", getId());
            BlockStorage.addBlockInfo(location, "energy", "0");
        }

        BlockMenu menu = BlockStorage.getInventory(location.getBlock());
        if (menu == null) {
            try {
                menu = new BlockMenu(preset, location);
                newItemMenu(menu);
                purifierLocations.add(normalizeLocation(location));
            } catch (IllegalStateException e) {
                plugin.getLogger().log(Level.WARNING, "无法创建 BlockMenu: " + location, e);
                return;
            }
        }

        // 获取状态和规则
        ConversionUtil.TaskState taskState = processor.getTaskState(location);
        ConversionUtil.ProgressInfo progress = processor.getProgressInfo(location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
        String ruleId = progress.getRuleId();
        ConversionUtil.ConversionRule activeRule = null;
        if (ruleId != null) {
            activeRule = processor.getRules().stream()
                    .filter(r -> r.getRuleId().equals(ruleId))
                    .findFirst()
                    .orElse(null);
            if (activeRule == null) {
            }
        }

        // 检查输入
        ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
        if (inputItem != null && activeRule == null) {
            activeRule = processor.getRules().stream()
                    .filter(r -> r.getInputItems().stream().anyMatch(m -> m.matches(inputItem)))
                    .findFirst()
                    .orElse(null);
        }

        boolean hasValidInput = inputItem != null && activeRule != null &&
                activeRule.getInputItems().stream().anyMatch(matcher -> {
                    boolean matches = matcher.matches(inputItem);
                    return matches;
                });

        // 无输入时重置状态
        if (!hasValidInput && taskState != ConversionUtil.TaskState.IDLE) {
            processor.terminateForce(menu, location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
            updateStatus(menu, location);
            return;
        }

        // 暂停状态跳过处理
        if (taskState == ConversionUtil.TaskState.PAUSED) {
            updateStatus(menu, location);
            return;
        }

        // 检查输出
        BlockMenu finalMenu = menu;
        boolean canOutput = activeRule == null || activeRule.getOutputMode() == ConversionUtil.OutputMode.NONE ||
                Arrays.stream(OUTPUT_SLOTS).anyMatch(slot -> {
                    ItemStack item = finalMenu.getItemInSlot(slot);
                    return item == null || item.getType() == Material.AIR || item.getAmount() < item.getMaxStackSize();
                });
        if (!canOutput) {
            StringBuilder outputStatus = new StringBuilder("输出槽状态: ");
            for (int slot : OUTPUT_SLOTS) {
                ItemStack item = menu.getItemInSlot(slot);
                outputStatus.append("槽").append(slot).append("=");
                if (item == null || item.getType() == Material.AIR) {
                    outputStatus.append("空, ");
                } else {
                    outputStatus.append(Util.getSlimefunId(item)).append("x").append(item.getAmount()).append(", ");
                }
            }
            plugin.getLogger().info(outputStatus.toString() + ", 位置=" + location);
        }

        if (!hasValidInput || !canOutput) {
            if (taskState == ConversionUtil.TaskState.RUNNING) {
                processor.pause(menu, location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
            }
            if (hasValidInput && !canOutput) {
                progress = processor.getProgressInfo(location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
            }
            updateStatus(menu, location);
            return;
        }

        // 检查能量
        if (!takeCharge(location)) {
            if (taskState == ConversionUtil.TaskState.RUNNING) {
                processor.pause(menu, location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
            }
            updateStatus(menu, location);
            return;
        }

        // 启动或恢复转化
        if ((taskState == ConversionUtil.TaskState.IDLE || taskState == ConversionUtil.TaskState.PAUSED) && hasValidInput && canOutput) {
            processor.resume(menu, location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
            menu.save(menu.getLocation());
        }

        // 处理转化
        boolean processed = processor.process(menu, location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
        menu.save(menu.getLocation());

        // 更新状态
        updateStatus(menu, location);
    }

    protected void updateStatus(@Nonnull BlockMenu menu, @Nonnull Location location) {
        ConversionUtil.ProgressInfo progress = processor.getProgressInfo(location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
        int currentCharge = getCharge(location);

        ItemStack statusItem = specialCaseHandler.handleSpecialCase(progress).orElseGet(() -> {
            Map<ConversionUtil.MachineState, ItemStack> stateItems = stateItemProvider.getStateItems();
            ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
            boolean hasValidInput = inputItem != null && processor.getRules().stream()
                    .anyMatch(r -> r.getInputItems().stream().anyMatch(m -> m.matches(inputItem)));
            boolean isOutputFull = hasValidInput && Arrays.stream(OUTPUT_SLOTS).allMatch(slot -> {
                ItemStack item = menu.getItemInSlot(slot);
                return item != null && item.getType() != Material.AIR && item.getAmount() >= item.getMaxStackSize();
            });
            if (isOutputFull) {
                return stateItems.get(ConversionUtil.MachineState.OUTPUT_FULL);
            }
            if (inputItem == null || inputItem.getType() == Material.AIR) {
                return stateItems.get(ConversionUtil.MachineState.NO_INPUT);
            }
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
            lore.add(statusIndex >= 0 ? statusIndex + 1 : lore.size(), "§7" + LoreBuilder.powerCharged(currentCharge, getCapacity()));
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

    protected boolean takeCharge(@Nonnull Location location) {
        if (!isChargeable()) return true;
        int charge = getCharge(location);
        if (charge < energyConsumption) {
            return false;
        }
        setCharge(location, charge - energyConsumption);
        return true;
    }

    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        return energyConsumption * 2;
    }

    @Override
    public boolean isChargeable() {
        return true;
    }

    @Override
    public int getCharge(@Nonnull Location l) {
        String charge = BlockStorage.getLocationInfo(l, "energy");
        try {
            return charge != null ? Integer.parseInt(charge) : 0;
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("能量格式错误: 值=" + charge + ", 位置=" + l);
            return 0;
        }
    }

    @Override
    public void setCharge(@Nonnull Location l, int charge) {
        if (charge < 0) charge = 0;
        BlockStorage.addBlockInfo(l, "energy", String.valueOf(charge));
    }

    protected void setupMenuPreset() {
        if (preset == null) return;
        for (int i = 0; i < 27; i++) {
            int finalI = i;
            if (i == INPUT_SLOT || i == STATUS_SLOT || Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == finalI)) continue;
            preset.addItem(i, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " "), ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addMenuClickHandler(INPUT_SLOT, (p, slot, item, action) -> true);
        for (int slot : OUTPUT_SLOTS) {
            preset.addMenuClickHandler(slot, (p, s, item, action) -> true);
        }
        preset.addMenuClickHandler(STATUS_SLOT, ChestMenuUtils.getEmptyClickHandler());
    }

    public void newItemMenu(@Nonnull BlockMenu menu) {
        for (int i = 0; i < 27; i++) {
            int finalI = i;
            if (i == INPUT_SLOT || i == STATUS_SLOT || Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == finalI)) continue;
            ItemStack existing = menu.getItemInSlot(i);
            if (existing == null || existing.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                menu.replaceExistingItem(i, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " "));
                menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
            }
        }
        menu.replaceExistingItem(INPUT_SLOT, null);
        menu.addMenuClickHandler(INPUT_SLOT, (p, slot, item, action) -> true);
        menu.replaceExistingItem(STATUS_SLOT, new CustomItemStack(Material.REDSTONE, "§c状态: 待机"));
        menu.addMenuClickHandler(STATUS_SLOT, ChestMenuUtils.getEmptyClickHandler());
        for (int slot : OUTPUT_SLOTS) {
            menu.replaceExistingItem(slot, null);
            menu.addMenuClickHandler(slot, (p, s, item, action) -> true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
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
                List<ConversionUtil.ConversionRule> rules = processor.getRules();
                if (rules.isEmpty()) {
                    event.setCancelled(true);
                    player.sendMessage("§c净化器无有效规则！");
                    return;
                }
                ConversionUtil.ConversionRule rule = rules.get(0);
                boolean matchesInput = rule.getInputItems().stream().anyMatch(matcher -> matcher.matches(current));
                boolean canInsertInput = inputItem == null || inputItem.getType() == Material.AIR ||
                        (inputItem.isSimilar(current) && inputItem.getAmount() < inputItem.getMaxStackSize() && matchesInput);
                if (!canInsertInput) {
                    event.setCancelled(true);
                    player.sendMessage("§c输入槽已满或不可存入！");
                }
            }
        }
        menu.save(menu.getLocation());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {

    }

    private Location normalizeLocation(Location location) {
        return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}