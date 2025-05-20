package me.xingchen.XingchenExpansion.util;



import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;


public class ConversionUtil {

    // Task state enum for explicit state management
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

    // Machine state enum for UI display
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

    // Output mode for conversion rules
    public enum OutputMode {
        ALL,
        RANDOM,
        NONE
    }

    // Interface for matching Slimefun items
    public interface SlimefunItemMatcher {
        boolean matches(@Nullable ItemStack item);

        @Nullable
        ItemStack getItem();
    }

    // Interface for providing state items
    public interface StateItemProvider {
        @Nonnull
        Map<MachineState, ItemStack> getStateItems();
    }

    // Interface for handling special cases
    public interface SpecialCaseHandler {
        @Nonnull
        Optional<ItemStack> handleSpecialCase(@Nonnull ProgressInfo progressInfo);
    }

    // Conversion rule definition
    public static class ConversionRule {
        private final List<SlimefunItemMatcher> inputItems;
        private final List<SlimefunItemMatcher> outputItems;
        private final List<Integer> inputAmounts;
        private final List<Integer> outputAmounts;
        private final OutputMode outputMode;
        private final int processingTime;
        private final int progressSlot;
        private final boolean showProgress;

        public ConversionRule(@Nonnull List<SlimefunItemMatcher> inputItems,
                              @Nonnull List<SlimefunItemMatcher> outputItems,
                              @Nonnull List<Integer> inputAmounts,
                              @Nonnull List<Integer> outputAmounts,
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

        @Nonnull
        public String getRuleId() {
            return Integer.toString(hashCode());
        }
    }

    // Progress information for UI
    public static class ProgressInfo {
        private final MachineState state;
        private final String ruleId;
        private final int processingTime;
        private final int currentProcessingTime;
        private final boolean processing;

        public ProgressInfo(@Nonnull MachineState state, @Nullable String ruleId,
                            int processingTime, int currentProcessingTime, boolean processing) {
            this.state = state;
            this.ruleId = ruleId;
            this.processingTime = processingTime;
            this.currentProcessingTime = currentProcessingTime;
            this.processing = processing;
        }

        @Nonnull
        public MachineState getState() {
            return state;
        }

        @Nullable
        public String getRuleId() {
            return ruleId;
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
            int progress = (int) ((float) currentProcessingTime / processingTime * length);
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < length; i++) {
                bar.append(i < progress ? "█" : "▒");
            }
            return bar.toString();
        }
    }

    // Default state item provider
    public static class DefaultStateItemProvider implements StateItemProvider {
        @Override
        @Nonnull
        public Map<MachineState, ItemStack> getStateItems() {
            Map<MachineState, ItemStack> items = new HashMap<>();
            items.put(MachineState.IDLE, new CustomItemStack(Material.REDSTONE, "§c状态: 待机"));
            items.put(MachineState.CONVERTING, new CustomItemStack(Material.LIME_DYE, "§a状态: 转化中"));
            items.put(MachineState.OUTPUT_FULL, new CustomItemStack(Material.REDSTONE, "§c状态: 输出槽已满"));
            items.put(MachineState.NO_INPUT, new CustomItemStack(Material.REDSTONE, "§c状态: 缺少输入"));
            items.put(MachineState.NO_ENERGY, new CustomItemStack(Material.REDSTONE, "§c状态: 能量不足"));
            return items;
        }
    }

    public abstract static class AbstractConversionProcessor {
        protected final List<ConversionRule> rules;
        protected final StateItemProvider stateItemProvider;
        protected final SpecialCaseHandler specialCaseHandler;

        protected AbstractConversionProcessor(@Nonnull List<ConversionRule> rules,
                                              @Nonnull StateItemProvider stateItemProvider,
                                              @Nonnull SpecialCaseHandler specialCaseHandler) {
            this.rules = rules;
            this.stateItemProvider = stateItemProvider;
            this.specialCaseHandler = specialCaseHandler;
        }

