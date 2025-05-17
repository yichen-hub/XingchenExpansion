package me.xingchen.XingchenExpansion.ability;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuffManager implements Listener {
    private final JavaPlugin plugin;
    private final Map<UUID, Map<String, Long>> playerBuffs = new HashMap<>();
    private final String dbPath;
    private boolean databaseInitialized = false;

    public BuffManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dbPath = plugin.getDataFolder().getAbsolutePath() + "/buffs.db";
        plugin.getLogger().info("Initializing BuffManager with dbPath: " + dbPath);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        try {
            initializeDatabase();
            if (databaseInitialized) {
                loadAllBuffs();
            } else {
                plugin.getLogger().warning("Database initialization failed, buffs will not be saved.");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize BuffManager: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS buffs (" +
                            "player_uuid TEXT, " +
                            "buff_id TEXT, " +
                            "expiry_time BIGINT, " +
                            "PRIMARY KEY (player_uuid, buff_id))"
            );
            databaseInitialized = true;
            plugin.getLogger().info("Database initialized at " + dbPath);
        }
    }

    public void addBuff(Player player, String buffId, long duration) {
        UUID uuid = player.getUniqueId();
        Map<String, Long> buffs = playerBuffs.computeIfAbsent(uuid, k -> new HashMap<>());
        if (buffs.containsKey(buffId)) {
            player.sendMessage("§c" + buffId + " 效果已在激活中！");
            return;
        }
        long expiry = duration == -1 ? -1L : System.currentTimeMillis() + duration * 50L;
        buffs.put(buffId, expiry);
        player.sendMessage("§a已激活 " + buffId + " 效果！");
        if (duration != -1) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    removeBuff(player, buffId);
                }
            }.runTaskLater(plugin, duration);
        }
    }

    public void removeBuff(Player player, String buffId) {
        UUID uuid = player.getUniqueId();
        Map<String, Long> buffs = playerBuffs.get(uuid);
        if (buffs != null && buffs.remove(buffId) != null) {
            player.sendMessage("§c" + buffId + " 效果已移除。");
            if (buffs.isEmpty()) {
                playerBuffs.remove(uuid);
            }
        }
    }

    public boolean hasBuff(Player player, String buffId) {
        Map<String, Long> buffs = playerBuffs.get(player.getUniqueId());
        if (buffs == null || !buffs.containsKey(buffId)) {
            return false;
        }
        Long expiry = buffs.get(buffId);
        if (expiry != -1 && System.currentTimeMillis() > expiry) {
            removeBuff(player, buffId);
            return false;
        }
        return true;
    }

    public void clearBuffs(Player player) {
        UUID uuid = player.getUniqueId();
        Map<String, Long> buffs = playerBuffs.remove(uuid);
        if (buffs != null) {
            buffs.keySet().forEach(buffId -> player.sendMessage("§c" + buffId + " 效果已移除。"));
        }
    }

    public void loadAllBuffs() {
        if (!databaseInitialized) {
            plugin.getLogger().warning("Database not initialized, skipping buff load.");
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
                    try (ResultSet rs = conn.createStatement().executeQuery("SELECT player_uuid, buff_id, expiry_time FROM buffs")) {
                        while (rs.next()) {
                            UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                            String buffId = rs.getString("buff_id");
                            long expiry = rs.getLong("expiry_time");
                            if (expiry == -1 || System.currentTimeMillis() < expiry) {
                                Map<String, Long> buffs = playerBuffs.computeIfAbsent(uuid, k -> new HashMap<>());
                                buffs.put(buffId, expiry);
                            }
                        }
                    }
                    conn.createStatement().execute("DELETE FROM buffs");
                    plugin.getLogger().info("Loaded buffs from database and cleared table.");
                } catch (SQLException e) {
                    plugin.getLogger().severe("Failed to load or clear buffs: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void saveAllBuffs() {
        if (!databaseInitialized) {
            plugin.getLogger().warning("Database not initialized, skipping buff save.");
            return;
        }
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            for (Map.Entry<UUID, Map<String, Long>> entry : playerBuffs.entrySet()) {
                UUID uuid = entry.getKey();
                for (Map.Entry<String, Long> buff : entry.getValue().entrySet()) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT OR REPLACE INTO buffs (player_uuid, buff_id, expiry_time) VALUES (?, ?, ?)")) {
                        stmt.setString(1, uuid.toString());
                        stmt.setString(2, buff.getKey());
                        stmt.setLong(3, buff.getValue());
                        stmt.executeUpdate();
                    }
                }
            }
            plugin.getLogger().info("Saved all buffs to database.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save all buffs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.MILK_BUCKET) {
            clearBuffs(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Map<String, Long> buffs = playerBuffs.get(uuid);
        if (buffs != null) {
            buffs.entrySet().removeIf(entry -> {
                long expiry = entry.getValue();
                if (expiry != -1 && System.currentTimeMillis() > expiry) {
                    player.sendMessage("§c" + entry.getKey() + " 效果已过期。");
                    return true;
                }
                return false;
            });
            if (buffs.isEmpty()) {
                playerBuffs.remove(uuid);
            }
        }
    }
}