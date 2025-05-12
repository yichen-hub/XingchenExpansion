package me.xingchen.XingchenExpansion;


import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ConfigManager {
    /*private final FileConfiguration config;
    private final FileConfiguration generatorConfig;
    private final Map<String, GeneratorConfig> generators = new HashMap<>();

    public static class GeneratorConfig {
        public final int energyCapacity;
        public final Map<String, FuelConfig> fuels;

        public GeneratorConfig(int energyCapacity, Map<String, FuelConfig> fuels) {
            this.energyCapacity = energyCapacity;
            this.fuels = fuels;
        }
    }

    public static class FuelConfig {
        public final int energyPerTick;
        public final Map<String, Integer> wasteItems;

        public FuelConfig(int energyPerTick, Map<String, Integer> wasteItems) {
            this.energyPerTick = energyPerTick;
            this.wasteItems = wasteItems;
        }
    }

    public ConfigManager(XingchenExpansion plugin) {
        this.config = plugin.getConfig();
        this.generatorConfig = loadGeneratorConfig();
        loadConfig();
    }

    private FileConfiguration loadGeneratorConfig() {
        File file = new File(XingchenExpansion.instance.getDataFolder(), "generator.yml");
        if (!file.exists()) {
            XingchenExpansion.instance.saveResource("generator.yml", false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void loadConfig() {
        XingchenExpansion.instance.saveDefaultConfig();

        ConfigurationSection generatorsSection = generatorConfig.getConfigurationSection("generators");
        if (generatorsSection != null) {
            for (String generatorId : generatorsSection.getKeys(false)) {
                int energyCapacity = generatorsSection.getInt(generatorId + ".energy_capacity", 1000);

                Map<String, FuelConfig> fuels = new HashMap<>();
                ConfigurationSection fuelsSection = generatorsSection.getConfigurationSection(generatorId + ".fuels");
                if (fuelsSection != null) {
                    for (String fuelId : fuelsSection.getKeys(false)) {
                        int energy = fuelsSection.getInt(fuelId + ".energy", 0);
                        Map<String, Integer> wasteItems = new HashMap<>();
                        ConfigurationSection wasteSection = fuelsSection.getConfigurationSection(fuelId + ".waste");
                        if (wasteSection != null) {
                            for (String wasteId : wasteSection.getKeys(false)) {
                                int amount = wasteSection.getInt(wasteId, 0);
                                if (amount > 0) {
                                    wasteItems.put(wasteId, amount);
                                }
                            }
                        }
                        if (energy > 0) {
                            fuels.put(fuelId, new FuelConfig(energy, wasteItems));
                        }
                    }
                }

                generators.put(generatorId, new GeneratorConfig(energyCapacity, fuels));
            }
        }

        XingchenExpansion.instance.getLogger().info("Config loaded: generators=" + generators.keySet());
    }

    public GeneratorConfig getGeneratorConfig(String generatorId) {
        return generators.get(generatorId);
    }*/
}