package me.xingchen.XingchenExpansion.generator.abstractGenerator;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import me.xingchen.XingchenExpansion.util.ConversionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.logging.Level;

/**
 * 抽象发电机类，处理能量生产和物品转化。
 * 自动管理 UI、能量生成和转化流程，支持开箱即用。
 */
public abstract class AbstractGenerator extends SlimefunItem implements EnergyNetProvider {
    protected final int uiSize;
    protected final int[] inputSlots;
    protected final int[] outputSlots;
    protected final int progressSlot;
    protected final int energyCapacity;
    protected final ConversionUtil.TaskManager taskManager;
    protected final int[] borderSlots;
    protected final XingchenExpansion plugin = XingchenExpansion.getInstance();

    public AbstractGenerator(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item, @Nonnull RecipeType recipeType,
                             @Nonnull ItemStack[] recipe, int uiSize, @Nonnull int[] inputSlots, @Nonnull int[] outputSlots,
                             int progressSlot, int energyCapacity,
                             @Nonnull List<ConversionUtil.AbstractConversionProcessor> processors,
                             @Nonnull ConversionUtil.StateItemProvider stateItemProvider,
                             @Nonnull ConversionUtil.SpecialCaseHandler specialCaseHandler) {
        super(itemGroup, item, recipeType, recipe);
        this.uiSize = uiSize;
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
        this.progressSlot = progressSlot;
        this.energyCapacity = energyCapacity;
        this.taskManager = new ConversionUtil.TaskManager(processors, stateItemProvider, specialCaseHandler) {
            @Override
            public ConversionUtil.AbstractConversionProcessor selectProcessor(@Nonnull BlockMenu menu, @Nonnull int[] inputSlots,
                                                                              @Nonnull JavaPlugin plugin, @Nonnull Location location) {
                for (ConversionUtil.AbstractConversionProcessor processor : processors) {
                    if (processor.canOutput(menu, outputSlots, plugin, location)) {
                        plugin.getLogger().fine(String.format("Selected processor %s at %s",
                                processor.getClass().getSimpleName(), location));
                        return processor;
                    }
                }
                return null;
            }
        };
        this.borderSlots = calculateBorderSlots(uiSize, inputSlots, outputSlots, progressSlot);
        validateSlots();
    }

    private int[] calculateBorderSlots(int uiSize, int[] inputSlots, int[] outputSlots, int progressSlot) {
        Set<Integer> functionalSlots = new HashSet<>();
        for (int slot : inputSlots) functionalSlots.add(slot);
        for (int slot : outputSlots) functionalSlots.add(slot);
        functionalSlots.add(progressSlot);

        List<Integer> border = new ArrayList<>();
        for (int i = 0; i < uiSize; i++) {
            if (!functionalSlots.contains(i)) {
                border.add(i);
            }
        }
        return border.stream().mapToInt(Integer::intValue).toArray();
    }

    protected void validateSlots() {
        if (uiSize < 9 || uiSize % 9 != 0) {
            XingchenExpansion.getInstance().getLogger().log(Level.SEVERE, "Invalid UI size for {0}: {1}", new Object[]{getId(), uiSize});
        }
        for (int slot : inputSlots) {
            if (slot < 0 || slot >= uiSize) {
                XingchenExpansion.getInstance().getLogger().log(Level.SEVERE, "Invalid input slot for {0}: {1}", new Object[]{getId(), slot});
            }
        }
        for (int slot : outputSlots) {
            if (slot < 0 || slot >= uiSize) {
                XingchenExpansion.getInstance().getLogger().log(Level.SEVERE, "Invalid output slot for {0}: {1}", new Object[]{getId(), slot});
            }
        }
        if (progressSlot < 0 || progressSlot >= uiSize) {
            XingchenExpansion.getInstance().getLogger().log(Level.SEVERE, "Invalid progress slot for {0}: {1}", new Object[]{getId(), progressSlot});
        }
    }