        // 获取任务状态
        public TaskState getTaskState(@Nonnull Location location) {
            String stateStr = BlockStorage.getLocationInfo(location, "task_state");
            if (stateStr == null) {
                return TaskState.IDLE;
            }
            try {
                return TaskState.valueOf(stateStr);
            } catch (IllegalArgumentException e) {
                return TaskState.IDLE;
            }
        }

        // 设置任务状态
        protected void setTaskState(@Nonnull Location location, @Nonnull TaskState state, @Nonnull JavaPlugin plugin) {
            BlockStorage.addBlockInfo(location, "task_state", state.name());
        }

        // 暂停：仅对 RUNNING 有效
        public boolean pause(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                             @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            if (getTaskState(location) != TaskState.RUNNING) {
                return false;
            }
            String ruleId = BlockStorage.getLocationInfo(location, "rule_id");
            if (ruleId != null) {
                String timeKey = "processing_time_" + ruleId;
                String timeStr = BlockStorage.getLocationInfo(location, timeKey);
                int currentProcessingTime = 0;
                try {
                    currentProcessingTime = timeStr != null ? Integer.parseInt(timeStr) : 0;
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("无效的处理时间: 值=" + timeStr + ", 位置=" + location);
                }
                BlockStorage.addBlockInfo(location, timeKey, String.valueOf(currentProcessingTime));
                setTaskState(location, TaskState.PAUSED, plugin);
                updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                return true;
            }
            return false;
        }

        // 继续：仅对 PAUSED 有效
        public boolean resume(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                              @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            if (getTaskState(location) != TaskState.PAUSED) {
                return false;
            }
            setTaskState(location, TaskState.RUNNING, plugin);
            updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
            return true;
        }

        // 返还物品终止：检查输入槽空间
        public boolean terminateWithReturn(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                                           @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            TaskState state = getTaskState(location);
            if (state == TaskState.IDLE) {
                return false;
            }
            String ruleId = BlockStorage.getLocationInfo(location, "rule_id");
            if (ruleId != null) {
                String timeKey = "processing_time_" + ruleId;
                returnInputItems(menu, location, plugin, inputSlots);
                BlockStorage.addBlockInfo(location, timeKey, "0");
                BlockStorage.addBlockInfo(location, "rule_id", null);
                setTaskState(location, TaskState.IDLE, plugin);
                updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                return true;
            }
            setTaskState(location, TaskState.IDLE, plugin);
            return false;
        }

        // 强制终止：不返还
        public boolean terminateForce(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                                      @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            TaskState state = getTaskState(location);
            if (state == TaskState.IDLE) {
                return false;
            }
            String ruleId = BlockStorage.getLocationInfo(location, "rule_id");
            if (ruleId != null) {
                String timeKey = "processing_time_" + ruleId;
                BlockStorage.addBlockInfo(location, timeKey, "0");
                BlockStorage.addBlockInfo(location, "rule_id", null);
                setTaskState(location, TaskState.IDLE, plugin);
                updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                return true;
            }
            setTaskState(location, TaskState.IDLE, plugin);
            return false;
        }

        // 子类实现：返还输入物品
        protected abstract void returnInputItems(@Nonnull BlockMenu menu, @Nonnull Location location,
                                                 @Nonnull JavaPlugin plugin, @Nonnull int[] inputSlots);

        // 检查输出槽空间
        public boolean canOutput(@Nullable BlockMenu menu, @Nonnull int[] outputSlots, @Nonnull ConversionRule rule,
                                 @Nonnull JavaPlugin plugin, @Nonnull Location location) {
            if (rule.getOutputMode() == OutputMode.NONE || rule.getOutputItems() == null || rule.getOutputItems().isEmpty()) {
                return true;
            }
            List<ItemStack> outputs = rule.getOutputMode() == OutputMode.RANDOM ?
                    getRandomOutput(rule, plugin, location) : getAllOutputs(rule, plugin, location);
            if (outputs.isEmpty()) {
                return true;
            }

            // 单输出槽优化（StarsQuarry）
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

            // 多输出槽
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
            if (totalAvailable < totalRequired) {
                return false;
            }

            // 详细检查
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
            if (!toPlace.isEmpty()) {
                return false;
            }
            return true;
        }
        @Nonnull
        public List<ConversionRule> getRules() {
            return Collections.unmodifiableList(rules);
        }

