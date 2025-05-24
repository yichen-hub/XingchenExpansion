package me.xingchen.XingchenExpansion.machine.abstractMachine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
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
 * 抽象机器类，处理物品转化和能量消耗。
 * 自动管理 UI、能量检查和转化流程，支持开箱即用。
 */
public abstract class AbstractMachine extends SlimefunItem implements EnergyNetComponent {
    protected final int uiSize;
    protected final int[] inputSlots;
    protected final int[] outputSlots;
    protected final int progressSlot;
    protected final int energyBuffer;
    protected final ConversionUtil.TaskManager taskManager;
    protected final int[] borderSlots;

    public AbstractMachine(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item, @Nonnull RecipeType recipeType,
                           @Nonnull ItemStack[] recipe, int uiSize, @Nonnull int[] inputSlots, @Nonnull int[] outputSlots,
                           int progressSlot, int energyBuffer,
                           @Nonnull List<ConversionUtil.AbstractConversionProcessor> processors,
                           @Nonnull ConversionUtil.StateItemProvider stateItemProvider,
                           @Nonnull ConversionUtil.SpecialCaseHandler specialCaseHandler) {
        super(itemGroup, item, recipeType, recipe);
        this.uiSize = uiSize;
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
        this.progressSlot = progressSlot;
        this.energyBuffer = energyBuffer;
        this.taskManager = new ConversionUtil.TaskManager(processors, stateItemProvider, specialCaseHandler) {
            @Override
            public ConversionUtil.AbstractConversionProcessor selectProcessor(@Nonnull BlockMenu menu, @Nonnull int[] inputSlots,
                                                                              @Nonnull JavaPlugin plugin, @Nonnull Location location) {
                for (ConversionUtil.AbstractConversionProcessor processor : processors) {
                    boolean inputValid = processor instanceof ConversionUtil.NoInputProcessor ||
                            processor instanceof ConversionUtil.SolarProcessor ||
                            processor.hasValidInput(menu, inputSlots);
                    if (!inputValid) {
                        plugin.getLogger().fine(String.format("Processor %s at %s failed input validation",
                                processor.getClass().getSimpleName(), location));
                        continue;
                    }
                    boolean canOutput = processor.canOutput(menu, outputSlots, plugin, location);
                    int energyRequired = processor.getCurrentEnergyChange(menu, inputSlots);
                    int currentCharge = getCharge(location);
                    plugin.getLogger().info(String.format("Selected processor %s at %s: inputValid=%b, canOutput=%b, energyRequired=%d, currentCharge=%d",
                            processor.getClass().getSimpleName(), location, inputValid, canOutput, energyRequired, currentCharge));
                    return processor;
                }
                plugin.getLogger().warning(String.format("No valid processor found at %s for input slots %s", location, Arrays.toString(inputSlots)));
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
        if (uiSize < 27 || uiSize % 9 != 0) {
            XingchenExpansion.instance.getLogger().log(Level.SEVERE, "Invalid UI size for {0}: {1}", new Object[]{getId(), uiSize});
        }
        for (int slot : inputSlots) {
            if (slot < 0 || slot >= uiSize) {
                XingchenExpansion.instance.getLogger().log(Level.SEVERE, "Invalid input slot for {0}: {1}", new Object[]{getId(), slot});
            }
        }
        for (int slot : outputSlots) {
            if (slot < 0 || slot >= uiSize) {
                XingchenExpansion.instance.getLogger().log(Level.SEVERE, "Invalid output slot for {0}: {1}", new Object[]{getId(), slot});
            }
        }
        if (progressSlot < 0 || progressSlot >= uiSize) {
            XingchenExpansion.instance.getLogger().log(Level.SEVERE, "Invalid progress slot for {0}: {1}", new Object[]{getId(), progressSlot});
        }
    }

    protected void setupMenuPreset() {
        try {
            XingchenExpansion.instance.getLogger().info("Setting up BlockMenuPreset for " + getId());
            new BlockMenuPreset(getId(), getItemName()) {
                @Override
                public void init() {
                    constructMenu(this);
                }

                @Override
                public boolean canOpen(@Nonnull Block b, @Nonnull Player p) {
                    return p.hasPermission("slimefun.inventory.bypass") || Slimefun.getPermissionsService().hasPermission(p, AbstractMachine.this);
                }

                @Override
                public int[] getSlotsAccessedByItemTransport(@Nonnull ItemTransportFlow flow) {
                    return flow == ItemTransportFlow.INSERT ? inputSlots : outputSlots;
                }

                @Override
                public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block b) {
                    for (int slot : outputSlots) {
                        menu.addMenuClickHandler(slot, (player, s, item, action) -> {
                            XingchenExpansion.instance.getServer().getScheduler().runTaskLater(XingchenExpansion.instance, () -> {
                                ConversionUtil.AbstractConversionProcessor processor = taskManager.selectProcessor(menu, inputSlots, XingchenExpansion.instance, b.getLocation());
                                if (processor != null && processor.getTaskState(b.getLocation()) == ConversionUtil.TaskState.PAUSED) {
                                    if (processor.canOutput(menu, outputSlots, XingchenExpansion.instance, b.getLocation())) {
                                        processor.resume(menu, b.getLocation(), XingchenExpansion.instance, inputSlots, outputSlots);
                                        XingchenExpansion.instance.getLogger().fine(String.format("Resumed task at %s due to output slot change", b.getLocation()));
                                    }
                                }
                                updateStatus(menu, b.getLocation());
                            }, 1L);
                            return true;
                        });
                    }
                    onNewInstance(b, AbstractMachine.this);
                }
            };
        } catch (Exception e) {
            XingchenExpansion.instance.getLogger().log(Level.SEVERE, "Failed to initialize BlockMenuPreset for {0}: {1}", new Object[]{getId(), e.getMessage()});
        }
    }

    protected void constructMenu(@Nonnull BlockMenuPreset preset) {
        for (int slot : borderSlots) {
            preset.addItem(slot, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " "), (p, s, item, action) -> false);
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

            // 优先检查能量
            if (energyRequired > 0 && currentCharge < energyRequired) {
                state = ConversionUtil.MachineState.NO_ENERGY;
            }
            // 再检查输出槽
            else if (!canOutput) {
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
                    XingchenExpansion.instance.getLogger().fine(String.format("Ticking %s at %s", getId(), b.getLocation()));
                    process(b.getLocation(), menu, XingchenExpansion.instance);
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
        ConversionUtil.AbstractConversionProcessor processor = taskManager.selectProcessor(menu, inputSlots, plugin, location);
        if (processor == null) {
            taskManager.updateProgressItem(menu, location, plugin, inputSlots, outputSlots, ConversionUtil.MachineState.NO_INPUT);
            return;
        }

        ConversionUtil.TaskState taskState = processor.getTaskState(location);
        int energyRequired = processor.getCurrentEnergyChange(menu, inputSlots);

        // 检查能量
        if (energyRequired > 0 && currentCharge < energyRequired) {
            if (taskState == ConversionUtil.TaskState.RUNNING) {
                processor.pause(menu, location, plugin, inputSlots, outputSlots);
            }
            taskManager.updateProgressItem(menu, location, plugin, inputSlots, outputSlots, ConversionUtil.MachineState.NO_ENERGY);
            return;
        }

        // 检查输出槽
        if (!processor.canOutput(menu, outputSlots, plugin, location)) {
            if (taskState == ConversionUtil.TaskState.RUNNING) {
                processor.pause(menu, location, plugin, inputSlots, outputSlots);
            }
            taskManager.updateProgressItem(menu, location, plugin, inputSlots, outputSlots, ConversionUtil.MachineState.OUTPUT_FULL);
            return;
        }

        // 扣除能量
        if (taskState == ConversionUtil.TaskState.RUNNING) {
            setCharge(location, currentCharge - energyRequired);
        }

        // 执行处理
        boolean completed = taskManager.process(menu, location, plugin, inputSlots, outputSlots);

        updateStatus(menu, location);
    }

    @Override
    public void setCharge(@Nonnull Location location, int charge) {
        BlockStorage.addBlockInfo(location, "energy-charge", String.valueOf(charge));
    }

    @Override
    public int getCharge(@Nonnull Location location) {
        String charge = BlockStorage.getLocationInfo(location, "energy-charge");
        try {
            return charge != null ? Integer.parseInt(charge) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    protected void onNewInstance(@Nonnull Block b, @Nonnull SlimefunItem item) {
        setCharge(b.getLocation(), energyBuffer);
    }

    @Override
    public @Nonnull EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        return energyBuffer;
    }

    protected abstract List<ConversionUtil.AbstractConversionProcessor> getProcessors();
}