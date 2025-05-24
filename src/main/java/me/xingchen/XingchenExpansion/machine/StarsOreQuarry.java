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

public class StarsOreQuarry {
    public static class StarsDiamondQuarryL1 extends AbstractMachine {
        private static final int UI_SIZE = 27;
        private static final int[] INPUT_SLOTS = {10, 11};
        private static final int[] OUTPUT_SLOTS = {15, 16};
        private static final int PROGRESS_SLOT = 13;
        private static final int ENERGY_BUFFER = 750;
        private final XingchenExpansion plugin;

        public StarsDiamondQuarryL1(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
                                    @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe,
                                    @Nonnull XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, UI_SIZE, INPUT_SLOTS, OUTPUT_SLOTS, PROGRESS_SLOT,
                    ENERGY_BUFFER, getProcessorList(plugin), getStateItemProvider(), getSpecialCaseHandler());
            this.plugin = plugin;
            XingchenExpansion.getInstance().getLogger().info(String.format("Initialized StarsQuarryL1 with energy consumption: -50 J/tick"));
        }

        private static List<ConversionUtil.AbstractConversionProcessor> getProcessorList(XingchenExpansion plugin) {
            return List.of(
                    new ConversionUtil.SingleInputProcessor(
                            "STARS_SOURCE_QUALITY",
                            new String[]{"DIAMOND"},
                            1,
                            new int[]{1},
                            80,
                            PROGRESS_SLOT,
                            250,
                            true,
                            getStateItemProvider(),
                            getSpecialCaseHandler()
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
    public static class StarsDiamondQuarryL2 extends AbstractMachine {
        private static final int UI_SIZE = 27;
        private static final int[] INPUT_SLOTS = {10, 11};
        private static final int[] OUTPUT_SLOTS = {15, 16};
        private static final int PROGRESS_SLOT = 13;
        private static final int ENERGY_BUFFER = 1200;
        private final XingchenExpansion plugin;

        public StarsDiamondQuarryL2(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
                               @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe,
                               @Nonnull XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, UI_SIZE, INPUT_SLOTS, OUTPUT_SLOTS, PROGRESS_SLOT,
                    ENERGY_BUFFER, getProcessorList(plugin), getStateItemProvider(), getSpecialCaseHandler());
            this.plugin = plugin;
            XingchenExpansion.getInstance().getLogger().info(String.format("Initialized StarsQuarryL1 with energy consumption: -50 J/tick"));
        }

        private static List<ConversionUtil.AbstractConversionProcessor> getProcessorList(XingchenExpansion plugin) {
            return List.of(
                    new ConversionUtil.SingleInputProcessor(
                            "STARS_SOURCE_QUALITY",
                            new String[]{"DIAMOND"},
                            1,
                            new int[]{2},
                            40,
                            PROGRESS_SLOT,
                            400,
                            true,
                            getStateItemProvider(),
                            getSpecialCaseHandler()
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
    public static class StarsDiamondQuarryUltimate extends AbstractMachine {
        private static final int UI_SIZE = 27;
        private static final int[] INPUT_SLOTS = {10, 11};
        private static final int[] OUTPUT_SLOTS = {15, 16};
        private static final int PROGRESS_SLOT = 13;
        private static final int ENERGY_BUFFER = 2310;
        private final XingchenExpansion plugin;

        public StarsDiamondQuarryUltimate(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
                                    @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe,
                                    @Nonnull XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, UI_SIZE, INPUT_SLOTS, OUTPUT_SLOTS, PROGRESS_SLOT,
                    ENERGY_BUFFER, getProcessorList(plugin), getStateItemProvider(), getSpecialCaseHandler());
            this.plugin = plugin;
            XingchenExpansion.getInstance().getLogger().info(String.format("Initialized StarsQuarryL1 with energy consumption: -50 J/tick"));
        }

        private static List<ConversionUtil.AbstractConversionProcessor> getProcessorList(XingchenExpansion plugin) {
            return List.of(
                    new ConversionUtil.SingleInputProcessor(
                            "STARS_SOURCE_QUALITY",
                            new String[]{"DIAMOND"},
                            1,
                            new int[]{5},
                            4,
                            PROGRESS_SLOT,
                            770,
                            true,
                            getStateItemProvider(),
                            getSpecialCaseHandler()
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
}

