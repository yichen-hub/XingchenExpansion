package me.xingchen.XingchenExpansion.machine;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.LoreBuilder;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public abstract class StarsPurifier extends SlimefunItem implements EnergyNetComponent, Listener {
    // 输入槽位，用于放置待处理的物品
    protected static final int INPUT_SLOT = 10;
    // 状态槽位，用于显示机器运行状态
    protected static final int STATUS_SLOT = 13;
    // 输出槽位数组，用于存放处理后的物品
    protected static final int[] OUTPUT_SLOTS = {14, 15, 16};
    // 受保护槽位，禁止随意交互
    protected static final int[] PROTECTED_SLOTS = {INPUT_SLOT, STATUS_SLOT, 14, 15, 16};
    // BlockStorage 中存储处理时间的键
    protected static final String PROCESSING_TIME_KEY = "processing_time";
    // BlockStorage 中存储输入物品 ID 的键
    protected static final String INPUT_ID_KEY = "input_id";
    // Slimefun 物品的命名空间键，用于持久化数据
    protected static final NamespacedKey SLIMEFUN_KEY = new NamespacedKey("slimefun", "slimefun_item");
    // 界面预设，用于定义机器的 GUI 布局
    protected final BlockMenuPreset preset;
    // 每 tick 消耗的能量（单位：J），默认值为 0
    protected final int energyConsumption;
    // 处理一个物品所需的时间（单位：tick），默认值为 0
    protected final int processingTime;
    // 输入物品的 Slimefun ID，默认值为 null
    protected final String inputId;
    // 输出物品的 Slimefun ID，默认值为 null
    protected final String outputId;
    // 每次处理输出的物品数量，默认值为 0
    protected final int outputAmount;
    // 存储所有活跃净化器位置的列表
    protected final List<Location> purifierLocations = new ArrayList<>();
    // 插件实例，用于访问数据文件夹和日志
    protected final JavaPlugin plugin;
    // 净化器 ID，用于从配置文件中加载特定配置
    protected final String purifierId;

    /**
     * 构造函数，初始化净化器的基本属性和配置。
     *
     * @param itemGroup   物品组，用于在 Slimefun 指南中分类
     * @param item        SlimefunItemStack，表示净化器的物品
     * @param recipeType  配方类型，定义合成方式
     * @param recipe      合成配方
     * @param plugin      XingchenExpansion 插件实例
     * @param purifierId  净化器 ID，用于加载配置文件中的对应配置
     */
    public StarsPurifier(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, XingchenExpansion plugin, String purifierId) {
        super(itemGroup, item, recipeType, recipe);
        this.plugin = plugin;
        this.purifierId = purifierId;

        // 注册事件监听器
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // 加载配置文件
        File configFile = new File(plugin.getDataFolder(), "purifiers.yml");
        if (!configFile.exists()) {
            plugin.saveResource("purifiers.yml", false); // 若文件不存在，生成默认文件
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // 获取净化器配置
        var section = config.getConfigurationSection("purifiers." + purifierId);
        if (section == null) {
            plugin.getLogger().severe("未找到 purifiers." + purifierId + " 的配置，净化器将被禁用！");
            this.energyConsumption = 0;
            this.processingTime = 0;
            this.inputId = null;
            this.outputId = null;
            this.outputAmount = 0;
            this.preset = null;
            return; // 提前返回，避免初始化 preset
        }

        // 从配置文件读取参数
        int tempEnergyConsumption = section.getInt("energy_consumption");
        int tempProcessingTime = section.getInt("processing_time");
        String tempInputId = section.getString("input_id");
        String tempOutputId = section.getString("output_id");
        int tempOutputAmount = section.getInt("output_amount");

        // 验证配置有效性
        if (tempEnergyConsumption <= 0 || tempProcessingTime <= 0 || tempInputId == null || tempOutputId == null || tempOutputAmount <= 0) {
            plugin.getLogger().severe("无效的净化器配置，ID: " + purifierId + "，净化器将被禁用！");
            this.energyConsumption = 0;
            this.processingTime = 0;
            this.inputId = null;
            this.outputId = null;
            this.outputAmount = 0;
            this.preset = null;
            return; // 提前返回，避免初始化 preset
        }

        // 赋值有效配置
        this.energyConsumption = tempEnergyConsumption;
        this.processingTime = tempProcessingTime;
        this.inputId = tempInputId;
        this.outputId = tempOutputId;
        this.outputAmount = tempOutputAmount;

        // 初始化界面预设，明确指定 27 格
        this.preset = new BlockMenuPreset(getId(), item.getDisplayName()) {
            @Override
            public void init() {
                setupMenuPreset(); // 设置 GUI 布局
            }

            @Override
            public void newInstance(BlockMenu menu, Block b) {
                StarsPurifier.this.newItemMenu(menu); // 初始化新菜单
            }

            @Override
            public boolean canOpen(Block b, Player p) {
                // 检查玩家是否有权限打开界面
                return p.hasPermission("slimefun.inventory.bypass") || Slimefun.getPermissionsService().hasPermission(p, StarsPurifier.this);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                // 定义物品传输槽位
                if (flow == ItemTransportFlow.INSERT) {
                    return new int[]{INPUT_SLOT};
                } else {
                    return OUTPUT_SLOTS;
                }
            }

            @Override
            public int getSize() {
                return 27; // 明确指定 GUI 为 27 格（3 行）
            }
        };

        // 添加每 tick 执行的处理器
        addItemHandler(new BlockTicker() {
            @Override
            public void tick(Block b, SlimefunItem sf, me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config data) {
                StarsPurifier.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return true; // 同步执行以避免并发问题
            }
        });

        // 添加方块破坏处理器
        addItemHandler(new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(BlockBreakEvent e, ItemStack item, List<ItemStack> drops) {
                Block b = e.getBlock();
                BlockMenu inv = BlockStorage.getInventory(b);
                if (inv != null) {
                    inv.dropItems(b.getLocation(), INPUT_SLOT); // 掉落输入槽物品
                    inv.dropItems(b.getLocation(), OUTPUT_SLOTS); // 掉落输出槽物品
                }
                BlockStorage.clearBlockInfo(b); // 清除 BlockStorage 数据
            }
        });

        plugin.getLogger().info("注册机器: " + purifierId + ", 输入=" + inputId + ", 输出=" + outputId);
    }

    /**
     * 获取界面预设。
     *
     * @return BlockMenuPreset 实例，或 null（如果配置无效）
     */
    public BlockMenuPreset getPreset() {
        return preset;
    }

    /**
     * 每 tick 执行的逻辑，调用 process 方法处理净化逻辑。
     *
     * @param b 机器所在的方块
     */
    protected void tick(Block b) {
        if (preset == null) return; // 如果 preset 为 null，跳过处理
        process(b.getLocation());
    }

    /**
     * 处理净化器的核心逻辑，包括初始化、物品处理和输出。
     * 修复了首次输入物品直接消耗问题，确保输出量等于输入量。
     *
     * @param location 机器的位置
     */
    public void process(@Nonnull Location location) {
        if (preset == null) return; // 如果 preset 为 null，跳过处理

        // 初始化 BlockStorage
        if (BlockStorage.getLocationInfo(location, "id") == null) {
            BlockStorage.addBlockInfo(location, "id", getId());
            BlockStorage.addBlockInfo(location, "energy", "0");
            plugin.getLogger().info("初始化 BlockStorage: 位置=" + location + ", ID=" + getId());
        }

        // 调试 BlockStorage
        String storedEnergy = BlockStorage.getLocationInfo(location, "energy");
        plugin.getLogger().info("BlockStorage 能量: 位置=" + location + ", energy=" + storedEnergy);
        StringBuilder jsonBuilder = new StringBuilder("{");
        String[] keys = {"id", "energy", PROCESSING_TIME_KEY, INPUT_ID_KEY};
        boolean first = true;
        for (String key : keys) {
            String value = BlockStorage.getLocationInfo(location, key);
            if (value != null) {
                if (!first) jsonBuilder.append(",");
                jsonBuilder.append("\"").append(key).append("\":\"").append(value).append("\"");
                first = false;
            }
        }
        jsonBuilder.append("}");
        plugin.getLogger().info("BlockStorage 数据: 位置=" + location + ", 数据=" + jsonBuilder.toString());

        BlockMenu menu = BlockStorage.getInventory(location.getBlock());
        if (menu == null) {
            try {
                menu = new BlockMenu(preset, location);
                newItemMenu(menu); // 初始化菜单，包括背景填充
                purifierLocations.add(location.clone());
                plugin.getLogger().info("菜单初始化: 位置=" + location);
            } catch (IllegalStateException e) {
                plugin.getLogger().warning("无法初始化 BlockStorage: 位置=" + location + ", 错误=" + e.getMessage());
                return;
            }
        }

        // 获取当前处理时间
        String processingTimeStr = BlockStorage.getLocationInfo(location, PROCESSING_TIME_KEY);
        int currentProcessingTime = 0;
        try {
            currentProcessingTime = processingTimeStr != null ? Integer.parseInt(processingTimeStr) : 0;
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("处理时间格式错误: 值=" + processingTimeStr + ", 位置=" + location);
        }

        // 如果正在处理，检查能量并继续
        if (currentProcessingTime > 0) {
            if (!takeCharge(location)) {
                updateStatus(menu);
                return;
            }

            // 递减处理时间
            currentProcessingTime--;
            BlockStorage.addBlockInfo(location, PROCESSING_TIME_KEY, String.valueOf(currentProcessingTime));
            plugin.getLogger().info("处理中: 剩余时间=" + currentProcessingTime + ", 位置=" + location);

            // 处理完成，生成输出
            if (currentProcessingTime <= 0) {
                ItemStack output = SlimefunItem.getById(outputId) != null ? SlimefunItem.getById(outputId).getItem().clone() : null;
                if (output == null) {
                    plugin.getLogger().warning("无效输出物品ID: " + outputId + ", 位置=" + location);
                    BlockStorage.addBlockInfo(location, PROCESSING_TIME_KEY, null);
                    BlockStorage.addBlockInfo(location, INPUT_ID_KEY, null);
                    updateStatus(menu);
                    return;
                }
                output.setAmount(outputAmount);

                // 尝试放入输出槽
                boolean outputPlaced = false;
                for (int slot : OUTPUT_SLOTS) {
                    ItemStack existing = menu.getItemInSlot(slot);
                    if (existing == null) {
                        menu.replaceExistingItem(slot, output.clone());
                        outputPlaced = true;
                        break;
                    } else if (existing.isSimilar(output)) {
                        int totalAmount = existing.getAmount() + output.getAmount();
                        if (totalAmount <= existing.getMaxStackSize()) {
                            existing.setAmount(totalAmount);
                            menu.replaceExistingItem(slot, existing);
                            outputPlaced = true;
                            break;
                        } else {
                            existing.setAmount(existing.getMaxStackSize());
                            output.setAmount(totalAmount - existing.getMaxStackSize());
                            menu.replaceExistingItem(slot, existing);
                        }
                    }
                }
                if (!outputPlaced) {
                    plugin.getLogger().warning("输出槽已满: 位置=" + location);
                    // 暂停处理，等待输出槽有空间
                    BlockStorage.addBlockInfo(location, PROCESSING_TIME_KEY, "1");
                } else {
                    plugin.getLogger().info("生成输出: ID=" + outputId + ", 数量=" + outputAmount + ", 位置=" + location);
                    BlockStorage.addBlockInfo(location, PROCESSING_TIME_KEY, null);
                    BlockStorage.addBlockInfo(location, INPUT_ID_KEY, null);
                }
            }
            updateStatus(menu);
            return;
        }

        // 检查输入槽位，启动新处理
        ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
        if (inputItem == null || inputItem.getType() == Material.AIR) {
            updateStatus(menu);
            return;
        }

        String inputId = getInputId(inputItem);
        if (inputId == null || !inputId.equals(this.inputId)) {
            updateStatus(menu);
            return;
        }

        // 检查能量
        if (!takeCharge(location)) {
            updateStatus(menu);
            return;
        }

        // 消耗一个输入物品
        int newAmount = inputItem.getAmount() - 1;
        ItemStack newItem = newAmount > 0 ? inputItem.clone() : null;
        if (newItem != null) newItem.setAmount(newAmount);
        menu.replaceExistingItem(INPUT_SLOT, newItem);
        plugin.getLogger().info("消耗输入物品: ID=" + inputId + ", 位置=" + location);

        // 设置处理时间
        currentProcessingTime = processingTime;
        BlockStorage.addBlockInfo(location, PROCESSING_TIME_KEY, String.valueOf(currentProcessingTime));
        BlockStorage.addBlockInfo(location, INPUT_ID_KEY, inputId);
        plugin.getLogger().info("开始新处理: 时间=" + processingTime + ", 位置=" + location);

        updateStatus(menu);
    }

    /**
     * 更新机器的 GUI 状态，显示运行状态、能量和剩余时间。
     *
     * @param menu 机器的 BlockMenu
     */
    protected void updateStatus(BlockMenu menu) {
        ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
        Location location = menu.getLocation();
        String statusText = "§c待机";
        int remainingTicks = 0;
        int currentCharge = getCharge(location);

        String processingTimeStr = BlockStorage.getLocationInfo(location, PROCESSING_TIME_KEY);
        try {
            remainingTicks = processingTimeStr != null ? Integer.parseInt(processingTimeStr) : 0;
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("GUI 处理时间格式错误: 值=" + processingTimeStr + ", 位置=" + location);
        }

        String powerPerTick = LoreBuilder.powerPerSecond(energyConsumption);
        String powerCharged = LoreBuilder.powerCharged(currentCharge, getCapacity());

        String inputId = getInputId(inputItem);
        if ((inputId != null && inputId.equals(this.inputId) || remainingTicks > 0) && currentCharge >= energyConsumption) {
            statusText = remainingTicks > 0 ? "§a运行中 (§e剩余: " + String.format("%.1f", remainingTicks / 20.0) + "秒)" : "§a运行中";
        } else {
            statusText = inputId == null || !inputId.equals(this.inputId) ? "§c待机 (§e无有效输入)" : "§c待机 (§e能量不足)";
        }

        ItemStack status = new CustomItemStack(Material.REDSTONE, "§c状态: " + statusText, powerPerTick, powerCharged);
        menu.replaceExistingItem(STATUS_SLOT, status); // 设置状态物品
        menu.addMenuClickHandler(STATUS_SLOT, ChestMenuUtils.getEmptyClickHandler()); // 设置空处理器防止交互
    }

    /**
     * 尝试消耗指定位置的能量。
     *
     * @param location 机器位置
     * @return 是否成功消耗能量
     */
    protected boolean takeCharge(@Nonnull Location location) {
        if (!isChargeable()) {
            return true;
        }
        int charge = getCharge(location);
        if (charge < energyConsumption) {
            plugin.getLogger().info("能量不足: 可用=" + charge + ", 需求=" + energyConsumption + ", 位置=" + location);
            return false;
        }
        setCharge(location, charge - energyConsumption);
        return true;
    }

    /**
     * 获取能量网络组件类型。
     *
     * @return 能量消费者类型
     */
    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    /**
     * 获取能量容量。
     *
     * @return 能量容量（能量消耗的两倍）
     */
    @Override
    public int getCapacity() {
        return energyConsumption * 2;
    }

    /**
     * 检查是否可充电。
     *
     * @return 总是返回 true，表示可充电
     */
    @Override
    public boolean isChargeable() {
        return true;
    }

    /**
     * 获取指定位置的当前能量。
     *
     * @param l 位置
     * @return 当前能量值
     */
    @Override
    public int getCharge(@Nonnull Location l) {
        String charge = BlockStorage.getLocationInfo(l, "energy");
        try {
            int value = charge != null ? Integer.parseInt(charge) : 0;
            plugin.getLogger().info("读取能量: 位置=" + l + ", 值=" + value);
            return value;
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("能量格式错误: 值=" + charge + ", 位置=" + l);
            return 0;
        }
    }

    /**
     * 设置指定位置的能量值。
     *
     * @param l      位置
     * @param charge 能量值
     */
    @Override
    public void setCharge(@Nonnull Location l, int charge) {
        if (charge < 0) {
            charge = 0;
        }
        BlockStorage.addBlockInfo(l, "energy", String.valueOf(charge));
        plugin.getLogger().info("设置能量: 位置=" + l + ", 值=" + charge);
    }

    /**
     * 设置 GUI 预设的初始布局，确保 27 格并填充灰色玻璃板。
     */
    protected void setupMenuPreset() {
        if (preset == null) return; // 防止 preset 为 null 时执行
        for (int i = 0; i < 27; i++) {
            int finalI = i;
            if (i == INPUT_SLOT || i == STATUS_SLOT || Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == finalI)) {
                continue;
            }
            preset.addItem(i, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " "), ChestMenuUtils.getEmptyClickHandler());
            plugin.getLogger().info("设置背景槽位: ID=" + getId() + ", 槽位=" + i); // 调试背景填充
        }
        preset.addMenuClickHandler(INPUT_SLOT, (p, slot, item, action) -> true); // 允许输入槽交互
        for (int slot : OUTPUT_SLOTS) {
            preset.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler()); // 输出槽交互由事件监听器控制
        }
        preset.addMenuClickHandler(STATUS_SLOT, ChestMenuUtils.getEmptyClickHandler()); // 禁止状态槽交互
        plugin.getLogger().info("GUI 预设初始化完成: ID=" + getId() + ", 槽位=27");
    }

    /**
     * 初始化新创建的菜单，并确保背景填充。
     *
     * @param menu 菜单
     */
    public void newItemMenu(BlockMenu menu) {
        // 填充背景槽位
        for (int i = 0; i < 27; i++) {
            int finalI = i;
            if (i == INPUT_SLOT || i == STATUS_SLOT || Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == finalI)) {
                continue;
            }
            ItemStack existing = menu.getItemInSlot(i);
            if (existing == null || existing.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                menu.replaceExistingItem(i, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " "));
                menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
                plugin.getLogger().info("初始化背景槽位: 位置=" + menu.getLocation() + ", 槽位=" + i);
            }
        }

        // 初始化功能槽位
        menu.replaceExistingItem(INPUT_SLOT, null);
        menu.addMenuClickHandler(INPUT_SLOT, (p, slot, item, action) -> true);
        ItemStack status = new CustomItemStack(Material.REDSTONE, "§c状态: 待机");
        menu.replaceExistingItem(STATUS_SLOT, status);
        menu.addMenuClickHandler(STATUS_SLOT, ChestMenuUtils.getEmptyClickHandler());
        for (int slot : OUTPUT_SLOTS) {
            menu.replaceExistingItem(slot, null); // 确保输出槽初始化为空
        }
        plugin.getLogger().info("菜单功能槽位初始化: 位置=" + menu.getLocation());
    }

    /**
     * 获取物品的 Slimefun ID 或材质名称。
     *
     * @param item 物品
     * @return 物品的 ID 或材质名称
     */
    protected String getInputId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String slimefunId = pdc.get(SLIMEFUN_KEY, PersistentDataType.STRING);
        return slimefunId != null ? slimefunId : item.getType().name();
    }

    /**
     * 获取物品的输出 ID，检查是否为有效输出物品。
     *
     * @param item 物品
     * @return 输出物品的 Slimefun ID，或 null（如果不是有效输出）
     */
    protected String getOutputId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String slimefunId = pdc.get(SLIMEFUN_KEY, PersistentDataType.STRING);
        return slimefunId != null && slimefunId.equals(outputId) ? slimefunId : null;
    }

    /**
     * 将物品返回给玩家背包，若背包满则掉落在玩家位置。
     *
     * @param player 玩家
     * @param item   物品
     */
    private void returnItem(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> unadded = player.getInventory().addItem(item);
        if (!unadded.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }

    /**
     * 处理 GUI 点击事件，控制输出槽交互。
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BlockMenu menu) || !menu.getPreset().getID().equals(getId())) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        int rawSlot = event.getRawSlot();
        org.bukkit.event.inventory.ClickType click = event.getClick();
        Inventory clickedInventory = event.getClickedInventory();
        boolean isBlockMenu = clickedInventory != null && clickedInventory.equals(menu.toInventory());
        boolean isPlayerInventory = clickedInventory != null && clickedInventory.equals(player.getInventory());

        // 处理从玩家背包 Shift+左键 转移物品
        if (isPlayerInventory && click == ClickType.SHIFT_LEFT && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            ItemStack current = event.getCurrentItem();
            if (current != null && current.getType() != Material.AIR) {
                ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);
                boolean canInsertInput = inputItem == null || inputItem.getType() == Material.AIR ||
                        (inputItem.isSimilar(current) && inputItem.getAmount() < inputItem.getMaxStackSize());

                if (!canInsertInput) {
                    event.setCancelled(true);
                    player.sendMessage("§c输入槽已满或不可存入！");
                    plugin.getLogger().info("阻止 Shift+左键: 输入槽不可用, 物品=" + current.getType() + ", 槽位=" + slot);
                    return;
                }
            }
        }

        // 输入槽允许存入和取出
        if (rawSlot == INPUT_SLOT && isBlockMenu) {
            return;
        }

        // 输出槽只允许特定方式取出，阻止存入
        if (Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == rawSlot) && isBlockMenu) {
            ItemStack cursor = event.getCursor();
            boolean isInsert = (cursor != null && cursor.getType() != Material.AIR) ||
                    (click == ClickType.SHIFT_LEFT && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) ||
                    click.isKeyboardClick();

            if (isInsert) {
                event.setCancelled(true);
                ItemStack item = cursor != null && cursor.getType() != Material.AIR ? cursor :
                        click.isKeyboardClick() ? player.getInventory().getItem(event.getHotbarButton()) : null;

                if (item != null && item.getType() != Material.AIR) {
                    returnItem(player, item.clone());
                    if (cursor != null && cursor.getType() != Material.AIR) {
                        player.setItemOnCursor(null);
                    } else if (click.isKeyboardClick()) {
                        player.getInventory().setItem(event.getHotbarButton(), null);
                    }
                    player.sendMessage("§c输出槽不支持存入！");
                    plugin.getLogger().info("阻止存入输出槽: 物品=" + item.getType() + ", 槽位=" + rawSlot);
                }

                // 延迟检查输出槽中的非法物品
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    for (int outputSlot : OUTPUT_SLOTS) {
                        ItemStack outputItem = menu.getItemInSlot(outputSlot);
                        if (outputItem != null && outputItem.getType() != Material.AIR && getOutputId(outputItem) == null) {
                            returnItem(player, outputItem.clone());
                            menu.replaceExistingItem(outputSlot, null);
                            player.sendMessage("§c输出槽不支持存入！");
                            plugin.getLogger().info("延迟检查移除非法物品: 输出槽=" + outputSlot + ", 物品=" + outputItem.getType());
                        }
                    }
                }, 2L);
            } else if (click != ClickType.LEFT && click != ClickType.SHIFT_LEFT &&
                    click != ClickType.NUMBER_KEY && click != ClickType.DROP) {
                event.setCancelled(true);
                player.sendMessage("§c输出槽只能左键、Shift+左键、数字键或丢弃键取出！");
            }
        }
    }

    /**
     * 处理 GUI 拖拽事件，控制物品在槽位间的拖拽行为。
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof BlockMenu menu) || !menu.getPreset().getID().equals(getId())) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        ItemStack cursor = event.getOldCursor();

        // 输入槽允许拖拽存入
        if (event.getRawSlots().contains(INPUT_SLOT)) {
            return;
        }

        // 输出槽阻止拖拽存入
        if (event.getRawSlots().stream().anyMatch(slot -> Arrays.stream(OUTPUT_SLOTS).anyMatch(s -> s == slot))) {
            event.setCancelled(true);
            if (cursor != null && cursor.getType() != Material.AIR) {
                returnItem(player, cursor.clone());
                event.setCursor(null);
                player.sendMessage("§c输出槽不支持存入！");
                plugin.getLogger().info("阻止拖拽存入输出槽: 物品=" + cursor.getType());
            }
        }
    }
}