        // 获取进度信息
        @Nonnull
        public ProgressInfo getProgressInfo(@Nonnull Location location, @Nonnull JavaPlugin plugin,
                                            @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            String ruleId = BlockStorage.getLocationInfo(location, "rule_id");
            TaskState state = getTaskState(location);
            if (ruleId == null || state == TaskState.IDLE) {
                return new ProgressInfo(MachineState.IDLE, null, 0, 0, false);
            }

            ConversionRule rule = rules.stream()
                    .filter(r -> r.getRuleId().equals(ruleId))
                    .findFirst()
                    .orElse(null);
            if (rule == null) {
                plugin.getLogger().warning("无效的 ruleId: " + ruleId + ", location=" + location);
                return new ProgressInfo(MachineState.IDLE, null, 0, 0, false);
            }

            String timeKey = "processing_time_" + ruleId;
            String timeStr = BlockStorage.getLocationInfo(location, timeKey);
            int currentProcessingTime = 0;
            try {
                currentProcessingTime = timeStr != null ? Integer.parseInt(timeStr) : 0;
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("无效的处理时间: 值=" + timeStr + ", 位置=" + location);
            }

            MachineState machineState = state == TaskState.PAUSED ? MachineState.OUTPUT_FULL : MachineState.CONVERTING;
            return new ProgressInfo(machineState, ruleId, rule.getProcessingTime(), currentProcessingTime, true);
        }

        // 更新进度显示
        protected void updateProgressItemIfEnabled(@Nonnull BlockMenu menu, @Nonnull Location location,
                                                   @Nonnull JavaPlugin plugin, @Nonnull int[] inputSlots,
                                                   @Nonnull int[] outputSlots) {
            String ruleId = BlockStorage.getLocationInfo(location, "rule_id");
            if (ruleId == null) {
                return;
            }
            ConversionRule rule = rules.stream()
                    .filter(r -> r.getRuleId().equals(ruleId))
                    .findFirst()
                    .orElse(null);
            if (rule == null || !rule.shouldShowProgress()) {
                return;
            }

            ProgressInfo progress = getProgressInfo(location, plugin, inputSlots, outputSlots);
            ItemStack progressItem = stateItemProvider.getStateItems().getOrDefault(progress.getState(),
                    new DefaultStateItemProvider().getStateItems().get(progress.getState()));
            if (progressItem == null) {
                plugin.getLogger().warning("状态模板缺失: state=" + progress.getState() + ", location=" + location);
                progressItem = new CustomItemStack(Material.REDSTONE, "§c状态: " + progress.getState().getDisplayName());
            }

            ItemStack result = progressItem.clone();
            ItemMeta meta = result.getItemMeta();
            if (meta == null) {
                meta = plugin.getServer().getItemFactory().getItemMeta(result.getType());
                result.setItemMeta(meta);
            }
            List<String> lore = new ArrayList<>(meta.hasLore() ? meta.getLore() : Collections.emptyList());
            int statusIndex = -1;
            for (int i = 0; i < lore.size(); i++) {
                if (lore.get(i).startsWith("§7状态:")) {
                    statusIndex = i;
                    break;
                }
            }
            if (progress.isProcessing() && progress.getState() == MachineState.CONVERTING) {
                lore.add(statusIndex >= 0 ? statusIndex + 1 : lore.size(), "§7进度: " + progress.getProgressBar(10));
                lore.add(statusIndex >= 0 ? statusIndex + 2 : lore.size(), "§7剩余: " + String.format("%.1fs", progress.getRemainingSeconds()));
            }
            meta.setLore(lore);
            result.setItemMeta(meta);

            menu.replaceExistingItem(rule.getProgressSlot(), result);
            menu.addMenuClickHandler(rule.getProgressSlot(), (p, slot, item, action) -> false);
        }

