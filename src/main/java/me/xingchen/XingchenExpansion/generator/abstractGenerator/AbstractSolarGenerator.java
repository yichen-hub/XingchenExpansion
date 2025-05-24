package me.xingchen.XingchenExpansion.generator.abstractGenerator;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.xingchen.XingchenExpansion.util.ConversionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class AbstractSolarGenerator extends AbstractGenerator {
    protected static final int PROGRESS_SLOT = 13;
    protected static final int[] INPUT_SLOTS = {};
    protected static final int[] OUTPUT_SLOTS = {};
    protected static final int[] BORDER_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};

    public AbstractSolarGenerator(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
                                  @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe,
                                  int uiSize, int energyCapacity,
                                  @Nonnull List<ConversionUtil.AbstractConversionProcessor> processors,
                                  @Nonnull ConversionUtil.StateItemProvider stateItemProvider,
                                  @Nonnull ConversionUtil.SpecialCaseHandler specialCaseHandler) {
        super(itemGroup, item, recipeType, recipe, uiSize, INPUT_SLOTS, OUTPUT_SLOTS, PROGRESS_SLOT,
                energyCapacity, processors, stateItemProvider, specialCaseHandler);
    }

    @Override
    protected void process(@Nonnull Location location, @Nonnull BlockMenu menu, @Nonnull JavaPlugin plugin) {
        plugin.getLogger().fine(String.format("Processing %s at %s: charge=%d", getId(), location, getCharge(location)));

        ConversionUtil.AbstractConversionProcessor processor = taskManager.selectProcessor(menu, inputSlots, plugin, location);
        ConversionUtil.TaskState taskState = processor != null ? processor.getTaskState(location) : ConversionUtil.TaskState.IDLE;

        if (processor == null) {
            taskManager.updateProgressItem(menu, location, plugin, inputSlots, outputSlots, ConversionUtil.MachineState.IDLE);
            plugin.getLogger().fine(String.format("No processor selected at %s", location));
            return;
        }

        int energyChange = getGeneratedOutput(location, BlockStorage.getLocationInfo(location));
        ConversionUtil.MachineState state = energyChange > 0 ? ConversionUtil.MachineState.CONVERTING : ConversionUtil.MachineState.IDLE;

        if (energyChange <= 0) {
            taskManager.updateProgressItem(menu, location, plugin, inputSlots, outputSlots, ConversionUtil.MachineState.IDLE);
            plugin.getLogger().fine(String.format("No energy generated at %s", location));
            return;
        }

        if (taskState == ConversionUtil.TaskState.RUNNING) {
            setCharge(location, getCharge(location) + energyChange);
            plugin.getLogger().fine(String.format("Generated %d energy at %s", energyChange, location));
        }

        boolean completed = taskManager.process(menu, location, plugin, inputSlots, outputSlots);
        if (completed) {
            plugin.getLogger().fine(String.format("Task completed at %s", location));
        }

        taskManager.updateProgressItem(menu, location, plugin, inputSlots, outputSlots, state);
    }

    @Override
    protected void constructMenu(@Nonnull BlockMenuPreset preset) {
        for (int slot : BORDER_SLOTS) {
            preset.addItem(slot, new CustomItemStack(Material.CYAN_STAINED_GLASS_PANE, " "), (p, s, item, action) -> false);
        }
    }

    public abstract int getGeneratedOutput(@Nonnull Location l, @Nonnull me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config data);

    protected abstract List<ConversionUtil.AbstractConversionProcessor> getProcessors();
}