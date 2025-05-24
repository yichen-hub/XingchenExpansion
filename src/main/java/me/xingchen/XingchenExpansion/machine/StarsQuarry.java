package me.xingchen.XingchenExpansion.machine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import me.xingchen.XingchenExpansion.machine.abstractMachine.AbstractMachine;
import me.xingchen.XingchenExpansion.util.ConversionUtil;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;

public class StarsQuarry {
    public static class StarsQuarryL1 extends AbstractMachine {
        private static final int UI_SIZE = 27;
        private static final int[] INPUT_SLOTS = {};
        private static final int[] OUTPUT_SLOTS = {15, 16};
        private static final int PROGRESS_SLOT = 13;
        private static final int ENERGY_BUFFER = 840;
        private final XingchenExpansion plugin;

        public StarsQuarryL1(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
                             @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe,
                             @Nonnull XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, UI_SIZE, INPUT_SLOTS, OUTPUT_SLOTS, PROGRESS_SLOT,
                    ENERGY_BUFFER, getProcessorList(plugin), getStateItemProvider(), getSpecialCaseHandler());
            this.plugin = plugin;
            XingchenExpansion.getInstance().getLogger().info(String.format("Initialized StarsQuarryL1 with energy consumption: -50 J/tick"));
        }

        private static List<ConversionUtil.AbstractConversionProcessor> getProcessorList(XingchenExpansion plugin) {
            return List.of(
                    new ConversionUtil.NoInputProcessor(
                            new String[]{"STARS_ORE"},
                            new int[]{1},
                            36,
                            13,
                            280,
                            true,
                            getStateItemProvider(),
                            getSpecialCaseHandler(),
                            OUTPUT_SLOTS,
                            plugin
                    )
            );
        }

        @Override
        protected List<ConversionUtil.AbstractConversionProcessor> getProcessors() {
            return getProcessorList(plugin);
        }

        private static ConversionUtil.StateItemProvider getStateItemProvider() {
            return new ConversionUtil.DefaultStateItemProvider();
        }

        private static ConversionUtil.SpecialCaseHandler getSpecialCaseHandler() {
            return progressInfo -> Optional.empty();
        }
    }
    public static class StarsQuarryL2 extends AbstractMachine {
        private static final int UI_SIZE = 27;
        private static final int[] INPUT_SLOTS = {};
        private static final int[] OUTPUT_SLOTS = {15, 16};
        private static final int PROGRESS_SLOT = 13;
        private static final int ENERGY_BUFFER = 1590;
        private final XingchenExpansion plugin; // 存储 plugin

        public StarsQuarryL2(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
                             @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe,
                             @Nonnull XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, UI_SIZE, INPUT_SLOTS, OUTPUT_SLOTS, PROGRESS_SLOT,
                    ENERGY_BUFFER, getProcessorList(plugin), getStateItemProvider(), getSpecialCaseHandler());
            this.plugin = plugin; // 初始化 plugin
            XingchenExpansion.getInstance().getLogger().info(String.format("Initialized StarsQuarryL1 with energy consumption: -50 J/tick"));
        }

        private static List<ConversionUtil.AbstractConversionProcessor> getProcessorList(XingchenExpansion plugin) {
            return List.of(
                    new ConversionUtil.NoInputProcessor(
                            new String[]{"STARS_ORE"},
                            new int[]{2},
                            28,
                            13,
                            530,
                            true,
                            getStateItemProvider(),
                            getSpecialCaseHandler(),
                            OUTPUT_SLOTS,
                            plugin
                    )
            );
        }

        @Override
        protected List<ConversionUtil.AbstractConversionProcessor> getProcessors() {
            return getProcessorList(plugin); // 使用实例的 plugin
        }

        private static ConversionUtil.StateItemProvider getStateItemProvider() {
            return new ConversionUtil.DefaultStateItemProvider();
        }

        private static ConversionUtil.SpecialCaseHandler getSpecialCaseHandler() {
            return progressInfo -> Optional.empty();
        }
    }
    public static class StarsQuarryUltimate extends AbstractMachine {
        private static final int UI_SIZE = 27;
        private static final int[] INPUT_SLOTS = {};
        private static final int[] OUTPUT_SLOTS = {15, 16};
        private static final int PROGRESS_SLOT = 13;
        private static final int ENERGY_BUFFER = 3072;
        private final XingchenExpansion plugin; // 存储 plugin

        public StarsQuarryUltimate(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
                             @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe,
                             @Nonnull XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, UI_SIZE, INPUT_SLOTS, OUTPUT_SLOTS, PROGRESS_SLOT,
                    ENERGY_BUFFER, getProcessorList(plugin), getStateItemProvider(), getSpecialCaseHandler());
            this.plugin = plugin; // 初始化 plugin
            XingchenExpansion.getInstance().getLogger().info(String.format("Initialized StarsQuarryL1 with energy consumption: -50 J/tick"));
        }

        private static List<ConversionUtil.AbstractConversionProcessor> getProcessorList(XingchenExpansion plugin) {
            return List.of(
                    new ConversionUtil.NoInputProcessor(
                            new String[]{"STARS_ORE"},
                            new int[]{5},
                            14,
                            13,
                            1024,
                            true,
                            getStateItemProvider(),
                            getSpecialCaseHandler(),
                            OUTPUT_SLOTS,
                            plugin
                    )
            );
        }

        @Override
        protected List<ConversionUtil.AbstractConversionProcessor> getProcessors() {
            return getProcessorList(plugin); // 使用实例的 plugin
        }

        private static ConversionUtil.StateItemProvider getStateItemProvider() {
            return new ConversionUtil.DefaultStateItemProvider();
        }

        private static ConversionUtil.SpecialCaseHandler getSpecialCaseHandler() {
            return progressInfo -> Optional.empty();
        }
    }
}