        // 获取所有输出物品
        @Nonnull
        protected List<ItemStack> getAllOutputs(@Nonnull ConversionRule rule, @Nonnull JavaPlugin plugin,
                                                @Nonnull Location location) {
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

        // 获取随机输出物品
        @Nonnull
        protected List<ItemStack> getRandomOutput(@Nonnull ConversionRule rule, @Nonnull JavaPlugin plugin,
                                                  @Nonnull Location location) {
            if (rule.getOutputItems().isEmpty()) {
                return Collections.emptyList();
            }
            Random random = new Random();
            int index = random.nextInt(rule.getOutputItems().size());
            SlimefunItemMatcher matcher = rule.getOutputItems().get(index);
            ItemStack item = matcher.getItem();
            if (item == null) {
                return Collections.emptyList();
            }
            ItemStack output = item.clone();
            output.setAmount(rule.getOutputAmounts().get(index));
            return List.of(output);
        }

        public abstract boolean process(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                                        @Nonnull int[] inputSlots, @Nonnull int[] outputSlots);
    }

    // No input processor
    public static class NoInputProcessor extends AbstractConversionProcessor {
        public NoInputProcessor(@Nonnull List<ConversionRule> rules,
                                @Nonnull StateItemProvider stateItemProvider,
                                @Nonnull SpecialCaseHandler specialCaseHandler) {
            super(rules, stateItemProvider, specialCaseHandler);
        }

        @Override
        public boolean process(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                               @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            TaskState state = getTaskState(location);
            if (state == TaskState.PAUSED) {
                return false; // 等待外部 resume
            }
            if (state == TaskState.IDLE) {
                setTaskState(location, TaskState.RUNNING, plugin);
            }

            String ruleId = BlockStorage.getLocationInfo(location, "rule_id");
            String timeKey = ruleId != null ? "processing_time_" + ruleId : null;
            int currentProcessingTime = 0;
            ConversionRule activeRule = null;

            if (ruleId != null && timeKey != null) {
                activeRule = rules.stream().filter(r -> r.getRuleId().equals(ruleId)).findFirst().orElse(null);
                if (activeRule != null) {
                    String timeStr = BlockStorage.getLocationInfo(location, timeKey);
                    try {
                        currentProcessingTime = timeStr != null ? Integer.parseInt(timeStr) : 0;
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("无效的处理时间: 值=" + timeStr + ", 位置=" + location);
                        currentProcessingTime = 0;
                    }
                }
            }

            for (ConversionRule rule : rules) {
                if (activeRule == null || !activeRule.getRuleId().equals(rule.getRuleId())) {
                    BlockStorage.addBlockInfo(location, "rule_id", rule.getRuleId());
                    timeKey = "processing_time_" + rule.getRuleId();
                    currentProcessingTime = 0;
                    activeRule = rule;
                    setTaskState(location, TaskState.RUNNING, plugin);
                }

                currentProcessingTime++;
                if (currentProcessingTime >= rule.getProcessingTime()) {
                    List<ItemStack> outputs = rule.getOutputMode() == OutputMode.RANDOM ?
                            getRandomOutput(rule, plugin, location) : getAllOutputs(rule, plugin, location);
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
                    currentProcessingTime = 0;
                    BlockStorage.addBlockInfo(location, timeKey, "0");
                    setTaskState(location, TaskState.IDLE, plugin);
                    updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                    return true;
                } else {
                    BlockStorage.addBlockInfo(location, timeKey, String.valueOf(currentProcessingTime));
                    updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                    return false;
                }
            }

            setTaskState(location, TaskState.IDLE, plugin);
            updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
            return false;
        }

        @Override
        protected void returnInputItems(@Nonnull BlockMenu menu, @Nonnull Location location,
                                        @Nonnull JavaPlugin plugin, @Nonnull int[] inputSlots) {
            // 无输入，无需返还
        }
    }

