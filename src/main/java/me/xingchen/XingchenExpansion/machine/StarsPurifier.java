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

public class StarsPurifier {
    public static class StarsPurifierL1 extends AbstractMachine {
        private static final int UI_SIZE = 27;
        private static final int[] INPUT_SLOTS = {10, 11};
        private static final int[] OUTPUT_SLOTS = {15, 16};
        private static final int PROGRESS_SLOT = 13;
        private static final int ENERGY_BUFFER = 120;
        private final XingchenExpansion plugin;

        public StarsPurifierL1(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
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
                            "STARS_WASTE",
                            new String[]{"STARS_SOURCE_QUALITY"},
                            1,
                            new int[]{1},
                            80,
                            PROGRESS_SLOT,
                            40,
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
    public static class StarsPurifierL2 extends AbstractMachine {
        private static final int UI_SIZE = 27;
        private static final int[] INPUT_SLOTS = {10, 11};
        private static final int[] OUTPUT_SLOTS = {15, 16};
        private static final int PROGRESS_SLOT = 13;
        private static final int ENERGY_BUFFER = 240;
        private final XingchenExpansion plugin;

        public StarsPurifierL2(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
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
                            "STARS_WASTE",
                            new String[]{"STARS_SOURCE_QUALITY"},
                            1,
                            new int[]{1},
                            40,
                            PROGRESS_SLOT,
                            80,
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
    public static class StarsPurifierUltimate extends AbstractMachine {
        private static final int UI_SIZE = 27;
        private static final int[] INPUT_SLOTS = {10, 11};
        private static final int[] OUTPUT_SLOTS = {15, 16};
        private static final int PROGRESS_SLOT = 13;
        private static final int ENERGY_BUFFER = 1536;
        private final XingchenExpansion plugin;

        public StarsPurifierUltimate(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
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
                            "STARS_WASTE",
                            new String[]{"STARS_SOURCE_QUALITY"},
                            1,
                            new int[]{1},
                            10,
                            PROGRESS_SLOT,
                            512,
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