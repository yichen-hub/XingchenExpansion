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
import java.util.Arrays;
import java.util.List;

public abstract class StarsQuarry extends SlimefunItem implements EnergyNetComponent, Listener {
    protected static final int STATUS_SLOT = 13;
    protected static final int[] OUTPUT_SLOTS = {22};
    protected static final int[] PROTECTED_SLOTS = {STATUS_SLOT, 22};
    protected final BlockMenuPreset preset;
    protected final int energyConsumption;
    protected final JavaPlugin plugin;
    protected final String quarryId;
    protected final ConversionUtil.ConversionProcessor processor;
    protected final String outputId;

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
            return;
        }

        this.energyConsumption = energyConsumption;
        this.outputId = outputId;

        ConversionUtil.ConversionRule rule = new ConversionUtil.ConversionRule(
                null, // 无输入
                List.of(outputId),
                null,
                List.of(outputAmount),
                ConversionUtil.OutputMode.ALL,
                processingTime
        );
        this.processor = new ConversionUtil.NoInputProcessor(List.of(rule));

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
            public void onPlayerBreak(BlockBreakEvent e, ItemStack item, java.util.List<ItemStack> drops) {
                Block b = e.getBlock();
                BlockMenu inv = BlockStorage.getInventory(b);
                if (inv != null) {
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
            } catch (IllegalStateException e) {
                return;
            }
        }

        // 检查输出槽是否满
        BlockMenu finalMenu = menu;
        boolean outputFull = Arrays.stream(OUTPUT_SLOTS)
                .allMatch(slot -> {
                    ItemStack item = finalMenu.getItemInSlot(slot);
                    return item != null && item.getAmount() >= item.getMaxStackSize();
                });

        // 检查是否需要处理
        ConversionUtil.ProgressInfo progress = processor.getProgressInfo(location, plugin);
        boolean shouldProcess = !outputFull && (!progress.isProcessing() || progress.getCurrentTicks() > 0);

        if (shouldProcess && !takeCharge(location)) {
            updateStatus(menu, outputFull);
            return;
        }

        boolean processed = shouldProcess && processor.process(menu, location, plugin, new int[0], OUTPUT_SLOTS);
        updateStatus(menu, outputFull);
    }

    protected void updateStatus(BlockMenu menu, boolean outputFull) {
        Location location = menu.getLocation();
        ConversionUtil.ProgressInfo progress = processor.getProgressInfo(location, plugin);
        int currentCharge = getCharge(location);
        String statusText;

        String powerPerTick = LoreBuilder.powerPerSecond(energyConsumption);
        String powerCharged = LoreBuilder.powerCharged(currentCharge, getCapacity());

        if (outputFull) {
            statusText = "§c待机 §e(输出槽已满)";
        } else if (progress.isProcessing() && currentCharge >= energyConsumption) {
            statusText = "§a运行中 §e(剩余: " + String.format("%.1f", progress.getRemainingSeconds()) + "秒)";
        } else {
            statusText = currentCharge >= energyConsumption ? "§a待机" : "§c待机 §e(能量不足)";
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
        for (int slot : OUTPUT_SLOTS) {
            preset.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }
    }

    public void newItemMenu(BlockMenu menu) {
        for (int i = 0; i < 27; i++) {
            int finalI = i;
            if (i == STATUS_SLOT || Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == finalI)) continue;
            ItemStack existing = menu.getItemInSlot(i);
            if (existing == null || existing.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                menu.replaceExistingItem(i, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " "));
                menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
            }
        }
        ItemStack status = new CustomItemStack(Material.REDSTONE, "§c状态: 待机");
        menu.replaceExistingItem(STATUS_SLOT, status);
        menu.addMenuClickHandler(STATUS_SLOT, ChestMenuUtils.getEmptyClickHandler());
        for (int slot : OUTPUT_SLOTS) {
            menu.replaceExistingItem(slot, null);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (Util.protectOutputSlots(event, plugin, OUTPUT_SLOTS, outputId,
                menu -> menu.getPreset().getID().equals(getId()))) {
            return;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Util.protectOutputSlots(event, plugin, OUTPUT_SLOTS, outputId,
                menu -> menu.getPreset().getID().equals(getId()));
    }
}