package me.xingchen.XingchenExpansion.generator;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import me.xingchen.XingchenExpansion.generator.abstractGenerator.AbstractSolarGenerator;
import me.xingchen.XingchenExpansion.util.ConversionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 太阳能发电机实现类，包含不同等级（L1、L2）。
 * 无输入输出，白天 20J/tick，夜晚 10J/tick，UI 显示白天/夜晚状态。
 */
public class StarsSolarGenerator {

    public static class StarsSolarGeneratorL1 extends AbstractSolarGenerator {
        private static final int UI_SIZE = 27;
        private static final int ENERGY_GENERATION_DAY = 25;
        private static final int ENERGY_GENERATION_NIGHT = 10;
        private static final int ENERGY_CAPACITY = 1000;

        public StarsSolarGeneratorL1(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
                                     @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe, @Nonnull XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, UI_SIZE, ENERGY_CAPACITY,
                    getProcessorList(), getStateItemProvider(), getSpecialCaseHandler());
        }

        private static List<ConversionUtil.AbstractConversionProcessor> getProcessorList() {
            return List.of(
                    new ConversionUtil.SolarProcessor(
                            PROGRESS_SLOT,
                            false,
                            ENERGY_GENERATION_DAY,
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
                    items.put(ConversionUtil.MachineState.IDLE, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "&7状态: 夜晚"));
                    items.put(ConversionUtil.MachineState.CONVERTING, new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a状态:发电中"));
                    return items;
                }
            };
        }

        private static ConversionUtil.SpecialCaseHandler getSpecialCaseHandler() {
            return (progressInfo) -> Optional.empty();
        }

        @Override
        protected List<ConversionUtil.AbstractConversionProcessor> getProcessors() {
            return getProcessorList();
        }

