package me.xingchen.XingchenExpansion.util;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


/**
 * 物品转化工具类，用于管理 Slimefun 机器的转化流程。
 * 提供处理器、任务管理器和状态处理功能，支持单输入、多输入和无输入转化。
 */
public class ConversionUtil {

    // 任务状态枚举，表示机器的运行状态
    public enum TaskState {
        PAUSED("暂停"),
        IDLE("待机"),
        RUNNING("运行中");

        private final String displayName;

        TaskState(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 机器状态枚举，用于 UI 显示
    public enum MachineState {
        IDLE("待机"),
        CONVERTING("转化中"),
        OUTPUT_FULL("输出槽已满"),
        NO_INPUT("缺少输入"),
        NO_ENERGY("能量不足");

        private final String displayName;

        MachineState(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 输出模式枚举，定义转化输出方式
    public enum OutputMode {
        ALL, RANDOM, NONE
    }

    // Slimefun 物品匹配接口
    public interface SlimefunItemMatcher {
        boolean matches(@Nullable ItemStack item);

        @Nullable
        ItemStack getItem();
    }

    // 状态物品提供接口，为不同状态提供提示物品
    public interface StateItemProvider {
        @Nonnull
        Map<MachineState, ItemStack> getStateItems();
    }

    // 特殊情况处理接口，用于自定义转化逻辑
    public interface SpecialCaseHandler {
        @Nonnull
        Optional<ItemStack> handleSpecialCase(@Nonnull ProgressInfo progressInfo);
    }

    // 转化规则，定义输入输出物品及处理参数
    public static class ConversionRule {
        private final List<SlimefunItemMatcher> inputItems;
        private final List<SlimefunItemMatcher> outputItems;
        private final List<Integer> inputAmounts;
        private final List<Integer> outputAmounts;
        private final OutputMode outputMode;
        private final int processingTime;
        private final int progressSlot;
        private final boolean showProgress;
        private final List<Integer> energyChange;

        public ConversionRule(@Nonnull List<SlimefunItemMatcher> inputItems,
                              @Nonnull List<SlimefunItemMatcher> outputItems,
                              @Nonnull List<Integer> inputAmounts,
                              @Nonnull List<Integer> outputAmounts,
                              @Nonnull List<Integer> energyChange,
                              @Nonnull OutputMode outputMode,
                              int processingTime,
                              int progressSlot,
                              boolean showProgress) {
            this.inputItems = inputItems;
            this.outputItems = outputItems;
            this.inputAmounts = inputAmounts;
            this.outputAmounts = outputAmounts;
            this.outputMode = outputMode;
            this.processingTime = processingTime;
            this.progressSlot = progressSlot;
            this.showProgress = showProgress;
            this.energyChange = energyChange;
        }

        @Nonnull
        public List<SlimefunItemMatcher> getInputItems() {
            return inputItems;
        }

        @Nonnull
        public List<SlimefunItemMatcher> getOutputItems() {
            return outputItems;
        }

        @Nonnull
        public List<Integer> getInputAmounts() {
            return inputAmounts;
        }

        @Nonnull
        public List<Integer> getOutputAmounts() {
            return outputAmounts;
        }

        @Nonnull
        public List<Integer> getEnergyChange() {
            return energyChange;
        }

        @Nonnull
        public OutputMode getOutputMode() {
            return outputMode;
        }

        public int getProcessingTime() {
            return processingTime;
        }

        public int getProgressSlot() {
            return progressSlot;
        }

        public boolean shouldShowProgress() {
            return showProgress;
        }
    }

    // 进度信息，用于 UI 显示
    public static class ProgressInfo {
        private final MachineState state;
        private final int processingTime;
        private final int currentProcessingTime;
        private final boolean processing;

        public ProgressInfo(@Nonnull MachineState state, int processingTime, int currentProcessingTime, boolean processing) {
            this.state = state;
            this.processingTime = processingTime;
            this.currentProcessingTime = currentProcessingTime;
            this.processing = processing;
        }

        @Nonnull
        public MachineState getState() {
            return state;
        }

        public int getProcessingTime() {
            return processingTime;
        }

        public int getCurrentProcessingTime() {
            return currentProcessingTime;
        }

        public boolean isProcessing() {
            return processing;
        }

        public float getRemainingSeconds() {
            return (processingTime - currentProcessingTime) / 2.0f;
        }

        @Nonnull
        public String getProgressBar(int length) {
            int progress = processingTime > 0 ? (int) ((float) currentProcessingTime / processingTime * length) : 0;
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < length; i++) {
                bar.append(i < progress ? "█" : "▒");
            }
            return bar.toString();
        }
    }

    // 默认状态物品提供器，为 UI 提供默认提示物品
    public static class DefaultStateItemProvider implements StateItemProvider {
        @Override
        @Nonnull
        public Map<MachineState, ItemStack> getStateItems() {
            Map<MachineState, ItemStack> items = new HashMap<>();
            items.put(MachineState.IDLE, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "&7状态: 待机"));
            items.put(MachineState.CONVERTING, new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a状态: 转化中"));
            items.put(MachineState.OUTPUT_FULL, new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c状态: 待机"));
            items.put(MachineState.NO_INPUT, new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c状态: 待机"));
            items.put(MachineState.NO_ENERGY, new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c状态: 待机"));
            return items;
        }
    }

    // Slimefun 物品匹配器实现
    public static class SlimefunItemMatcherImpl implements SlimefunItemMatcher {
        private final String itemId;
        private final Material material;

        public SlimefunItemMatcherImpl(@Nonnull String itemId) {
            this.itemId = itemId;
            Material tempMaterial = null;
            try {
                tempMaterial = Material.valueOf(itemId);
            } catch (IllegalArgumentException e) {
            }
            this.material = tempMaterial; // 在 try-catch 外赋值
        }

        @Override
        public boolean matches(@Nullable ItemStack item) {
            if (item == null || item.getType() == Material.AIR) {
                return false;
            }
            // 优先检查 Slimefun 物品
            SlimefunItem sfItem = SlimefunItem.getByItem(item);
            boolean slimefunMatch = sfItem != null && sfItem.getId().equals(itemId);
            if (slimefunMatch) {
                return true;
            }
            // Fallback 到原版 Material
            boolean materialMatch = material != null && item.getType() == material;
            if (materialMatch) {
                XingchenExpansion.instance.getLogger().fine(String.format("Material matched: %s for %s", itemId, item.getType()));
            } else {
                XingchenExpansion.instance.getLogger().fine(String.format("No match for %s: item=%s, sfItem=%s", itemId, item.getType(), sfItem != null ? sfItem.getId() : "null"));
            }
            return materialMatch;
        }

        @Override
        @Nullable
        public ItemStack getItem() {
            // 优先返回 Slimefun 物品
            SlimefunItem sfItem = SlimefunItem.getById(itemId);
            if (sfItem != null) {
                XingchenExpansion.instance.getLogger().fine(String.format("Returning Slimefun item: %s", itemId));
                return sfItem.getItem().clone();
            }
            // Fallback 到原版物品
            if (material != null) {
                XingchenExpansion.instance.getLogger().fine(String.format("Returning Material item: %s", itemId));
                return new ItemStack(material);
            }
            XingchenExpansion.instance.getLogger().warning(String.format("No item found for %s", itemId));
            return null;
        }

        @Override
        public String toString() {
            return itemId;
        }
    }

    // 抽象转化处理器基类
    // 抽象转化处理器基类
    public abstract static class AbstractConversionProcessor {
        protected final ConversionRule rule;
        protected final StateItemProvider stateItemProvider;
        protected final SpecialCaseHandler specialCaseHandler;

        protected AbstractConversionProcessor(@Nonnull ConversionRule rule,
                                              @Nonnull StateItemProvider stateItemProvider,
                                              @Nonnull SpecialCaseHandler specialCaseHandler) {
            this.rule = rule;
            this.stateItemProvider = stateItemProvider;
            this.specialCaseHandler = specialCaseHandler;
        }

        public TaskState getTaskState(@Nonnull Location location) {
            String stateStr = BlockStorage.getLocationInfo(location, "task_state");
            try {
                return stateStr != null ? TaskState.valueOf(stateStr) : TaskState.IDLE;
            } catch (IllegalArgumentException e) {
                return TaskState.IDLE;
            }
        }

        protected void setTaskState(@Nonnull Location location, @Nonnull TaskState state) {
            BlockStorage.addBlockInfo(location, "task_state", state.name());
        }

        public boolean pause(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                             @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            if (getTaskState(location) != TaskState.RUNNING) return false;
            setTaskState(location, TaskState.PAUSED);
            updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
            return true;
        }

        public boolean resume(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                              @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            if (getTaskState(location) != TaskState.PAUSED) return false;
            setTaskState(location, TaskState.RUNNING);
            updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
            return true;
        }

        public boolean terminateWithReturn(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                                           @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            TaskState state = getTaskState(location);
            if (state == TaskState.IDLE) return false;
            returnInputItems(menu, location, plugin, inputSlots);
            BlockStorage.addBlockInfo(location, "processing_time", "0");
            setTaskState(location, TaskState.IDLE);
            updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
            return true;
        }

        public boolean terminateForce(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                                      @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            TaskState state = getTaskState(location);
            if (state == TaskState.IDLE) return false;
            BlockStorage.addBlockInfo(location, "processing_time", "0");
            setTaskState(location, TaskState.IDLE);
            updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
            return true;
        }

        protected abstract void returnInputItems(@Nonnull BlockMenu menu, @Nonnull Location location,
                                                 @Nonnull JavaPlugin plugin, @Nonnull int[] inputSlots);

        public boolean canOutput(@Nullable BlockMenu menu, @Nonnull int[] outputSlots, @Nonnull JavaPlugin plugin, @Nonnull Location location) {
            if (rule.getOutputMode() == OutputMode.NONE || rule.getOutputItems().isEmpty()) return true;
            List<ItemStack> outputs = rule.getOutputMode() == OutputMode.RANDOM ?
                    getRandomOutput(plugin, location) : getAllOutputs(plugin, location);
            if (outputs.isEmpty()) return true;

            if (outputSlots.length == 1 && outputs.size() == 1) {
                ItemStack output = outputs.get(0);
                ItemStack existing = menu != null ? menu.getItemInSlot(outputSlots[0]) : null;
                if (existing == null || existing.getType() == Material.AIR) {
                    return output.getAmount() <= output.getMaxStackSize();
                }
                if (existing.isSimilar(output)) {
                    return existing.getAmount() + output.getAmount() <= existing.getMaxStackSize();
                }
                return false;
            }

            int totalRequired = outputs.stream().mapToInt(ItemStack::getAmount).sum();
            int totalAvailable = 0;
            for (int slot : outputSlots) {
                ItemStack item = menu != null ? menu.getItemInSlot(slot) : null;
                if (item == null || item.getType() == Material.AIR) {
                    totalAvailable += outputs.stream().mapToInt(ItemStack::getMaxStackSize).max().orElse(64);
                } else {
                    for (ItemStack output : outputs) {
                        if (item.isSimilar(output)) {
                            totalAvailable += item.getMaxStackSize() - item.getAmount();
                            break;
                        }
                    }
                }
            }
            if (totalAvailable < totalRequired) return false;

            Map<Integer, ItemStack> slotStatus = new HashMap<>();
            for (int slot : outputSlots) {
                ItemStack item = menu != null ? menu.getItemInSlot(slot) : null;
                slotStatus.put(slot, item != null ? item.clone() : null);
            }
            List<ItemStack> toPlace = new ArrayList<>(outputs);
            for (int slot : outputSlots) {
                ItemStack existing = slotStatus.get(slot);
                Iterator<ItemStack> iterator = toPlace.iterator();
                while (iterator.hasNext()) {
                    ItemStack output = iterator.next();
                    if (existing == null || existing.getType() == Material.AIR) {
                        slotStatus.put(slot, output.clone());
                        iterator.remove();
                    } else if (existing.isSimilar(output)) {
                        int totalAmount = existing.getAmount() + output.getAmount();
                        if (totalAmount <= existing.getMaxStackSize()) {
                            ItemStack newItem = existing.clone();
                            newItem.setAmount(totalAmount);
                            slotStatus.put(slot, newItem);
                            iterator.remove();
                        } else {
                            ItemStack newItem = existing.clone();
                            newItem.setAmount(existing.getMaxStackSize());
                            slotStatus.put(slot, newItem);
                            output = output.clone();
                            output.setAmount(totalAmount - existing.getMaxStackSize());
                        }
                    }
                }
                if (toPlace.isEmpty()) break;
            }
            return toPlace.isEmpty();
        }

        @Nonnull
        public ProgressInfo getProgressInfo(@Nonnull Location location, @Nonnull JavaPlugin plugin) {
            String timeKey = "processing_time";
            String timeStr = BlockStorage.getLocationInfo(location, timeKey);
            int currentProcessingTime = parseProcessingTime(timeStr, plugin, location);
            TaskState state = getTaskState(location);
            MachineState machineState = state == TaskState.PAUSED ? MachineState.OUTPUT_FULL :
                    state == TaskState.RUNNING ? MachineState.CONVERTING : MachineState.IDLE;
            int totalProcessingTime = rule.getProcessingTime();
            return new ProgressInfo(machineState, totalProcessingTime, currentProcessingTime, state == TaskState.RUNNING);
        }

        protected void updateProgressItemIfEnabled(@Nonnull BlockMenu menu, @Nonnull Location location,
                                                   @Nonnull JavaPlugin plugin, @Nonnull int[] inputSlots,
                                                   @Nonnull int[] outputSlots) {
            if (!rule.shouldShowProgress()) return;
            ProgressInfo progress = getProgressInfo(location, plugin);
            ItemStack progressItem = stateItemProvider.getStateItems().get(progress.getState());
            if (progressItem == null) {
                progressItem = new DefaultStateItemProvider().getStateItems().get(progress.getState());
                plugin.getLogger().log(Level.WARNING, "Missing state item for {0} at {1}, using default", new Object[]{progress.getState(), location});
            }
            if (progressItem == null) {
                progressItem = new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c状态: " + progress.getState().getDisplayName());
            }

            ItemStack result = progressItem.clone();
            ItemMeta meta = result.getItemMeta();
            if (meta == null) {
                meta = plugin.getServer().getItemFactory().getItemMeta(result.getType());
                result.setItemMeta(meta);
            }
            List<String> lore = new ArrayList<>(meta.hasLore() ? meta.getLore() : Collections.emptyList());
            lore.removeIf(line -> line.startsWith("§7状态:") || line.startsWith("§7进度:") || line.startsWith("§7剩余:"));
            lore.add("§7状态: " + progress.getState().getDisplayName());
            if (progress.isProcessing() && progress.getState() == MachineState.CONVERTING) {
                lore.add("§7进度: " + progress.getProgressBar(10));
                lore.add("§7剩余: " + String.format("%.1fs", progress.getRemainingSeconds()));
            }
            meta.setLore(lore);
            result.setItemMeta(meta);

            menu.replaceExistingItem(rule.getProgressSlot(), result);
            menu.addMenuClickHandler(rule.getProgressSlot(), (p, slot, item, action) -> false);
            plugin.getLogger().info(String.format("Updated progress item at %s: state=%s, lore=%s", location, progress.getState(), lore));
        }

        @Nonnull
        protected List<ItemStack> getAllOutputs(@Nonnull JavaPlugin plugin, @Nonnull Location location) {
            List<ItemStack> outputs = new ArrayList<>();
            for (int i = 0; i < rule.getOutputItems().size(); i++) {
                SlimefunItemMatcher matcher = rule.getOutputItems().get(i);
                ItemStack item = matcher.getItem();
                if (item != null) {
                    ItemStack output = item.clone();
                    output.setAmount(rule.getOutputAmounts().get(i));
                    outputs.add(output);
                }
            }
            return outputs;
        }

        @Nonnull
        protected List<ItemStack> getRandomOutput(@Nonnull JavaPlugin plugin, @Nonnull Location location) {
            if (rule.getOutputItems().isEmpty()) return Collections.emptyList();
            Random random = new Random();
            int index = random.nextInt(rule.getOutputItems().size());
            SlimefunItemMatcher matcher = rule.getOutputItems().get(index);
            ItemStack item = matcher.getItem();
            if (item == null) return Collections.emptyList();
            ItemStack output = item.clone();
            output.setAmount(rule.getOutputAmounts().get(index));
            return List.of(output);
        }

        protected int parseProcessingTime(@Nullable String timeStr, @Nonnull JavaPlugin plugin, @Nonnull Location location) {
            if (timeStr == null) return 0;
            try {
                return Integer.parseInt(timeStr);
            } catch (NumberFormatException e) {
                plugin.getLogger().log(Level.WARNING, "Invalid processing time {0} at {1}", new Object[]{timeStr, location});
                return 0;
            }
        }

        public abstract boolean process(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                                        @Nonnull int[] inputSlots, @Nonnull int[] outputSlots);

        public abstract boolean hasValidInput(@Nonnull BlockMenu menu, @Nonnull int[] inputSlots);

        public abstract int getCurrentEnergyChange(@Nonnull BlockMenu menu, @Nonnull int[] inputSlots);
    }

    //无输入处理器
    public static class NoInputProcessor extends AbstractConversionProcessor {
        private final String[] outputItemIds;
        private final int[] outputAmounts;
        private final int[] outputSlots;
        private final JavaPlugin plugin;
        private final int energyGeneration;

        public NoInputProcessor(@Nonnull String[] outputItemIds, @Nonnull int[] outputAmounts, int processingTime,
                                int progressSlot, int energyGeneration, boolean showProgress,
                                @Nonnull StateItemProvider stateItemProvider,
                                @Nullable SpecialCaseHandler specialCaseHandler,
                                @Nonnull int[] outputSlots, @Nonnull JavaPlugin plugin) {
            super(createRule(null, null, outputItemIds, outputAmounts, new int[]{energyGeneration}, OutputMode.ALL,
                    processingTime, progressSlot, showProgress), stateItemProvider, specialCaseHandler);
            this.outputItemIds = outputItemIds;
            this.outputAmounts = outputAmounts;
            this.outputSlots = outputSlots;
            this.plugin = plugin;
            this.energyGeneration = energyGeneration;
        }

        @Override
        public boolean hasValidInput(@Nonnull BlockMenu menu, @Nonnull int[] inputSlots) {
            return true; // 无输入，始终有效
        }

        @Override
        public boolean canOutput(@Nonnull BlockMenu menu, @Nonnull int[] outputSlots, @Nonnull JavaPlugin plugin, @Nonnull Location location) {
            if (rule.getOutputMode() == OutputMode.NONE || rule.getOutputItems().isEmpty()) {
                plugin.getLogger().fine(String.format("NoInputProcessor: No output items at %s", location));
                return true;
            }
            List<ItemStack> outputs = rule.getOutputMode() == OutputMode.RANDOM ?
                    getRandomOutput(plugin, location) : getAllOutputs(plugin, location);
            if (outputs.isEmpty()) {
                plugin.getLogger().fine(String.format("NoInputProcessor: No outputs available at %s", location));
                return true;
            }

            for (ItemStack output : outputs) {
                boolean placed = false;
                for (int slot : outputSlots) {
                    ItemStack existing = menu.getItemInSlot(slot);
                    if (existing == null || existing.getType() == Material.AIR) {
                        plugin.getLogger().fine(String.format("NoInputProcessor: Empty output slot %d at %s", slot, location));
                        placed = true;
                        break;
                    } else if (existing.isSimilar(output) && existing.getAmount() + output.getAmount() <= existing.getMaxStackSize()) {
                        plugin.getLogger().fine(String.format("NoInputProcessor: Stackable output slot %d at %s", slot, location));
                        placed = true;
                        break;
                    }
                }
                if (!placed) {
                    plugin.getLogger().fine(String.format("NoInputProcessor: No available output slots for %s at %s", output.getType(), location));
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean process(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                               @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            TaskState state = getTaskState(location);
            plugin.getLogger().info(String.format("NoInputProcessor: TaskState at %s: %s", location, state));
            if (state == TaskState.PAUSED) {
                plugin.getLogger().fine(String.format("NoInputProcessor: Paused at %s", location));
                return false;
            }
            if (state == TaskState.IDLE) {
                setTaskState(location, TaskState.RUNNING);
                BlockStorage.addBlockInfo(location, "processing_time", "0");
                plugin.getLogger().info(String.format("NoInputProcessor: Started at %s, reset processing_time=0", location));
            }

            if (!canOutput(menu, outputSlots, plugin, location)) {
                setTaskState(location, TaskState.PAUSED);
                updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                plugin.getLogger().info(String.format("NoInputProcessor: Output slots full at %s", location));
                return false;
            }

            String timeKey = "processing_time";
            String timeStr = BlockStorage.getLocationInfo(location, timeKey);
            int currentProcessingTime = parseProcessingTime(timeStr, plugin, location);
            plugin.getLogger().info(String.format("NoInputProcessor: Processing at %s: blockStorage_processing_time=%s, currentProcessingTime=%d, totalProcessingTime=%d",
                    location, timeStr == null ? "null" : timeStr, currentProcessingTime, rule.getProcessingTime()));
            currentProcessingTime++;
            if (currentProcessingTime >= rule.getProcessingTime()) {
                processOutputs(menu, outputSlots, plugin, location);
                currentProcessingTime = 0;
                BlockStorage.addBlockInfo(location, timeKey, "0");
                setTaskState(location, TaskState.IDLE);
                updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                plugin.getLogger().info(String.format("NoInputProcessor: Conversion completed at %s", location));
                return true;
            } else {
                BlockStorage.addBlockInfo(location, timeKey, String.valueOf(currentProcessingTime));
                updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                plugin.getLogger().info(String.format("NoInputProcessor: Saved processing_time=%d at %s", currentProcessingTime, location));
                return false;
            }
        }

        @Override
        public int getCurrentEnergyChange(@Nonnull BlockMenu menu, @Nonnull int[] inputSlots) {
            if (energyGeneration <= 0) {
                plugin.getLogger().warning(String.format("NoInputProcessor: Invalid energyGeneration=%d at %s", energyGeneration, menu.getLocation()));
                return 0;
            }
            plugin.getLogger().fine(String.format("NoInputProcessor: Energy consumption at %s: %d J/tick", menu.getLocation(), energyGeneration));
            return energyGeneration;
        }

        @Override
        protected void returnInputItems(@Nonnull BlockMenu menu, @Nonnull Location location,
                                        @Nonnull JavaPlugin plugin, @Nonnull int[] inputSlots) {
            plugin.getLogger().fine(String.format("NoInputProcessor: No input items to return at %s", location));
        }

        private void processOutputs(@Nonnull BlockMenu menu, @Nonnull int[] outputSlots, @Nonnull JavaPlugin plugin,
                                    @Nonnull Location location) {
            List<ItemStack> outputs = rule.getOutputMode() == OutputMode.RANDOM ?
                    getRandomOutput(plugin, location) : getAllOutputs(plugin, location);
            for (ItemStack output : outputs) {
                for (int slot : outputSlots) {
                    ItemStack existing = menu.getItemInSlot(slot);
                    if (existing == null || existing.getType() == Material.AIR) {
                        menu.replaceExistingItem(slot, output.clone());
                        plugin.getLogger().fine(String.format("NoInputProcessor: Placed %s in slot %d at %s", output.getType(), slot, location));
                        break;
                    } else if (existing.isSimilar(output) && existing.getAmount() + output.getAmount() <= existing.getMaxStackSize()) {
                        existing.setAmount(existing.getAmount() + output.getAmount());
                        plugin.getLogger().fine(String.format("NoInputProcessor: Stacked %s in slot %d at %s", output.getType(), slot, location));
                        break;
                    }
                }
            }
        }
    }

    // 单输入处理器
    public static class SingleInputProcessor extends AbstractConversionProcessor {
        public SingleInputProcessor(@Nonnull String inputItemId, @Nullable String[] outputItemIds, int inputAmount,
                                    @Nonnull int[] outputAmounts, int processingTime, int progressSlot, int energyChange, boolean showProgress,
                                    @Nonnull StateItemProvider stateItemProvider, @Nonnull SpecialCaseHandler specialCaseHandler) {
            super(createRule(new String[]{inputItemId}, new int[]{inputAmount}, outputItemIds, outputAmounts,
                    new int[]{energyChange}, OutputMode.ALL, processingTime, progressSlot, showProgress), stateItemProvider, specialCaseHandler);
        }

        @Override
        public boolean process(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                               @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            TaskState state = getTaskState(location);
            if (state == TaskState.PAUSED) return false;
            if (state == TaskState.IDLE) setTaskState(location, TaskState.RUNNING);

            if (!hasValidInput(menu, inputSlots)) {
                setTaskState(location, TaskState.IDLE);
                updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                return false;
            }
            if (!canOutput(menu, outputSlots, plugin, location)) {
                setTaskState(location, TaskState.PAUSED);
                updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                return false;
            }

            String timeKey = "processing_time";
            int currentProcessingTime = parseProcessingTime(BlockStorage.getLocationInfo(location, timeKey), plugin, location);
            currentProcessingTime++;
            if (currentProcessingTime >= rule.getProcessingTime()) {
                processOutputs(menu, outputSlots, plugin, location);
                consumeInput(menu, inputSlots);
                currentProcessingTime = 0;
                BlockStorage.addBlockInfo(location, timeKey, "0");
                setTaskState(location, TaskState.IDLE);
                updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                return true;
            } else {
                BlockStorage.addBlockInfo(location, timeKey, String.valueOf(currentProcessingTime));
                updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                return false;
            }
        }
        @Override
        public int getCurrentEnergyChange(@Nonnull BlockMenu menu, @Nonnull int[] inputSlots) {
            if (!hasValidInput(menu, inputSlots)) {
                return 0;
            }
            return rule.getEnergyChange().isEmpty() ? 0 : rule.getEnergyChange().get(0);
        }

        @Override
        protected void returnInputItems(@Nonnull BlockMenu menu, @Nonnull Location location,
                                        @Nonnull JavaPlugin plugin, @Nonnull int[] inputSlots) {
            for (int slot : inputSlots) {
                ItemStack item = menu.getItemInSlot(slot);
                if (item != null && item.getType() != Material.AIR) {
                    boolean placed = false;
                    for (int inputSlot : inputSlots) {
                        ItemStack existing = menu.getItemInSlot(inputSlot);
                        if (existing == null || existing.getType() == Material.AIR) {
                            menu.replaceExistingItem(inputSlot, item.clone());
                            placed = true;
                            break;
                        } else if (existing.isSimilar(item) && existing.getAmount() + item.getAmount() <= existing.getMaxStackSize()) {
                            existing.setAmount(existing.getAmount() + item.getAmount());
                            placed = true;
                            break;
                        }
                    }
                    if (!placed) {
                        location.getWorld().dropItemNaturally(location, item.clone());
                        plugin.getLogger().log(Level.INFO, "输入槽已满，在 {0} 掉落物品: {1}", new Object[]{location, item.getType()});
                    }
                    menu.replaceExistingItem(slot, null);
                }
            }
        }

        public boolean hasValidInput(@Nonnull BlockMenu menu, @Nonnull int[] inputSlots) {
            for (int slot : inputSlots) {
                ItemStack item = menu.getItemInSlot(slot);
                if (item != null && item.getType() != Material.AIR) {
                    for (int i = 0; i < rule.getInputItems().size(); i++) {
                        if (rule.getInputItems().get(i).matches(item) && item.getAmount() >= rule.getInputAmounts().get(i)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private void consumeInput(@Nonnull BlockMenu menu, @Nonnull int[] inputSlots) {
            for (int slot : inputSlots) {
                ItemStack item = menu.getItemInSlot(slot);
                if (item != null && item.getType() != Material.AIR) {
                    for (int i = 0; i < rule.getInputItems().size(); i++) {
                        if (rule.getInputItems().get(i).matches(item) && item.getAmount() >= rule.getInputAmounts().get(i)) {
                            item.setAmount(item.getAmount() - rule.getInputAmounts().get(i));
                            if (item.getAmount() <= 0) {
                                menu.replaceExistingItem(slot, null);
                            }
                            return;
                        }
                    }
                }
            }
        }

        private void processOutputs(@Nonnull BlockMenu menu, @Nonnull int[] outputSlots, @Nonnull JavaPlugin plugin,
                                    @Nonnull Location location) {
            List<ItemStack> outputs = rule.getOutputMode() == OutputMode.RANDOM ?
                    getRandomOutput(plugin, location) : getAllOutputs(plugin, location);
            for (ItemStack output : outputs) {
                for (int slot : outputSlots) {
                    ItemStack existing = menu.getItemInSlot(slot);
                    if (existing == null || existing.getType() == Material.AIR) {
                        menu.replaceExistingItem(slot, output.clone());
                        break;
                    } else if (existing.isSimilar(output) && existing.getAmount() + output.getAmount() <= existing.getMaxStackSize()) {
                        existing.setAmount(existing.getAmount() + output.getAmount());
                        break;
                    }
                }
            }
        }

        public int getCurrentEnergyChange() {
            return rule.getEnergyChange().isEmpty() ? 0 : rule.getEnergyChange().get(0);
        }
    }

    // 多输入处理器
    public static class MultiInputProcessor extends AbstractConversionProcessor {
        public MultiInputProcessor(@Nonnull String[] inputItemIds, @Nonnull int[] inputAmounts,
                                   @Nullable String[] outputItemIds, @Nonnull int[] outputAmounts,
                                   int processingTime, int progressSlot, boolean showProgress, int energyChange,
                                   @Nonnull StateItemProvider stateItemProvider, @Nonnull SpecialCaseHandler specialCaseHandler) {
            super(createRule(inputItemIds, inputAmounts, outputItemIds, outputAmounts, new int[]{energyChange}, OutputMode.ALL,
                    processingTime, progressSlot, showProgress), stateItemProvider, specialCaseHandler);
        }

        @Override
        public boolean process(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                               @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            TaskState state = getTaskState(location);
            if (state == TaskState.PAUSED) return false;
            if (state == TaskState.IDLE) setTaskState(location, TaskState.RUNNING);

            if (!hasValidInput(menu, inputSlots)) {
                setTaskState(location, TaskState.IDLE);
                updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                return false;
            }
            if (!canOutput(menu, outputSlots, plugin, location)) {
                setTaskState(location, TaskState.PAUSED);
                updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                return false;
            }

            String timeKey = "processing_time";
            int currentProcessingTime = parseProcessingTime(BlockStorage.getLocationInfo(location, timeKey), plugin, location);
            currentProcessingTime++;
            if (currentProcessingTime >= rule.getProcessingTime()) {
                processOutputs(menu, outputSlots, plugin, location);
                consumeInput(menu, inputSlots);
                currentProcessingTime = 0;
                BlockStorage.addBlockInfo(location, timeKey, "0");
                setTaskState(location, TaskState.IDLE);
                updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                return true;
            } else {
                BlockStorage.addBlockInfo(location, timeKey, String.valueOf(currentProcessingTime));
                updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                return false;
            }
        }


        @Override
        protected void returnInputItems(@Nonnull BlockMenu menu, @Nonnull Location location,
                                        @Nonnull JavaPlugin plugin, @Nonnull int[] inputSlots) {
            for (int slot : inputSlots) {
                ItemStack item = menu.getItemInSlot(slot);
                if (item != null && item.getType() != Material.AIR) {
                    boolean placed = false;
                    for (int inputSlot : inputSlots) {
                        ItemStack existing = menu.getItemInSlot(inputSlot);
                        if (existing == null || existing.getType() == Material.AIR) {
                            menu.replaceExistingItem(inputSlot, item.clone());
                            placed = true;
                            break;
                        } else if (existing.isSimilar(item) && existing.getAmount() + item.getAmount() <= existing.getMaxStackSize()) {
                            existing.setAmount(existing.getAmount() + item.getAmount());
                            placed = true;
                            break;
                        }
                    }
                    if (!placed) {
                        location.getWorld().dropItemNaturally(location, item.clone());
                        plugin.getLogger().log(Level.INFO, "输入槽已满，在 {0} 掉落物品: {1}", new Object[]{location, item.getType()});
                    }
                    menu.replaceExistingItem(slot, null);
                }
            }
        }

        public boolean hasValidInput(@Nonnull BlockMenu menu, @Nonnull int[] inputSlots) {
            Map<String, Integer> foundItems = new HashMap<>();
            for (int slot : inputSlots) {
                ItemStack item = menu.getItemInSlot(slot);
                if (item != null && item.getType() != Material.AIR) {
                    for (int i = 0; i < rule.getInputItems().size(); i++) {
                        if (rule.getInputItems().get(i).matches(item)) {
                            foundItems.merge(rule.getInputItems().get(i).toString(), item.getAmount(), Integer::sum);
                        }
                    }
                }
            }
            for (int i = 0; i < rule.getInputItems().size(); i++) {
                String itemId = rule.getInputItems().get(i).toString();
                int requiredAmount = rule.getInputAmounts().get(i);
                if (foundItems.getOrDefault(itemId, 0) < requiredAmount) return false;
            }
            return true;
        }

        private void consumeInput(@Nonnull BlockMenu menu, @Nonnull int[] inputSlots) {
            Map<String, Integer> toConsume = new HashMap<>();
            for (int i = 0; i < rule.getInputItems().size(); i++) {
                toConsume.put(rule.getInputItems().get(i).toString(), rule.getInputAmounts().get(i));
            }
            for (int slot : inputSlots) {
                ItemStack item = menu.getItemInSlot(slot);
                if (item != null && item.getType() != Material.AIR) {
                    for (SlimefunItemMatcher matcher : rule.getInputItems()) {
                        String itemId = matcher.toString();
                        if (matcher.matches(item) && toConsume.getOrDefault(itemId, 0) > 0) {
                            int consumeAmount = Math.min(item.getAmount(), toConsume.get(itemId));
                            item.setAmount(item.getAmount() - consumeAmount);
                            toConsume.put(itemId, toConsume.get(itemId) - consumeAmount);
                            if (item.getAmount() <= 0) {
                                menu.replaceExistingItem(slot, null);
                            }
                            if (toConsume.get(itemId) <= 0) {
                                toConsume.remove(itemId);
                            }
                            break;
                        }
                    }
                }
                if (toConsume.isEmpty()) break;
            }
        }

        private void processOutputs(@Nonnull BlockMenu menu, @Nonnull int[] outputSlots, @Nonnull JavaPlugin plugin,
                                    @Nonnull Location location) {
            List<ItemStack> outputs = rule.getOutputMode() == OutputMode.RANDOM ?
                    getRandomOutput(plugin, location) : getAllOutputs(plugin, location);
            for (ItemStack output : outputs) {
                for (int slot : outputSlots) {
                    ItemStack existing = menu.getItemInSlot(slot);
                    if (existing == null || existing.getType() == Material.AIR) {
                        menu.replaceExistingItem(slot, output.clone());
                        break;
                    } else if (existing.isSimilar(output) && existing.getAmount() + output.getAmount() <= existing.getMaxStackSize()) {
                        existing.setAmount(existing.getAmount() + output.getAmount());
                        break;
                    }
                }
            }
        }
        @Override
        public int getCurrentEnergyChange(@Nonnull BlockMenu menu, @Nonnull int[] inputSlots) {
            if (!hasValidInput(menu, inputSlots)) {
                return 0;
            }
            return rule.getEnergyChange().isEmpty() ? 0 : rule.getEnergyChange().get(0);
        }
    }

    // 太阳能处理器
    public static class SolarProcessor extends AbstractConversionProcessor {
        public SolarProcessor(int progressSlot, boolean showProgress, int energyChange,
                              @Nonnull StateItemProvider stateItemProvider,
                              @Nonnull SpecialCaseHandler specialCaseHandler) {
            super(createRule(null, null, null, null, new int[]{energyChange}, OutputMode.NONE, 1, progressSlot, showProgress),
                    stateItemProvider, specialCaseHandler);
        }

        private boolean isDaytime(@Nonnull World world) {
            long time = world.getTime();
            return time >= 0 && time < 12000;
        }

        @Override
        public boolean hasValidInput(@Nonnull BlockMenu menu, @Nonnull int[] inputSlots) {
            return true; // 太阳能处理器无需输入
        }

        @Override
        public int getCurrentEnergyChange(@Nonnull BlockMenu menu, @Nonnull int[] inputSlots) {
            World world = menu.getLocation().getWorld();
            if (world == null || !isDaytime(world)) {
                return 0;
            }
            return rule.getEnergyChange().isEmpty() ? 0 : rule.getEnergyChange().get(0);
        }

        @Override
        public boolean process(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                               @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            TaskState state = getTaskState(location);
            if (state == TaskState.PAUSED) {
                updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                return false;
            }
            if (state == TaskState.IDLE) {
                setTaskState(location, TaskState.RUNNING);
            }

            updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
            return true;
        }

        @Override
        protected void returnInputItems(@Nonnull BlockMenu menu, @Nonnull Location location,
                                        @Nonnull JavaPlugin plugin, @Nonnull int[] inputSlots) {
            // 无输入，无需返还
        }

        @Override
        public ProgressInfo getProgressInfo(@Nonnull Location location, @Nonnull JavaPlugin plugin) {
            World world = location.getWorld();
            long time = world != null ? world.getTime() : 0;
            MachineState machineState = (time >= 0 && time < 12000) ? MachineState.CONVERTING : MachineState.IDLE;
            return new ProgressInfo(machineState, 1, 0, getTaskState(location) == TaskState.RUNNING);
        }
    }


    // 任务管理器，协调处理器运行
    public static abstract class TaskManager {
        protected final List<AbstractConversionProcessor> processors;
        protected final StateItemProvider stateItemProvider;
        protected final SpecialCaseHandler specialCaseHandler;
        protected final int progressSlot;

        public TaskManager(@Nonnull List<AbstractConversionProcessor> processors,
                           @Nonnull StateItemProvider stateItemProvider,
                           @Nonnull SpecialCaseHandler specialCaseHandler) {
            this.processors = processors;
            this.stateItemProvider = stateItemProvider;
            this.specialCaseHandler = specialCaseHandler;
            this.progressSlot = processors.isEmpty() ? 13 : processors.get(0).rule.getProgressSlot();
        }

        public StateItemProvider getStateItemProvider() {
            return stateItemProvider;
        }


        public boolean process(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                               @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            validateState(location, plugin);
            AbstractConversionProcessor cachedProcessor = getCachedProcessor(location);
            if (cachedProcessor != null && cachedProcessor.getTaskState(location) == TaskState.RUNNING) {
                boolean result = cachedProcessor.process(menu, location, plugin, inputSlots, outputSlots);
                persistData(location, cachedProcessor, plugin);
                return result;
            }
            AbstractConversionProcessor processor = selectProcessor(menu, inputSlots, plugin, location);
            cacheProcessor(location, processor);
            if (processor == null) {
                updateProgressItem(menu, location, plugin, inputSlots, outputSlots, MachineState.NO_INPUT);
                persistData(location, null, plugin);
                return false;
            }
            boolean result = processor.process(menu, location, plugin, inputSlots, outputSlots);
            persistData(location, processor, plugin);
            return result;
        }

        public boolean pause(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                             @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            AbstractConversionProcessor processor = selectProcessor(menu, inputSlots, plugin, location);
            if (processor == null) return false;
            boolean result = processor.pause(menu, location, plugin, inputSlots, outputSlots);
            persistData(location, processor, plugin);
            return result;
        }

        public boolean resume(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                              @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            AbstractConversionProcessor processor = selectProcessor(menu, inputSlots, plugin, location);
            if (processor == null) return false;
            boolean result = processor.resume(menu, location, plugin, inputSlots, outputSlots);
            persistData(location, processor, plugin);
            return result;
        }

        public boolean terminateWithReturn(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                                           @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            AbstractConversionProcessor processor = selectProcessor(menu, inputSlots, plugin, location);
            if (processor == null) return false;
            boolean result = processor.terminateWithReturn(menu, location, plugin, inputSlots, outputSlots);
            persistData(location, processor, plugin);
            return result;
        }

        public boolean terminateForce(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                                      @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            AbstractConversionProcessor processor = selectProcessor(menu, inputSlots, plugin, location);
            if (processor == null) return false;
            boolean result = processor.terminateForce(menu, location, plugin, inputSlots, outputSlots);
            persistData(location, processor, plugin);
            return result;
        }

        public void persistData(@Nonnull Location location, @Nullable AbstractConversionProcessor processor, @Nonnull JavaPlugin plugin) {
            if (processor != null) {
                String timeStr = BlockStorage.getLocationInfo(location, "processing_time");
                int currentProcessingTime = processor.parseProcessingTime(timeStr, plugin, location);
                BlockStorage.addBlockInfo(location, "processing_time", String.valueOf(currentProcessingTime));
                BlockStorage.addBlockInfo(location, "task_state", processor.getTaskState(location).name());
            } else {
                BlockStorage.addBlockInfo(location, "task_state", TaskState.IDLE.name());
            }
        }

        private void validateState(@Nonnull Location location, @Nonnull JavaPlugin plugin) {
            String timeStr = BlockStorage.getLocationInfo(location, "processing_time");
            if (timeStr != null) {
                try {
                    int time = Integer.parseInt(timeStr);
                    if (time < 0) {
                        plugin.getLogger().log(Level.WARNING, "无效的处理时间在 {0}，重置", location);
                        BlockStorage.addBlockInfo(location, "processing_time", "0");
                        BlockStorage.addBlockInfo(location, "task_state", TaskState.IDLE.name());
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().log(Level.WARNING, "无效的处理时间格式在 {0}，重置", location);
                    BlockStorage.addBlockInfo(location, "processing_time", "0");
                    BlockStorage.addBlockInfo(location, "task_state", TaskState.IDLE.name());
                }
            }
        }

        private void cacheProcessor(@Nonnull Location location, @Nullable AbstractConversionProcessor processor) {
            int index = processor != null ? processors.indexOf(processor) : -1;
            BlockStorage.addBlockInfo(location, "last_processor", String.valueOf(index));
        }

        private AbstractConversionProcessor getCachedProcessor(@Nonnull Location location) {
            String indexStr = BlockStorage.getLocationInfo(location, "last_processor");
            if (indexStr != null) {
                try {
                    int index = Integer.parseInt(indexStr);
                    if (index >= 0 && index < processors.size()) return processors.get(index);
                } catch (NumberFormatException ignored) {
                }
            }
            return null;
        }

        public void updateProgressItem(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                                       @Nonnull int[] inputSlots, @Nonnull int[] outputSlots, @Nullable MachineState state) {
            AbstractConversionProcessor processor = selectProcessor(menu, inputSlots, plugin, location);
            if (processor == null || !processor.rule.shouldShowProgress()) {
                if (state != null) {
                    ItemStack fallbackItem = stateItemProvider.getStateItems().get(state);
                    if (fallbackItem != null) {
                        menu.replaceExistingItem(this.progressSlot, fallbackItem.clone());
                        menu.addMenuClickHandler(this.progressSlot, (p, slot, item, action) -> false);
                    }
                }
                return;
            }
            // 获取 ProgressInfo
            ProgressInfo progress = processor.getProgressInfo(location, plugin);
            // 直接从 ProgressInfo 和 processor 获取值
            int currentProcessingTime = progress.getCurrentProcessingTime(); // 复用 progress 的值
            int processingTime = processor.rule.getProcessingTime();
            // 记录 BlockStorage 原始值以调试
            String timeKey = "processing_time";
            String timeStr = BlockStorage.getLocationInfo(location, timeKey);
            plugin.getLogger().info(String.format("Raw data at %s: blockStorage_processing_time=%s, processor=%s, rule.processingTime=%d",
                    location, timeStr == null ? "null" : timeStr, processor.getClass().getSimpleName(), processingTime));

            ItemStack progressItem = stateItemProvider.getStateItems().get(progress.getState());
            if (progressItem == null) {
                progressItem = new DefaultStateItemProvider().getStateItems().get(progress.getState());
                plugin.getLogger().log(Level.WARNING, "缺失状态 {0} 的提示物品，在 {1} 使用默认物品", new Object[]{progress.getState(), location});
            }
            if (progressItem == null) {
                progressItem = new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c状态: " + progress.getState().getDisplayName());
            }

            ItemStack result = progressItem.clone();
            ItemMeta meta = result.getItemMeta();
            if (meta == null) {
                meta = plugin.getServer().getItemFactory().getItemMeta(result.getType());
                result.setItemMeta(meta);
            }
            List<String> lore = new ArrayList<>(meta.hasLore() ? meta.getLore() : Collections.emptyList());
            lore.removeIf(line -> line.startsWith("§7状态:") || line.startsWith("§7进度:") || line.startsWith("§7剩余:"));
            lore.add("§7状态: " + progress.getState().getDisplayName());
            if ( progress.getState() == MachineState.CONVERTING) {
                XingchenExpansion.instance.getLogger().warning("ProcessingTime:" + processingTime + ", CurrentProcessingTime:" + currentProcessingTime +
                        ", Remaining:" + String.format("%.1fs", progress.getRemainingSeconds()));
                XingchenExpansion.instance.getLogger().warning("ProcessingTime"+processingTime+"currentProcessingTime:"+currentProcessingTime);
                lore.add("§7进度: " + progress.getProgressBar(10));
                lore.add("§7剩余: " + String.format("%.1fs", progress.getRemainingSeconds()));
            }

            meta.setLore(lore);
            result.setItemMeta(meta);

            menu.replaceExistingItem(processor.rule.getProgressSlot(), result);
            menu.addMenuClickHandler(processor.rule.getProgressSlot(), (p, slot, item, action) -> false);
        }

        public abstract AbstractConversionProcessor selectProcessor(@Nonnull BlockMenu menu, @Nonnull int[] inputSlots,
                                                                    @Nonnull JavaPlugin plugin, @Nonnull Location location);
    }

    static ConversionRule createRule(@Nullable String[] inputItemIds, @Nullable int[] inputAmounts,
                                     @Nullable String[] outputItemIds, @Nullable int[] outputAmounts,
                                     @Nullable int[] energyChange,
                                     @Nonnull OutputMode outputMode, int processingTime, int progressSlot,
                                     boolean showProgress) {
        List<SlimefunItemMatcher> inputItems = new ArrayList<>();
        List<Integer> inputAmountsList = new ArrayList<>();
        List<SlimefunItemMatcher> outputItems = new ArrayList<>();
        List<Integer> outputAmountsList = new ArrayList<>();
        List<Integer> energyChangeList = new ArrayList<>();

        if (inputItemIds != null && inputAmounts != null && energyChange != null) {
            for (int i = 0; i < inputItemIds.length; i++) {
                inputItems.add(new SlimefunItemMatcherImpl(inputItemIds[i]));
                inputAmountsList.add(inputAmounts[i]);
                energyChangeList.add(i < energyChange.length ? energyChange[i] : 0);
            }
        }
        if (outputItemIds != null && outputAmounts != null) {
            for (int i = 0; i < outputItemIds.length; i++) {
                outputItems.add(new SlimefunItemMatcherImpl(outputItemIds[i]));
                outputAmountsList.add(outputAmounts[i]);
            }
        }

        return new ConversionRule(inputItems, outputItems, inputAmountsList, outputAmountsList,
                energyChangeList, outputMode, processingTime, progressSlot, showProgress);
    }
}