    // Single input processor
    public static class SingleInputProcessor extends AbstractConversionProcessor {
        public SingleInputProcessor(@Nonnull List<ConversionRule> rules,
                                    @Nonnull StateItemProvider stateItemProvider,
                                    @Nonnull SpecialCaseHandler specialCaseHandler) {
            super(rules, stateItemProvider, specialCaseHandler);
        }

        @Override
        public boolean process(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                               @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            TaskState state = getTaskState(location);
            if (state == TaskState.PAUSED) {
                return false; // 等待外部 resume
            }
            if (state == TaskState.IDLE) {
                setTaskState(location, TaskState.RUNNING, plugin);
            }

            String ruleId = BlockStorage.getLocationInfo(location, "rule_id");
            String timeKey = ruleId != null ? "processing_time_" + ruleId : null;
            int currentProcessingTime = 0;
            ConversionRule activeRule = null;

            if (ruleId != null && timeKey != null) {
                activeRule = rules.stream().filter(r -> r.getRuleId().equals(ruleId)).findFirst().orElse(null);
                if (activeRule != null) {
                    String timeStr = BlockStorage.getLocationInfo(location, timeKey);
                    try {
                        currentProcessingTime = timeStr != null ? Integer.parseInt(timeStr) : 0;
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("无效的处理时间: 值=" + timeStr + ", 位置=" + location);
                        currentProcessingTime = 0;
                    }
                }
            }

            for (ConversionRule rule : rules) {
                if (activeRule == null || !activeRule.getRuleId().equals(rule.getRuleId())) {
                    BlockStorage.addBlockInfo(location, "rule_id", rule.getRuleId());
                    timeKey = "processing_time_" + rule.getRuleId();
                    currentProcessingTime = 0;
                    activeRule = rule;
                    setTaskState(location, TaskState.RUNNING, plugin);
                }

                currentProcessingTime++;
                if (currentProcessingTime >= rule.getProcessingTime()) {
                    List<ItemStack> outputs = rule.getOutputMode() == OutputMode.RANDOM ?
                            getRandomOutput(rule, plugin, location) : getAllOutputs(rule, plugin, location);
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
                    for (int slot : inputSlots) {
                        ItemStack item = menu.getItemInSlot(slot);
                        if (item != null && item.getType() != Material.AIR) {
                            item.setAmount(item.getAmount() - 1);
                            if (item.getAmount() <= 0) {
                                menu.replaceExistingItem(slot, null);
                            }
                            break;
                        }
                    }
                    currentProcessingTime = 0;
                    BlockStorage.addBlockInfo(location, timeKey, "0");
                    setTaskState(location, TaskState.IDLE, plugin);
                    updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                    return true;
                } else {
                    BlockStorage.addBlockInfo(location, timeKey, String.valueOf(currentProcessingTime));
                    updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
                    return false;
                }
            }

            setTaskState(location, TaskState.IDLE, plugin);
            updateProgressItemIfEnabled(menu, location, plugin, inputSlots, outputSlots);
            return false;
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
                        plugin.getLogger().info("输入槽满，物品掉落: location=" + location + ", item=" + item.getType());
                    }
                    menu.replaceExistingItem(slot, null);
                }
            }
        }
    }

    // Slimefun item matcher implementation
    public static class SlimefunItemMatcherImpl implements SlimefunItemMatcher {
        private final String itemId;

        public SlimefunItemMatcherImpl(@Nonnull String itemId) {
            this.itemId = itemId;
        }

        @Override
        public boolean matches(@Nullable ItemStack item) {
            if (item == null || item.getType() == Material.AIR) {
                return false;
            }
            SlimefunItem sfItem = SlimefunItem.getByItem(item);
            return sfItem != null && sfItem.getId().equals(itemId);
        }

        @Override
        @Nullable
        public ItemStack getItem() {
            SlimefunItem sfItem = SlimefunItem.getById(itemId);
            return sfItem != null ? sfItem.getItem().clone() : null;
        }
    }
}