    protected void constructMenu(@Nonnull BlockMenuPreset preset) {
        for (int slot : borderSlots) {
            preset.addItem(slot, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " "), (p, s, item, action) -> false);
        }
        // 初始化进度槽
        preset.addItem(progressSlot, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "&7Idle"), (p, s, item, action) -> false);
    }


    protected void setupMenuPreset() {
        try {
            new BlockMenuPreset(getId(), getItemName()) {
                @Override
                public void init() {
                    constructMenu(this);
                }

                @Override
                public boolean canOpen(@Nonnull Block b, @Nonnull Player p) {
                    return p.hasPermission("slimefun.inventory.bypass") || Slimefun.getPermissionsService().hasPermission(p, AbstractGenerator.this);
                }

                @Override
                public int[] getSlotsAccessedByItemTransport(@Nonnull ItemTransportFlow flow) {
                    return flow == ItemTransportFlow.INSERT ? inputSlots : outputSlots;
                }

                @Override
                public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block b) {
                    for (int slot : outputSlots) {
                        menu.addMenuClickHandler(slot, (player, s, item, action) -> {
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                ConversionUtil.AbstractConversionProcessor processor = taskManager.selectProcessor(menu, inputSlots, plugin, b.getLocation());
                                if (processor != null && processor.getTaskState(b.getLocation()) == ConversionUtil.TaskState.PAUSED) {
                                    if (processor.canOutput(menu, outputSlots, plugin, b.getLocation())) {
                                        processor.resume(menu, b.getLocation(), plugin, inputSlots, outputSlots);
                                        plugin.getLogger().fine(String.format("Resumed task at %s due to output slot change", b.getLocation()));
                                    }
                                }
                                updateStatus(menu, b.getLocation());
                            }, 1L);
                            return true;
                        });
                    }
                }
            };
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize BlockMenuPreset for {0}: {1}", new Object[]{getId(), e.getMessage()});
        }
    }

    public void updateStatus(@Nonnull BlockMenu menu, @Nonnull Location location) {
        ConversionUtil.AbstractConversionProcessor processor = taskManager.selectProcessor(menu, inputSlots, XingchenExpansion.instance, location);
        ConversionUtil.MachineState state;

        if (processor == null) {
            state = ConversionUtil.MachineState.NO_INPUT;
        } else {
            int energyRequired = processor.getCurrentEnergyChange(menu, inputSlots);
            int currentCharge = getCharge(location);
            boolean canOutput = processor.canOutput(menu, outputSlots, XingchenExpansion.instance, location);
            ConversionUtil.TaskState taskState = processor.getTaskState(location);

            if (!canOutput) {
                state = ConversionUtil.MachineState.OUTPUT_FULL;
            }
            // 根据任务状态设置 CONVERTING 或 IDLE
            else {
                state = taskState == ConversionUtil.TaskState.RUNNING ? ConversionUtil.MachineState.CONVERTING : ConversionUtil.MachineState.IDLE;
            }

            XingchenExpansion.instance.getLogger().info(
                    String.format("Updating status for %s at %s: processor=%s, taskState=%s, energyRequired=%d, currentCharge=%d, canOutput=%b, finalState=%s",
                            getId(), location, processor.getClass().getSimpleName(), taskState,
                            energyRequired, currentCharge, canOutput, state)
            );
        }

        taskManager.updateProgressItem(menu, location, XingchenExpansion.instance, inputSlots, outputSlots, state);
    }

    @Override
    public void preRegister() {
        setupMenuPreset();
        addItemHandler(new BlockTicker() {
            @Override
            public void tick(@Nonnull Block b, @Nonnull SlimefunItem item, @Nonnull Config data) {
                BlockMenu menu = BlockStorage.getInventory(b);
                if (menu != null) {
                    plugin.getLogger().fine(String.format("Ticking %s at %s", getId(), b.getLocation()));
                    process(b.getLocation(), menu, plugin);
                }
            }

            @Override
            public boolean isSynchronized() {
                return true;
            }
        });
        addItemHandler(new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(@Nonnull BlockBreakEvent e, @Nonnull ItemStack item, @Nonnull List<ItemStack> drops) {
                Block b = e.getBlock();
                BlockMenu menu = BlockStorage.getInventory(b);
                if (menu != null) {
                    for (int slot : inputSlots) {
                        ItemStack stack = menu.getItemInSlot(slot);
                        if (stack != null && stack.getType() != Material.AIR) {
                            b.getWorld().dropItemNaturally(b.getLocation(), stack.clone());
                            menu.replaceExistingItem(slot, null);
                        }
                    }
                    for (int slot : outputSlots) {
                        ItemStack stack = menu.getItemInSlot(slot);
                        if (stack != null && stack.getType() != Material.AIR) {
                            b.getWorld().dropItemNaturally(b.getLocation(), stack.clone());
                            menu.replaceExistingItem(slot, null);
                        }
                    }
                    menu.dropItems(b.getLocation(), inputSlots);
                    menu.dropItems(b.getLocation(), outputSlots);
                    BlockStorage.clearBlockInfo(b.getLocation());
                }
            }
        });
    }

    protected void process(@Nonnull Location location, @Nonnull BlockMenu menu, @Nonnull JavaPlugin plugin) {
        int currentCharge = getCharge(location);


        ConversionUtil.AbstractConversionProcessor processor = taskManager.selectProcessor(menu, inputSlots, plugin, location);;
        if (processor == null) {
            taskManager.updateProgressItem(menu, location, plugin, inputSlots, outputSlots, ConversionUtil.MachineState.NO_INPUT);
            return;
        }

        // 检查输出槽
        if (!processor.canOutput(menu, outputSlots, plugin, location)) {
            taskManager.updateProgressItem(menu, location, plugin, inputSlots, outputSlots, ConversionUtil.MachineState.OUTPUT_FULL);
            return;
        }

        // 获取固定能量生成量
        int energyGeneration = processor.getCurrentEnergyChange(menu, inputSlots);
        if (energyGeneration <= 0) {
            taskManager.updateProgressItem(menu, location, plugin, inputSlots, outputSlots, ConversionUtil.MachineState.NO_INPUT);
            return;
        }

        // 执行处理并生成能量
        boolean completed = taskManager.process(menu, location, plugin, inputSlots, outputSlots);
        if (completed) {
            int newCharge = Math.min(currentCharge + energyGeneration, energyCapacity);
            setCharge(location, newCharge);
        }
        // 强制更新 UI
        updateStatus(menu, location);
    }

    private int getEnergyChange(@Nonnull ConversionUtil.AbstractConversionProcessor processor, @Nonnull BlockMenu menu) {
        return processor.getCurrentEnergyChange(menu, inputSlots);
    }

    @Override
    public int getCharge(@Nonnull Location location) {
        String charge = BlockStorage.getLocationInfo(location, "energy-charge");
        try {
            int value = charge != null ? Integer.parseInt(charge) : 0;
            return value;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void setCharge(@Nonnull Location location, int charge) {
        int cappedCharge = Math.max(0, Math.min(charge, getCapacity()));
        BlockStorage.addBlockInfo(location, "energy-charge", String.valueOf(cappedCharge));
    }

    @Override
    public int getGeneratedOutput(@Nonnull Location location, @Nonnull Config data) {
        BlockMenu menu = BlockStorage.getInventory(location.getBlock());
        if (menu == null) {
            return 0;
        }

        ConversionUtil.AbstractConversionProcessor processor = taskManager.selectProcessor(menu, inputSlots, plugin, location);
        if (processor == null || !processor.canOutput(menu, outputSlots, plugin, location)) {
            return 0;
        }

        int energyGeneration = processor.getCurrentEnergyChange(menu, inputSlots);
        if (energyGeneration <= 0) {
            return 0;
        }
        return energyGeneration;
    }

    @Override
    public int getCapacity() {
        return energyCapacity;
    }
    public ConversionUtil.MachineState getMachineState(@Nonnull Location location) {
        BlockMenu menu = BlockStorage.getInventory(location.getBlock());
        if (menu == null) {
            plugin.getLogger().fine(String.format("No BlockMenu at %s", location));
            return ConversionUtil.MachineState.NO_INPUT;
        }
        ConversionUtil.AbstractConversionProcessor processor = taskManager.selectProcessor(menu, inputSlots, plugin, location);
        if (processor == null) {
            return ConversionUtil.MachineState.NO_INPUT;
        }
        return processor.canOutput(menu, outputSlots, plugin, location) ?
                ConversionUtil.MachineState.CONVERTING : ConversionUtil.MachineState.OUTPUT_FULL;
    }

    public ConversionUtil.TaskState getTaskState(@Nonnull Location location) {
        BlockMenu menu = BlockStorage.getInventory(location.getBlock());
        if (menu == null) {
            plugin.getLogger().fine(String.format("No BlockMenu at %s", location));
            return ConversionUtil.TaskState.IDLE;
        }
        ConversionUtil.AbstractConversionProcessor processor = taskManager.selectProcessor(menu, inputSlots, plugin, location);
        if (processor == null) {
            String taskStateStr = BlockStorage.getLocationInfo(location, "task_state");
            try {
                return taskStateStr != null ? ConversionUtil.TaskState.valueOf(taskStateStr) : ConversionUtil.TaskState.IDLE;
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning(String.format("Invalid task state at %s: %s, resetting to IDLE", location, taskStateStr));
                BlockStorage.addBlockInfo(location, "task_state", ConversionUtil.TaskState.IDLE.name());
                return ConversionUtil.TaskState.IDLE;
            }
        }
        return processor.getTaskState(location);
    }

    protected abstract List<ConversionUtil.AbstractConversionProcessor> getProcessors();
}