        @Override
        public int getGeneratedOutput(@Nonnull Location l, @Nonnull me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config data) {
            World world = l.getWorld();
            long time = world != null ? world.getTime() : 0;
            boolean isDaytime = time >= 0 && time < 12300;
            boolean isValid = world != null && world.getHighestBlockYAt(l) <= l.getBlockY();
            int output = (isDaytime && isValid) ? ENERGY_GENERATION_DAY : (isValid ? ENERGY_GENERATION_NIGHT : 0);
            XingchenExpansion.instance.getLogger().fine(String.format("Generating %d energy at %s (daytime=%b, valid=%b)",
                    output, l, isDaytime, isValid));
            return output;
        }
    }

    public static class StarsSolarGeneratorL2 extends AbstractSolarGenerator {
        private static final int UI_SIZE = 27;
        private static final int ENERGY_GENERATION_DAY = 55;
        private static final int ENERGY_GENERATION_NIGHT = 25;
        private static final int ENERGY_CAPACITY = 2000;

        public StarsSolarGeneratorL2(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
                                     @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe, @Nonnull XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, UI_SIZE, ENERGY_CAPACITY,
                    getProcessorList(), getStateItemProvider(), getSpecialCaseHandler());
        }

        private static List<ConversionUtil.AbstractConversionProcessor> getProcessorList() {
            return List.of(
                    new ConversionUtil.SolarProcessor(
                            PROGRESS_SLOT,
                            false,
                            ENERGY_GENERATION_DAY,
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
                    items.put(ConversionUtil.MachineState.IDLE, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "&7状态: 夜晚"));
                    items.put(ConversionUtil.MachineState.CONVERTING, new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a状态:发电中"));
                    return items;
                }
            };
        }

        private static ConversionUtil.SpecialCaseHandler getSpecialCaseHandler() {
            return (progressInfo) -> Optional.empty();
        }

        @Override
        protected List<ConversionUtil.AbstractConversionProcessor> getProcessors() {
            return getProcessorList();
        }

        @Override
        public int getGeneratedOutput(@Nonnull Location l, @Nonnull me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config data) {
            World world = l.getWorld();
            long time = world != null ? world.getTime() : 0;
            boolean isDaytime = time >= 0 && time < 12300;
            boolean isValid = world != null && world.getHighestBlockYAt(l) <= l.getBlockY();
            int output = (isDaytime && isValid) ? ENERGY_GENERATION_DAY : (isValid ? ENERGY_GENERATION_NIGHT : 0);
            XingchenExpansion.instance.getLogger().fine(String.format("Generating %d energy at %s (daytime=%b, valid=%b)",
                    output, l, isDaytime, isValid));
            return output;
        }
    }

    public static class StarsSolarGeneratorL3 extends AbstractSolarGenerator {
        private static final int UI_SIZE = 27;
        private static final int ENERGY_GENERATION_DAY = 120;
        private static final int ENERGY_GENERATION_NIGHT = 60;
        private static final int ENERGY_CAPACITY = 2000;

        public StarsSolarGeneratorL3(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
                                     @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe, @Nonnull XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, UI_SIZE, ENERGY_CAPACITY,
                    getProcessorList(), getStateItemProvider(), getSpecialCaseHandler());
        }

        private static List<ConversionUtil.AbstractConversionProcessor> getProcessorList() {
            return List.of(
                    new ConversionUtil.SolarProcessor(
                            PROGRESS_SLOT,
                            false,
                            ENERGY_GENERATION_DAY,
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
                    items.put(ConversionUtil.MachineState.IDLE, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "&7状态: 夜晚"));
                    items.put(ConversionUtil.MachineState.CONVERTING, new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a状态:发电中"));
                    return items;
                }
            };
        }

        private static ConversionUtil.SpecialCaseHandler getSpecialCaseHandler() {
            return (progressInfo) -> Optional.empty();
        }

        @Override
        protected List<ConversionUtil.AbstractConversionProcessor> getProcessors() {
            return getProcessorList();
        }

        @Override
        public int getGeneratedOutput(@Nonnull Location l, @Nonnull me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config data) {
            World world = l.getWorld();
            long time = world != null ? world.getTime() : 0;
            boolean isDaytime = time >= 0 && time < 12300;
            boolean isValid = world != null && world.getHighestBlockYAt(l) <= l.getBlockY();
            int output = (isDaytime && isValid) ? ENERGY_GENERATION_DAY : (isValid ? ENERGY_GENERATION_NIGHT : 0);
            XingchenExpansion.instance.getLogger().fine(String.format("Generating %d energy at %s (daytime=%b, valid=%b)",
                    output, l, isDaytime, isValid));
            return output;
        }
    }
    public static class StarsSolarGeneratorL4 extends AbstractSolarGenerator {
        private static final int UI_SIZE = 27;
        private static final int ENERGY_GENERATION_DAY = 245;
        private static final int ENERGY_GENERATION_NIGHT = 135;
        private static final int ENERGY_CAPACITY = 2000;

        public StarsSolarGeneratorL4(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
                                     @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe, @Nonnull XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, UI_SIZE, ENERGY_CAPACITY,
                    getProcessorList(), getStateItemProvider(), getSpecialCaseHandler());
        }

        private static List<ConversionUtil.AbstractConversionProcessor> getProcessorList() {
            return List.of(
                    new ConversionUtil.SolarProcessor(
                            PROGRESS_SLOT,
                            false,
                            ENERGY_GENERATION_DAY,
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
                    items.put(ConversionUtil.MachineState.IDLE, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "&7状态: 夜晚"));
                    items.put(ConversionUtil.MachineState.CONVERTING, new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a状态:发电中"));
                    return items;
                }
            };
        }

        private static ConversionUtil.SpecialCaseHandler getSpecialCaseHandler() {
            return (progressInfo) -> Optional.empty();
        }

        @Override
        protected List<ConversionUtil.AbstractConversionProcessor> getProcessors() {
            return getProcessorList();
        }

        @Override
        public int getGeneratedOutput(@Nonnull Location l, @Nonnull me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config data) {
            World world = l.getWorld();
            long time = world != null ? world.getTime() : 0;
            boolean isDaytime = time >= 0 && time < 12300;
            boolean isValid = world != null && world.getHighestBlockYAt(l) <= l.getBlockY();
            int output = (isDaytime && isValid) ? ENERGY_GENERATION_DAY : (isValid ? ENERGY_GENERATION_NIGHT : 0);
            XingchenExpansion.instance.getLogger().fine(String.format("Generating %d energy at %s (daytime=%b, valid=%b)",
                    output, l, isDaytime, isValid));
            return output;
        }
    }
    public static class StarsSolarGeneratorL5 extends AbstractSolarGenerator {
        private static final int UI_SIZE = 27;
        private static final int ENERGY_GENERATION_DAY = 520;
        private static final int ENERGY_GENERATION_NIGHT = 255;
        private static final int ENERGY_CAPACITY = 2000;

        public StarsSolarGeneratorL5(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
                                     @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe, @Nonnull XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, UI_SIZE, ENERGY_CAPACITY,
                    getProcessorList(), getStateItemProvider(), getSpecialCaseHandler());
        }

        private static List<ConversionUtil.AbstractConversionProcessor> getProcessorList() {
            return List.of(
                    new ConversionUtil.SolarProcessor(
                            PROGRESS_SLOT,
                            false,
                            ENERGY_GENERATION_DAY,
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
                    items.put(ConversionUtil.MachineState.IDLE, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "&7状态: 夜晚"));
                    items.put(ConversionUtil.MachineState.CONVERTING, new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a状态:发电中"));
                    return items;
                }
            };
        }

        private static ConversionUtil.SpecialCaseHandler getSpecialCaseHandler() {
            return (progressInfo) -> Optional.empty();
        }

        @Override
        protected List<ConversionUtil.AbstractConversionProcessor> getProcessors() {
            return getProcessorList();
        }

        @Override
        public int getGeneratedOutput(@Nonnull Location l, @Nonnull me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config data) {
            World world = l.getWorld();
            long time = world != null ? world.getTime() : 0;
            boolean isDaytime = time >= 0 && time < 12300;
            boolean isValid = world != null && world.getHighestBlockYAt(l) <= l.getBlockY();
            int output = (isDaytime && isValid) ? ENERGY_GENERATION_DAY : (isValid ? ENERGY_GENERATION_NIGHT : 0);
            XingchenExpansion.instance.getLogger().fine(String.format("Generating %d energy at %s (daytime=%b, valid=%b)",
                    output, l, isDaytime, isValid));
            return output;
        }
    }
    public static class StarsSolarGeneratorUltimate extends AbstractSolarGenerator {
        private static final int UI_SIZE = 27;
        private static final int ENERGY_GENERATION_DAY = 1024;
        private static final int ENERGY_GENERATION_NIGHT = 855;
        private static final int ENERGY_CAPACITY = 2000;

        public StarsSolarGeneratorUltimate(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item,
                                     @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe, @Nonnull XingchenExpansion plugin) {
            super(itemGroup, item, recipeType, recipe, UI_SIZE, ENERGY_CAPACITY,
                    getProcessorList(), getStateItemProvider(), getSpecialCaseHandler());
        }

        private static List<ConversionUtil.AbstractConversionProcessor> getProcessorList() {
            return List.of(
                    new ConversionUtil.SolarProcessor(
                            PROGRESS_SLOT,
                            false,
                            ENERGY_GENERATION_DAY,
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
                    items.put(ConversionUtil.MachineState.IDLE, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "&7状态: 夜晚"));
                    items.put(ConversionUtil.MachineState.CONVERTING, new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a状态:发电中"));
                    return items;
                }
            };
        }

        private static ConversionUtil.SpecialCaseHandler getSpecialCaseHandler() {
            return (progressInfo) -> Optional.empty();
        }

        @Override
        protected List<ConversionUtil.AbstractConversionProcessor> getProcessors() {
            return getProcessorList();
        }

        @Override
        public int getGeneratedOutput(@Nonnull Location l, @Nonnull me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config data) {
            World world = l.getWorld();
            long time = world != null ? world.getTime() : 0;
            boolean isDaytime = time >= 0 && time < 12300;
            boolean isValid = world != null && world.getHighestBlockYAt(l) <= l.getBlockY();
            int output = (isDaytime && isValid) ? ENERGY_GENERATION_DAY : (isValid ? ENERGY_GENERATION_NIGHT : 0);
            XingchenExpansion.instance.getLogger().fine(String.format("Generating %d energy at %s (daytime=%b, valid=%b)",
                    output, l, isDaytime, isValid));
            return output;
        }
    }
}