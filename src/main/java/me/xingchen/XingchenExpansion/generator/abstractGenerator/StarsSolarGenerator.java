package me.xingchen.XingchenExpansion.generator.abstractGenerator;

import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import me.xingchen.XingchenExpansion.XingchenExpansion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Arrays;

public abstract class StarsSolarGenerator extends SlimefunItem implements EnergyNetProvider, Listener {
    protected static final int STATUS_SLOT = 13;
    protected static final ItemStack BACKGROUND_ITEM = new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "&7 ");
    protected final int dayEnergy;
    protected final int nightEnergy;
    protected final int energyCapacity;
    protected final String generatorId;
    protected final XingchenExpansion plugin;
    protected BlockMenuPreset preset;

    public StarsSolarGenerator(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, XingchenExpansion plugin, String generatorId) {
        super(itemGroup, item, recipeType, recipe);
        this.plugin = plugin;
        this.generatorId = generatorId;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        File configFile = new File(plugin.getDataFolder(), "solar_generator.yml");
        if (!configFile.exists()) {plugin.saveResource("solar_generator.yml", false);}
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        var section = config.getConfigurationSection("generators." + generatorId);
        if (section == null) {
            plugin.getLogger().severe("未找到 generators." + generatorId + " 的配置，发电机将被禁用！");
            this.energyCapacity = 0;
            this.dayEnergy = 0;
            this.nightEnergy = 0;
            disable();
            return;
        }
        this.energyCapacity = section.getInt("energy_capacity");
        this.dayEnergy = section.getInt("day_energy");
        this.nightEnergy = section.getInt("night_energy");

        if (energyCapacity <= 0 || dayEnergy <= 0 || nightEnergy <= 0) {
            plugin.getLogger().severe("无效的发电量配置，发电机禁用！ID: " + generatorId);
            disable();
            return;
        }

        setupMenuPreset();
        addItemHandler(
                new BlockUseHandler() {
                    @Override
                    public void onRightClick(PlayerRightClickEvent event) {
                        if (event.getPlayer().isSneaking()) return;
                        event.cancel();
                        BlockMenu menu = BlockStorage.getInventory(event.getClickedBlock().get());
                        if (menu != null) {
                            updateStatus(menu, event.getClickedBlock().get().getLocation());
                            event.getPlayer().openInventory(menu.getInventory());
                        }
                    }
                }
        );
    }

    protected void setupMenuPreset() {
        preset = new BlockMenuPreset(generatorId, "星辰太阳能发电机") {
            @Override
            public void init() {
                for (int i = 0; i < 27; i++) {
                    if (i != STATUS_SLOT) {
                        addItem(i, BACKGROUND_ITEM, (p, s, item, action) -> false);
                    }
                }
                addMenuClickHandler(STATUS_SLOT, (p, s, item, action) -> false);
            }

            @Override
            public boolean canOpen(Block b, Player p) {
                return true;
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                return new int[0]; // 太阳能发电机无输入/输出槽
            }
        };
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRightClick(PlayerRightClickEvent event) {
        if (event.getClickedBlock().isEmpty()) return;
        Block block = event.getClickedBlock().get();
        String blockId = BlockStorage.getLocationInfo(block.getLocation(), "id");
        if (!generatorId.equals(blockId)) return;

        if (event.getPlayer().isSneaking()) return;
        event.cancel();
        BlockMenu menu = BlockStorage.getInventory(block);
        if (menu == null) return;
        updateStatus(menu, block.getLocation());
        event.getPlayer().openInventory(menu.getInventory());
    }

    protected void updateStatus(BlockMenu menu, Location location) {
        long time = location.getWorld().getTime();
        boolean isDay = time >= 0 && time < 12000;
        int energy = isDay ? dayEnergy : nightEnergy;
        String statusText = isDay ? "&e白天" : "&8夜晚";

        ItemStack status = new ItemStack(Material.REDSTONE);
        ItemMeta meta = status.getItemMeta();
        meta.setDisplayName("§c能量: " + energy + " J/tick");
        meta.setLore(Arrays.asList("§7状态: " + statusText));
        status.setItemMeta(meta);
        menu.replaceExistingItem(STATUS_SLOT, status);
    }

    @Override
    public int getGeneratedOutput(@Nonnull Location location, @Nonnull Config config) {
        long time = location.getWorld().getTime();
        boolean isDay = time >= 0 && time < 12000;
        return isDay ? dayEnergy : nightEnergy;
    }

    @Override
    public int getCapacity() {
        return energyCapacity;
    }

    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.GENERATOR;
    }

    @Override
    public boolean isChargeable() {
        return true;
    }
}