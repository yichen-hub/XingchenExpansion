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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.logging.Level;

public abstract class StarsQuarry extends SlimefunItem implements EnergyNetComponent, Listener {
    protected static final int STATUS_SLOT = 13;
    protected static final int[] OUTPUT_SLOTS = {22};
    protected static final int[] PROTECTED_SLOTS = {STATUS_SLOT, 22};
    protected final BlockMenuPreset preset;
    protected final int energyConsumption;
    protected final JavaPlugin plugin;
    protected final String quarryId;
    protected final ConversionUtil.AbstractConversionProcessor processor;
    protected final String outputId;
    protected final ConversionUtil.StateItemProvider stateItemProvider;
    protected final ConversionUtil.SpecialCaseHandler specialCaseHandler;

    public StarsQuarry(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe,
                       XingchenExpansion plugin, String quarryId) {
        super(itemGroup, item, recipeType, recipe);
        this.plugin = plugin;
        this.quarryId = quarryId;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        File configFile = new File(plugin.getDataFolder(), "stars_quarry.yml");
        if (!configFile.exists()) {
            plugin.saveResource("stars_quarry.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        var section = config.getConfigurationSection("quarries." + quarryId);
        if (section == null) {
            plugin.getLogger().severe("未找到 quarries." + quarryId + " 的配置，矿机将被禁用！");
            this.energyConsumption = 0;
            this.preset = null;
            this.processor = null;
            this.outputId = null;
            this.stateItemProvider = null;
            this.specialCaseHandler = null;
            return;
        }

        int energyConsumption = section.getInt("energy_consumption");
        int processingTime = section.getInt("processing_time");
        String outputId = section.getString("output_id");
        int outputAmount = section.getInt("output_amount");

        if (energyConsumption <= 0 || processingTime <= 0 || outputId == null || outputAmount <= 0) {
            plugin.getLogger().severe("无效的矿机配置，ID: " + quarryId + "，矿机将被禁用！");
            this.energyConsumption = 0;
            this.preset = null;
            this.processor = null;
            this.outputId = null;
            this.stateItemProvider = null;
            this.specialCaseHandler = null;
            return;
        }

        this.energyConsumption = energyConsumption;
        this.outputId = outputId;
        this.stateItemProvider = new QuarryStateItemProvider();
        this.specialCaseHandler = new QuarrySpecialCaseHandler();

        ConversionUtil.ConversionRule rule = new ConversionUtil.ConversionRule(
                Collections.emptyList(),
                List.of(new ConversionUtil.SlimefunItemMatcherImpl(outputId)),
                Collections.emptyList(),
                List.of(outputAmount),
                ConversionUtil.OutputMode.ALL,
                processingTime,
                STATUS_SLOT,
                true
        );
        this.processor = new ConversionUtil.NoInputProcessor(
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
                StarsQuarry.this.newItemMenu(menu);
            }

            @Override
            public boolean canOpen(Block b, Player p) {
                return p.hasPermission("slimefun.inventory.bypass") ||
                        Slimefun.getPermissionsService().hasPermission(p, StarsQuarry.this);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                return flow == ItemTransportFlow.WITHDRAW ? OUTPUT_SLOTS : new int[0];
            }

            @Override
            public int getSize() {
                return 27;
            }
        };

        addItemHandler(new BlockTicker() {
            @Override
            public void tick(Block b, SlimefunItem sf, me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config data) {
                StarsQuarry.this.tick(b);
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
                    inv.dropItems(b.getLocation(), OUTPUT_SLOTS);
                    processor.terminateForce(inv, b.getLocation(), plugin, new int[0], OUTPUT_SLOTS);
                }
                BlockStorage.clearBlockInfo(b);
            }
        });
    }

