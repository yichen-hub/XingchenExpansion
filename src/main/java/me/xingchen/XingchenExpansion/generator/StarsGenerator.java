package me.xingchen.XingchenExpansion.generator;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import me.xingchen.XingchenExpansion.generator.abstractGenerator.AbstractGenerator;
import me.xingchen.XingchenExpansion.util.ConversionUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StarsGenerator {

    public static class StarsGeneratorL1 extends AbstractGenerator {
        private static final int UI_SIZE = 27;
        private static final int[] INPUT_SLOTS = {10, 11};
        private static final int[] OUTPUT_SLOTS = {15, 16};
        private static final int PROGRESS_SLOT = 13;
        private static final int ENERGY_CAPACITY = 1000;
        private static final int PROCESSING_TIME = 20; // 每任务 20 ticks

        public StarsGeneratorL1(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
                                @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe,
                                @Nonnull XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, UI_SIZE, INPUT_SLOTS, OUTPUT_SLOTS, PROGRESS_SLOT,
                    ENERGY_CAPACITY, getProcessorList(), getStateItemProvider(), getSpecialCaseHandler());
        }

        @Override
        protected List<ConversionUtil.AbstractConversionProcessor> getProcessors() {
            return getProcessorList();
        }

        private static List<ConversionUtil.AbstractConversionProcessor> getProcessorList() {
            return List.of(
                    new ConversionUtil.SingleInputProcessor(
                            "STARS_ORE",
                            new String[]{"STARS_WASTE"},
                            1,
                            new int[]{1},
                            PROCESSING_TIME,
                            PROGRESS_SLOT,
                            40,
                            true,
                            getStateItemProvider(),
                            getSpecialCaseHandler()
                    ),
                    new ConversionUtil.SingleInputProcessor(
                            "STARS_INGOT",
                            new String[]{"STARS_WASTE"},
                            1,
                            new int[]{2},
                            PROCESSING_TIME,
                            PROGRESS_SLOT,
                            80,
                            true,
                            getStateItemProvider(),
                            getSpecialCaseHandler()
                    )
            );
        }

        private static ConversionUtil.StateItemProvider getStateItemProvider() {
            return new ConversionUtil.StateItemProvider() {
                @Override
                @Nonnull
                public Map<ConversionUtil.MachineState, ItemStack> getStateItems() {
                    Map<ConversionUtil.MachineState, ItemStack> items = new HashMap<>();
                    items.put(ConversionUtil.MachineState.IDLE,
                            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&7状态: 待机"));
                    items.put(ConversionUtil.MachineState.CONVERTING,
                            new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a状态: 运行中"));
                    items.put(ConversionUtil.MachineState.OUTPUT_FULL,
                            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c状态:待机"));
                    items.put(ConversionUtil.MachineState.NO_INPUT,
                            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c状态: 待机"));
                    items.put(ConversionUtil.MachineState.NO_ENERGY,
                            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c状态: 待机"));
                    return items;
                }
            };
        }

        private static ConversionUtil.SpecialCaseHandler getSpecialCaseHandler() {
            return (progressInfo) -> Optional.empty();
        }
    }
    public static class StarsGeneratorL2 extends AbstractGenerator {
        private static final int UI_SIZE = 27;
        private static final int[] INPUT_SLOTS = {10, 11};
        private static final int[] OUTPUT_SLOTS = {15, 16};
        private static final int PROGRESS_SLOT = 13;
        private static final int ENERGY_CAPACITY = 1000;
        private static final int PROCESSING_TIME = 20; // 每任务 20 ticks

        public StarsGeneratorL2(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
                                @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe,
                                @Nonnull XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, UI_SIZE, INPUT_SLOTS, OUTPUT_SLOTS, PROGRESS_SLOT,
                    ENERGY_CAPACITY, getProcessorList(), getStateItemProvider(), getSpecialCaseHandler());
        }

        @Override
        protected List<ConversionUtil.AbstractConversionProcessor> getProcessors() {
            return getProcessorList();
        }

        private static List<ConversionUtil.AbstractConversionProcessor> getProcessorList() {
            return List.of(
                    new ConversionUtil.SingleInputProcessor(
                            "STARS_ORE",
                            new String[]{"STARS_WASTE"},
                            1,
                            new int[]{1},
                            PROCESSING_TIME,
                            PROGRESS_SLOT,
                            710,
                            true,
                            getStateItemProvider(),
                            getSpecialCaseHandler()
                    ),
                    new ConversionUtil.SingleInputProcessor(
                            "STARS_INGOT",
                            new String[]{"STARS_WASTE"},
                            1,
                            new int[]{2},
                            PROCESSING_TIME,
                            PROGRESS_SLOT,
                            1000,
                            true,
                            getStateItemProvider(),
                            getSpecialCaseHandler()
                    )
            );
        }

        private static ConversionUtil.StateItemProvider getStateItemProvider() {
            return new ConversionUtil.StateItemProvider() {
                @Override
                @Nonnull
                public Map<ConversionUtil.MachineState, ItemStack> getStateItems() {
                    Map<ConversionUtil.MachineState, ItemStack> items = new HashMap<>();
                    items.put(ConversionUtil.MachineState.IDLE,
                            new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "&7状态: 待机"));
                    items.put(ConversionUtil.MachineState.CONVERTING,
                            new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a状态: 运行中"));
                    items.put(ConversionUtil.MachineState.OUTPUT_FULL,
                            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c状态: 待机"));
                    items.put(ConversionUtil.MachineState.NO_INPUT,
                            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c状态: 待机"));
                    items.put(ConversionUtil.MachineState.NO_ENERGY,
                            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c状态: 待机"));
                    return items;
                }
            };
        }

        private static ConversionUtil.SpecialCaseHandler getSpecialCaseHandler() {
            return (progressInfo) -> Optional.empty();
        }
    }
    public static class StarsGeneratorUltimate extends AbstractGenerator {
        private static final int UI_SIZE = 27;
        private static final int[] INPUT_SLOTS = {10, 11};
        private static final int[] OUTPUT_SLOTS = {15, 16};
        private static final int PROGRESS_SLOT = 13;
        private static final int ENERGY_CAPACITY = 1000;
        private static final int PROCESSING_TIME = 20; // 每任务 20 ticks

        public StarsGeneratorUltimate(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
                                @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe,
                                @Nonnull XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, UI_SIZE, INPUT_SLOTS, OUTPUT_SLOTS, PROGRESS_SLOT,
                    ENERGY_CAPACITY, getProcessorList(), getStateItemProvider(), getSpecialCaseHandler());
        }

        @Override
        protected List<ConversionUtil.AbstractConversionProcessor> getProcessors() {
            return getProcessorList();
        }

        private static List<ConversionUtil.AbstractConversionProcessor> getProcessorList() {
            return List.of(
                    new ConversionUtil.SingleInputProcessor(
                            "STARS_ORE",
                            new String[]{"STARS_WASTE"},
                            1,
                            new int[]{1},
                            PROCESSING_TIME,
                            PROGRESS_SLOT,
                            1024,
                            true,
                            getStateItemProvider(),
                            getSpecialCaseHandler()
                    ),
                    new ConversionUtil.SingleInputProcessor(
                            "STARS_INGOT",
                            new String[]{"STARS_WASTE"},
                            1,
                            new int[]{2},
                            PROCESSING_TIME,
                            PROGRESS_SLOT,
                            1440,
                            true,
                            getStateItemProvider(),
                            getSpecialCaseHandler()
                    )
            );
        }

        private static ConversionUtil.StateItemProvider getStateItemProvider() {
            return new ConversionUtil.StateItemProvider() {
                @Override
                @Nonnull
                public Map<ConversionUtil.MachineState, ItemStack> getStateItems() {
                    Map<ConversionUtil.MachineState, ItemStack> items = new HashMap<>();
                    items.put(ConversionUtil.MachineState.IDLE,
                            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&7状态: 待机"));
                    items.put(ConversionUtil.MachineState.CONVERTING,
                            new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a状态: 运行中"));
                    items.put(ConversionUtil.MachineState.OUTPUT_FULL,
                            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c状态:待机"));
                    items.put(ConversionUtil.MachineState.NO_INPUT,
                            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c状态: 待机"));
                    items.put(ConversionUtil.MachineState.NO_ENERGY,
                            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c状态: 待机"));
                    return items;
                }
            };
        }

        private static ConversionUtil.SpecialCaseHandler getSpecialCaseHandler() {
            return (progressInfo) -> Optional.empty();
        }
    }
}