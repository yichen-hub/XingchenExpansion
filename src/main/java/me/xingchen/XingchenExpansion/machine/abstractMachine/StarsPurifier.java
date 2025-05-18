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
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class StarsPurifier extends SlimefunItem implements EnergyNetComponent, Listener {
    protected static final int INPUT_SLOT = 10;
    protected static final int STATUS_SLOT = 13;
    protected static final int[] OUTPUT_SLOTS = {14, 15, 16};
    protected static final int[] PROTECTED_SLOTS = {INPUT_SLOT, STATUS_SLOT, 14, 15, 16};
    protected final BlockMenuPreset preset;
    protected final int energyConsumption;
    protected final List<Location> purifierLocations = new ArrayList<>();
    protected final JavaPlugin plugin;
    protected final String purifierId;
    protected final ConversionUtil.ConversionProcessor processor;
    protected final String outputId;

    public StarsPurifier(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, XingchenExpansion plugin, String purifierId) {
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
            return;
        }

        this.energyConsumption = energyConsumption;
        this.outputId = outputId;

        ConversionUtil.ConversionRule rule = new ConversionUtil.ConversionRule(
                List.of(inputId),
                List.of(outputId),
                List.of(1),
                List.of(outputAmount),
                ConversionUtil.OutputMode.ALL, processingTime
        );
        this.processor = new ConversionUtil.SingleInputProcessor(List.of(rule));

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
                return p.hasPermission("slimefun.inventory.bypass") || Slimefun.getPermissionsService().hasPermission(p, StarsPurifier.this);
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
                }
                BlockStorage.clearBlockInfo(b);
            }
        });
    }

    public BlockMenuPreset getPreset() {
        return preset;
    }

    protected void tick(Block b) {
        if (preset == null || processor == null) return;
        process(b.getLocation());
    }

    public void process(@Nonnull Location location) {
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
                purifierLocations.add(location.clone());
            } catch (IllegalStateException e) {
                return;
            }
        }

        // 检查是否需要处理
        ConversionUtil.ProgressInfo progress = processor.getProgressInfo(location, plugin);
        ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
        String inputId = Util.getSlimefunId(inputItem);
        String expectedInputId = processor.getRules().get(0).getInputItems().get(0);
        boolean shouldProcess = progress.isProcessing() || (inputId != null && inputId.equals(expectedInputId));

        if (shouldProcess && !takeCharge(location)) {
            updateStatus(menu);
            return;
        }

        boolean processed = shouldProcess && processor.process(menu, location, plugin, new int[]{INPUT_SLOT}, OUTPUT_SLOTS);
        updateStatus(menu);
    }
    protected void updateStatus(BlockMenu menu) {
        ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
        Location location = menu.getLocation();
        ConversionUtil.ProgressInfo progress = processor.getProgressInfo(location, plugin);
        String statusText = "§c待机";
        int currentCharge = getCharge(location);

        String powerPerTick = LoreBuilder.powerPerSecond(energyConsumption);
        String powerCharged = LoreBuilder.powerCharged(currentCharge, getCapacity());

        String inputId = Util.getSlimefunId(inputItem);
        if ((inputId != null || progress.isProcessing()) && currentCharge >= energyConsumption) {
            statusText = progress.isProcessing() ?
                    "§a运行中 §e(剩余: " + String.format("%.1f", progress.getRemainingSeconds()) + "秒)" :
                    "§a运行中";
        } else {
            statusText = inputId == null ? "§c待机 §e(无有效输入)" : "§c待机 §e(能量不足)";
        }

        ItemStack status = new CustomItemStack(Material.REDSTONE, "§c状态: " + statusText, powerPerTick, powerCharged);
        menu.replaceExistingItem(STATUS_SLOT, status);
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
            int value = charge != null ? Integer.parseInt(charge) : 0;
            return value;
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
            preset.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addMenuClickHandler(STATUS_SLOT, ChestMenuUtils.getEmptyClickHandler());
    }

    public void newItemMenu(BlockMenu menu) {
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
        ItemStack status = new CustomItemStack(Material.REDSTONE, "§c状态: 待机");
        menu.replaceExistingItem(STATUS_SLOT, status);
        menu.addMenuClickHandler(STATUS_SLOT, ChestMenuUtils.getEmptyClickHandler());
        for (int slot : OUTPUT_SLOTS) {
            menu.replaceExistingItem(slot, null);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (Util.protectOutputSlots(event, plugin, OUTPUT_SLOTS, outputId, menu -> menu.getPreset().getID().equals(getId()))) {
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
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getRawSlots().contains(INPUT_SLOT)) {
            return;
        }
        Util.protectOutputSlots(event, plugin, OUTPUT_SLOTS, outputId, menu -> menu.getPreset().getID().equals(getId()));
    }
}