    private class QuarryStateItemProvider implements ConversionUtil.StateItemProvider {
        @Override
        @Nonnull
        public Map<ConversionUtil.MachineState, ItemStack> getStateItems() {
            Map<ConversionUtil.MachineState, ItemStack> items = new HashMap<>();
            items.put(ConversionUtil.MachineState.CONVERTING, new CustomItemStack(
                    Material.LIME_DYE, "§a运行中",
                    "§7状态: 运行中", "§7" + LoreBuilder.powerPerSecond(energyConsumption)
            ));
            items.put(ConversionUtil.MachineState.OUTPUT_FULL, new CustomItemStack(
                    Material.REDSTONE, "§c暂停",
                    "§7状态: 输出槽已满", "§7" + LoreBuilder.powerPerSecond(energyConsumption)
            ));
            items.put(ConversionUtil.MachineState.IDLE, new CustomItemStack(
                    Material.REDSTONE, "§c待机",
                    "§7状态: 待机", "§7" + LoreBuilder.powerPerSecond(energyConsumption)
            ));
            items.put(ConversionUtil.MachineState.NO_ENERGY, new CustomItemStack(
                    Material.REDSTONE, "§c能量不足",
                    "§7状态: 能量不足", "§7" + LoreBuilder.powerPerSecond(energyConsumption)
            ));
            return items;
        }
    }

    private class QuarrySpecialCaseHandler implements ConversionUtil.SpecialCaseHandler {
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
            } catch (IllegalStateException e) {
                plugin.getLogger().log(Level.WARNING, "无法创建 BlockMenu: " + location, e);
                return;
            }
        }

        // 获取状态和规则
        ConversionUtil.TaskState taskState = processor.getTaskState(location);
        ConversionUtil.ProgressInfo progress = processor.getProgressInfo(location, plugin, new int[0], OUTPUT_SLOTS);
        String ruleId = progress.getRuleId();
        ConversionUtil.ConversionRule activeRule = null;
        if (ruleId != null) {
            activeRule = processor.getRules().stream()
                    .filter(r -> r.getRuleId().equals(ruleId))
                    .findFirst()
                    .orElse(null);
        }

        // 检查能量
        if (!takeCharge(location)) {
            if (taskState == ConversionUtil.TaskState.RUNNING) {
                processor.pause(menu, location, plugin, new int[0], OUTPUT_SLOTS);
            }
            updateStatus(menu, location);
            return;
        }

        // 检查输出槽
        if (activeRule != null && !processor.canOutput(menu, OUTPUT_SLOTS, activeRule, plugin, location)) {
            if (taskState == ConversionUtil.TaskState.RUNNING) {
                processor.pause(menu, location, plugin, new int[0], OUTPUT_SLOTS);
            }
            updateStatus(menu, location);
            return;
        }

        // 恢复运行
        if (taskState == ConversionUtil.TaskState.PAUSED && activeRule != null) {
            processor.resume(menu, location, plugin, new int[0], OUTPUT_SLOTS);
        }

        // 处理转化
        boolean processed = processor.process(menu, location, plugin, new int[0], OUTPUT_SLOTS);

        // 更新状态
        updateStatus(menu, location);
    }

    protected void updateStatus(@Nonnull BlockMenu menu, @Nonnull Location location) {
        ConversionUtil.ProgressInfo progress = processor.getProgressInfo(location, plugin, new int[0], OUTPUT_SLOTS);
        int currentCharge = getCharge(location);

        ItemStack statusItem = specialCaseHandler.handleSpecialCase(progress).orElseGet(() -> {
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
            if (i == STATUS_SLOT || Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == finalI)) continue;
            preset.addItem(i, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " "), ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addMenuClickHandler(STATUS_SLOT, ChestMenuUtils.getEmptyClickHandler());
    }

    public void newItemMenu(@Nonnull BlockMenu menu) {
        for (int i = 0; i < 27; i++) {
            int finalI = i;
            if (i == STATUS_SLOT || Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == finalI)) continue;
            ItemStack existing = menu.getItemInSlot(i);
            if (existing == null || existing.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                menu.replaceExistingItem(i, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " "));
                menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
            }
        }
        menu.replaceExistingItem(STATUS_SLOT, new CustomItemStack(Material.REDSTONE, "§c状态: 待机"));
        menu.addMenuClickHandler(STATUS_SLOT, ChestMenuUtils.getEmptyClickHandler());
        for (int slot : OUTPUT_SLOTS) {
            menu.replaceExistingItem(slot, null);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BlockMenu menu) || !menu.getPreset().getID().equals(getId())) {
            return;
        }
        menu.save(menu.getLocation());
    }
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {

    }
}