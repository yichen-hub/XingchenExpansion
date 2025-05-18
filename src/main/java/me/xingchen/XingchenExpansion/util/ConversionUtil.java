package me.xingchen.XingchenExpansion.util;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ConversionUtil {
    private static final Random RANDOM = new Random();

    // 输出模式枚举
    public enum OutputMode {
        RANDOM, ALL, NONE
    }

    // 转化规则类
    public static class ConversionRule {
        private final String ruleId;
        @Nullable private final List<String> inputItems;
        @Nullable private final List<String> outputItems;
        @Nullable private final List<Integer> inputAmounts;
        @Nullable private final List<Integer> outputAmounts;
        private final OutputMode outputMode;
        private final int conversionTime;

        public ConversionRule(@Nullable List<String> inputItems, @Nullable List<String> outputItems,
                              @Nullable List<Integer> inputAmounts, @Nullable List<Integer> outputAmounts,
                              @Nonnull OutputMode outputMode, int conversionTime) {
            this.ruleId = UUID.randomUUID().toString();
            if (outputItems != null && outputAmounts != null && outputItems.size() != outputAmounts.size()) {
                throw new IllegalArgumentException("outputItems 和 outputAmounts 长度必须一致");
            }
            if (inputItems != null && inputAmounts != null && inputItems.size() != inputAmounts.size()) {
                throw new IllegalArgumentException("inputItems 和 inputAmounts 长度必须一致");
            }
            this.inputItems = inputItems;
            this.outputItems = outputItems;
            this.inputAmounts = inputAmounts;
            this.outputAmounts = outputAmounts;
            this.outputMode = outputMode;
            this.conversionTime = conversionTime;
        }

        public String getRuleId() { return ruleId; }
        @Nullable public List<String> getInputItems() { return inputItems; }
        @Nullable public List<String> getOutputItems() { return outputItems; }
        @Nullable public List<Integer> getInputAmounts() { return inputAmounts; }
        @Nullable public List<Integer> getOutputAmounts() { return outputAmounts; }
        public OutputMode getOutputMode() { return outputMode; }
        public int getConversionTime() { return conversionTime; }
    }

    // 转化进度信息
    public static class ProgressInfo {
        private final boolean isProcessing;
        private final String ruleId;
        private final int currentTicks;
        private final int totalTicks;

        public ProgressInfo(boolean isProcessing, @Nullable String ruleId, int currentTicks, int totalTicks) {
            this.isProcessing = isProcessing;
            this.ruleId = ruleId;
            this.currentTicks = currentTicks;
            this.totalTicks = totalTicks;
        }

        public boolean isProcessing() { return isProcessing; }
        @Nullable public String getRuleId() { return ruleId; }
        public int getCurrentTicks() { return currentTicks; }
        public int getTotalTicks() { return totalTicks; }

        public double getProgressPercentage() {
            return totalTicks > 0 ? (1.0 - (double) currentTicks / totalTicks) * 100 : 0;
        }
        public String getProgressBar(int barLength) {
            int filled = (int) (getProgressPercentage() / 100 * barLength);
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < barLength; i++) {
                bar.append(i < filled ? "■" : "□");
            }
            return bar.toString();
        }
        public double getRemainingSeconds() {
            return currentTicks / 20.0;
        }
    }

    // 转化处理器接口
    public interface ConversionProcessor {
        boolean process(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                        @Nonnull int[] inputSlots, @Nonnull int[] outputSlots);
        @Nonnull ProgressInfo getProgressInfo(@Nonnull Location location, @Nonnull JavaPlugin plugin);
        @Nonnull List<ConversionRule> getRules();
    }

    // 单例输入处理器
    public static class SingleInputProcessor implements ConversionProcessor {
        private final List<ConversionRule> rules;
        private final Map<String, ConversionRule> ruleMap;

        public SingleInputProcessor(@Nonnull List<ConversionRule> rules) {
            this.rules = rules;
            this.ruleMap = rules.stream().collect(Collectors.toMap(ConversionRule::getRuleId, r -> r));
        }

        @Override
        @Nonnull
        public List<ConversionRule> getRules() {return rules;}

        @Override
        public boolean process(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                               @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            String ruleId = BlockStorage.getLocationInfo(location, "rule_id");
            String timeKey = ruleId != null ? "processing_time_" + ruleId : null;
            int currentProcessingTime = 0;

            if (ruleId != null && timeKey != null) {
                String timeStr = BlockStorage.getLocationInfo(location, timeKey);
                try {
                    currentProcessingTime = timeStr != null ? Integer.parseInt(timeStr) : 0;
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("处理时间格式错误: ruleId=" + ruleId + ", 时间=" + timeStr + ", 位置=" + location);
                }
            }

            if (currentProcessingTime > 0 && ruleId != null && ruleMap.containsKey(ruleId)) {
                ConversionRule rule = ruleMap.get(ruleId);
                currentProcessingTime--;
                BlockStorage.addBlockInfo(location, timeKey, String.valueOf(currentProcessingTime));

                if (currentProcessingTime <= 0) {
                    boolean success = processOutput(menu, outputSlots, rule, plugin, location);
                    if (success) {
                        BlockStorage.addBlockInfo(location, timeKey, null);
                        BlockStorage.addBlockInfo(location, "rule_id", null);
                    } else {
                        BlockStorage.addBlockInfo(location, timeKey, "1");
                    }
                }
                return true;
            }

            for (ConversionRule rule : rules) {
                if (rule.getInputItems() == null || rule.getInputAmounts() == null) continue;

                int selectedSlot = -1;
                String selectedInputId = null;
                for (int slot : inputSlots) {
                    ItemStack item = menu.getItemInSlot(slot);
                    String itemId = Util.getSlimefunId(item);
                    if (itemId != null && item != null && rule.getInputItems().contains(itemId) &&
                            item.getAmount() >= rule.getInputAmounts().get(0)) {
                        selectedSlot = slot;
                        selectedInputId = itemId;
                        break;
                    }
                }

                if (selectedSlot != -1) {
                    ItemStack inputItem = menu.getItemInSlot(selectedSlot);
                    int newAmount = inputItem.getAmount() - rule.getInputAmounts().get(0);
                    ItemStack newItem = newAmount > 0 ? inputItem.clone() : null;
                    if (newItem != null) newItem.setAmount(newAmount);
                    menu.replaceExistingItem(selectedSlot, newItem);

                    BlockStorage.addBlockInfo(location, "rule_id", rule.getRuleId());
                    BlockStorage.addBlockInfo(location, "processing_time_" + rule.getRuleId(), String.valueOf(rule.getConversionTime()));
                    return true;
                }
            }

            return false;
        }

        @Override
        @Nonnull
        public ProgressInfo getProgressInfo(@Nonnull Location location, @Nonnull JavaPlugin plugin) {
            String ruleId = BlockStorage.getLocationInfo(location, "rule_id");
            String timeKey = ruleId != null ? "processing_time_" + ruleId : null;
            int currentProcessingTime = 0;
            int totalTicks = 0;

            if (ruleId != null && timeKey != null && ruleMap.containsKey(ruleId)) {
                String timeStr = BlockStorage.getLocationInfo(location, timeKey);
                try {
                    currentProcessingTime = timeStr != null ? Integer.parseInt(timeStr) : 0;
                    totalTicks = ruleMap.get(ruleId).getConversionTime();
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("处理时间格式错误: ruleId=" + ruleId + ", 时间=" + timeStr + ", 位置=" + location);
                }
            }
            return new ProgressInfo(currentProcessingTime > 0, ruleId, currentProcessingTime, totalTicks);
        }
    }

    // 多例输入处理器
    public static class MultiInputProcessor implements ConversionProcessor {
        private final List<ConversionRule> rules;
        private final Map<String, ConversionRule> ruleMap;

        public MultiInputProcessor(@Nonnull List<ConversionRule> rules) {
            this.rules = rules;
            this.ruleMap = rules.stream().collect(Collectors.toMap(ConversionRule::getRuleId, r -> r));
        }

        @Override
        @Nonnull
        public List<ConversionRule> getRules() {return rules;}

        @Override
        public boolean process(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                               @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            String ruleId = BlockStorage.getLocationInfo(location, "rule_id");
            String timeKey = ruleId != null ? "processing_time_" + ruleId : null;
            int currentProcessingTime = 0;

            if (ruleId != null && timeKey != null) {
                String timeStr = BlockStorage.getLocationInfo(location, timeKey);
                try {
                    currentProcessingTime = timeStr != null ? Integer.parseInt(timeStr) : 0;
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("处理时间格式错误: ruleId=" + ruleId + ", 时间=" + timeStr + ", 位置=" + location);
                }
            }

            if (currentProcessingTime > 0 && ruleId != null && ruleMap.containsKey(ruleId)) {
                ConversionRule rule = ruleMap.get(ruleId);
                currentProcessingTime--;
                BlockStorage.addBlockInfo(location, timeKey, String.valueOf(currentProcessingTime));

                if (currentProcessingTime <= 0) {
                    boolean success = processOutput(menu, outputSlots, rule, plugin, location);
                    if (success) {
                        BlockStorage.addBlockInfo(location, timeKey, null);
                        BlockStorage.addBlockInfo(location, "rule_id", null);
                    } else {
                        BlockStorage.addBlockInfo(location, timeKey, "1");
                    }
                }
                return true;
            }

            for (ConversionRule rule : rules) {
                if (rule.getInputItems() == null || rule.getInputAmounts() == null) continue;

                Map<String, Integer> required = new HashMap<>();
                for (int i = 0; i < rule.getInputItems().size(); i++) {
                    required.put(rule.getInputItems().get(i), rule.getInputAmounts().get(i));
                }

                Map<Integer, ItemStack> slotItems = new HashMap<>();
                for (int slot : inputSlots) {
                    ItemStack item = menu.getItemInSlot(slot);
                    String itemId = Util.getSlimefunId(item);
                    if (itemId != null && item != null && required.containsKey(itemId)) {
                        int current = required.get(itemId);
                        if (item.getAmount() >= current) {
                            required.remove(itemId);
                            slotItems.put(slot, item);
                        }
                    }
                }

                if (required.isEmpty()) {
                    for (Map.Entry<Integer, ItemStack> entry : slotItems.entrySet()) {
                        int slot = entry.getKey();
                        ItemStack item = entry.getValue();
                        String itemId = Util.getSlimefunId(item);
                        int amount = rule.getInputAmounts().get(rule.getInputItems().indexOf(itemId));
                        int newAmount = item.getAmount() - amount;
                        ItemStack newItem = newAmount > 0 ? item.clone() : null;
                        if (newItem != null) newItem.setAmount(newAmount);
                        menu.replaceExistingItem(slot, newItem);
                    }

                    BlockStorage.addBlockInfo(location, "rule_id", rule.getRuleId());
                    BlockStorage.addBlockInfo(location, "processing_time_" + rule.getRuleId(), String.valueOf(rule.getConversionTime()));
                    return true;
                }
            }

            return false;
        }

        @Override
        @Nonnull
        public ProgressInfo getProgressInfo(@Nonnull Location location, @Nonnull JavaPlugin plugin) {
            String ruleId = BlockStorage.getLocationInfo(location, "rule_id");
            String timeKey = ruleId != null ? "processing_time_" + ruleId : null;
            int currentProcessingTime = 0;
            int totalTicks = 0;

            if (ruleId != null && timeKey != null && ruleMap.containsKey(ruleId)) {
                String timeStr = BlockStorage.getLocationInfo(location, timeKey);
                try {
                    currentProcessingTime = timeStr != null ? Integer.parseInt(timeStr) : 0;
                    totalTicks = ruleMap.get(ruleId).getConversionTime();
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("处理时间格式错误: ruleId=" + ruleId + ", 时间=" + timeStr + ", 位置=" + location);
                }
            }
            return new ProgressInfo(currentProcessingTime > 0, ruleId, currentProcessingTime, totalTicks);
        }
    }

    // 无输入处理器
    public static class NoInputProcessor implements ConversionProcessor {
        private final List<ConversionRule> rules;
        private final Map<String, ConversionRule> ruleMap;

        public NoInputProcessor(@Nonnull List<ConversionRule> rules) {
            this.rules = rules;
            this.ruleMap = rules.stream().collect(Collectors.toMap(ConversionRule::getRuleId, r -> r));
        }

        @Override
        @Nonnull
        public List<ConversionRule> getRules() {
            return rules;
        }

        @Override
        public boolean process(@Nonnull BlockMenu menu, @Nonnull Location location, @Nonnull JavaPlugin plugin,
                               @Nonnull int[] inputSlots, @Nonnull int[] outputSlots) {
            String ruleId = BlockStorage.getLocationInfo(location, "rule_id");
            String timeKey = ruleId != null ? "processing_time_" + ruleId : null;
            int currentProcessingTime = 0;

            if (ruleId != null && timeKey != null) {
                String timeStr = BlockStorage.getLocationInfo(location, timeKey);
                try {
                    currentProcessingTime = timeStr != null ? Integer.parseInt(timeStr) : 0;
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("处理时间格式错误: ruleId=" + ruleId + ", 时间=" + timeStr + ", 位置=" + location);
                }
            }

            if (currentProcessingTime > 0 && ruleId != null && ruleMap.containsKey(ruleId)) {
                ConversionRule rule = ruleMap.get(ruleId);
                currentProcessingTime--;
                BlockStorage.addBlockInfo(location, timeKey, String.valueOf(currentProcessingTime));

                if (currentProcessingTime <= 0) {
                    boolean success = processOutput(menu, outputSlots, rule, plugin, location);
                    if (success) {
                        BlockStorage.addBlockInfo(location, timeKey, null);
                        BlockStorage.addBlockInfo(location, "rule_id", null);
                    } else {
                        BlockStorage.addBlockInfo(location, timeKey, "1");
                    }
                }
                return true;
            }

            for (ConversionRule rule : rules) {
                BlockStorage.addBlockInfo(location, "rule_id", rule.getRuleId());
                BlockStorage.addBlockInfo(location, "processing_time_" + rule.getRuleId(), String.valueOf(rule.getConversionTime()));
                return true;
            }

            return false;
        }

        @Override
        @Nonnull
        public ProgressInfo getProgressInfo(@Nonnull Location location, @Nonnull JavaPlugin plugin) {
            String ruleId = BlockStorage.getLocationInfo(location, "rule_id");
            String timeKey = ruleId != null ? "processing_time_" + ruleId : null;
            int currentProcessingTime = 0;
            int totalTicks = 0;

            if (ruleId != null && timeKey != null && ruleMap.containsKey(ruleId)) {
                String timeStr = BlockStorage.getLocationInfo(location, timeKey);
                try {
                    currentProcessingTime = timeStr != null ? Integer.parseInt(timeStr) : 0;
                    totalTicks = ruleMap.get(ruleId).getConversionTime();
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("处理时间格式错误: ruleId=" + ruleId + ", 时间=" + timeStr + ", 位置=" + location);
                }
            }
            return new ProgressInfo(currentProcessingTime > 0, ruleId, currentProcessingTime, totalTicks);
        }
    }

    private static boolean processOutput(@Nonnull BlockMenu menu, @Nonnull int[] outputSlots,
                                         @Nonnull ConversionRule rule, @Nonnull JavaPlugin plugin,
                                         @Nonnull Location location) {
        if (rule.getOutputMode() == OutputMode.NONE || rule.getOutputItems() == null || rule.getOutputItems().isEmpty()) {
            return true;
        }

        List<ItemStack> outputs = rule.getOutputMode() == OutputMode.RANDOM ?
                getRandomOutput(rule, plugin, location) :
                getAllOutputs(rule, plugin, location);

        List<ItemStack> toPlace = new ArrayList<>(outputs);
        for (int slot : outputSlots) {
            ItemStack existing = menu.getItemInSlot(slot);
            Iterator<ItemStack> iterator = toPlace.iterator();
            while (iterator.hasNext()) {
                ItemStack output = iterator.next();
                if (existing == null) {
                    menu.replaceExistingItem(slot, output.clone());
                    iterator.remove();
                } else if (existing.isSimilar(output)) {
                    int totalAmount = existing.getAmount() + output.getAmount();
                    if (totalAmount <= existing.getMaxStackSize()) {
                        existing.setAmount(totalAmount);
                        menu.replaceExistingItem(slot, existing);
                        iterator.remove();
                    } else {
                        existing.setAmount(existing.getMaxStackSize());
                        menu.replaceExistingItem(slot, existing);
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

    private static List<ItemStack> getRandomOutput(@Nonnull ConversionRule rule, @Nonnull JavaPlugin plugin, @Nonnull Location location) {
        List<String> outputIds = rule.getOutputItems();
        List<Integer> outputAmounts = rule.getOutputAmounts();
        int index = RANDOM.nextInt(outputIds.size());
        String outputId = outputIds.get(index);
        ItemStack output = SlimefunItem.getById(outputId) != null ?
                SlimefunItem.getById(outputId).getItem().clone() : null;
        if (output == null) {
            plugin.getLogger().warning("无效输出物品 ID: " + outputId + ", 位置=" + location);
            return List.of();
        }
        output.setAmount(outputAmounts.get(index));
        return List.of(output);
    }

    private static List<ItemStack> getAllOutputs(@Nonnull ConversionRule rule, @Nonnull JavaPlugin plugin, @Nonnull Location location) {
        List<ItemStack> outputs = new ArrayList<>();
        for (int i = 0; i < rule.getOutputItems().size(); i++) {
            String outputId = rule.getOutputItems().get(i);
            ItemStack output = SlimefunItem.getById(outputId) != null ?
                    SlimefunItem.getById(outputId).getItem().clone() : null;
            if (output == null) {
                plugin.getLogger().warning("无效输出物品 ID: " + outputId + ", 位置=" + location);
                continue;
            }
            output.setAmount(rule.getOutputAmounts().get(i));
            outputs.add(output);
        }
        return outputs;
    }
}