package me.xingchen.XingchenExpansion.ability.buff;

import me.xingchen.XingchenExpansion.ability.BuffManager;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ManifoldBuff implements Listener {
    private final JavaPlugin plugin;
    private final BuffManager buffManager;
    private final Map<UUID, BukkitRunnable> dashTasks = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Random random = new Random();
    private final double startSpeed = 0.2; // 起始速度
    private final double endSpeed = 1.7; // 最大速度
    private final double maxSpeedTime = 0.6; // 最大速度前摇
    private final double maxDuration = 1.2; // 最长冲刺时间

    public ManifoldBuff(JavaPlugin plugin, BuffManager buffManager) {
        this.plugin = plugin;
        this.buffManager = buffManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (event.isSneaking()) {
            tryStartDash(player);
        } else {
            stopDash(player);
        }
    }

    public void tryStartDash(Player player) {
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid) && currentTime - cooldowns.get(uuid) < 500) {
            return;
        }
        Vector direction = player.getLocation().getDirection();
        boolean isMovingForward = direction.getX() != 0 || direction.getZ() != 0;
        if (buffManager.hasBuff(player, "MANIFOLD") &&
                (player.getVelocity().getY() > 0 || !player.isOnGround()) &&
                player.getLocation().subtract(0, 0.2, 0).getBlock().getType().isAir() && // 脚下 0.2 格空气
                isMovingForward &&
                !dashTasks.containsKey(uuid)) {
            startDash(player);
        }
    }

    public void stopDash(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitRunnable task = dashTasks.remove(uuid);
        if (task != null) {
            task.cancel();
            player.setVelocity(new Vector(0, 0, 0));
            player.setFallDistance(0);
            player.setInvisible(false);
        }
    }

    private void startDash(Player player) {
        UUID uuid = player.getUniqueId();
        cooldowns.put(uuid, System.currentTimeMillis());
        player.setInvisible(true);
        BukkitRunnable dashTask = new BukkitRunnable() {
            private double elapsed = 0;
            @Override
            public void run() {
                elapsed += 0.05;
                if (elapsed >= maxDuration || !player.isSneaking() || player.isOnGround() || isColliding(player)) {
                    stopDash(player);
                    return;
                }
                player.setInvisible(true);
                spawnTrailParticles(player);
                // 计算速度
                double speed;
                if (elapsed <= maxSpeedTime) {
                    double t = elapsed / maxSpeedTime;
                    speed = startSpeed + (endSpeed - startSpeed) * t;
                } else {
                    speed = endSpeed;
                }
                Vector direction = player.getLocation().getDirection().normalize().multiply(speed);
                direction.setY(0);
                player.setVelocity(direction);
                player.setFallDistance(0);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5F, 1.0F);
            }
        };
        dashTasks.put(uuid, dashTask);
        dashTask.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnTrailParticles(Player player) {
        Location loc = player.getEyeLocation();
        Vector direction = player.getLocation().getDirection().normalize();
        Location particleLoc = loc.clone()
                .subtract(direction.multiply(0.5 + random.nextDouble() * 0.5))
                .add(new Vector(
                        (random.nextDouble() - 0.5) * 0.4,
                        (random.nextDouble() - 0.5) * 0.4,
                        (random.nextDouble() - 0.5) * 0.4
                ));
        Particle.DustOptions dustOptions = random.nextBoolean()
                ? new Particle.DustOptions(Color.fromRGB(128, 128, 128), 1.5F)
                : new Particle.DustOptions(Color.fromRGB(192, 192, 192), 1.5F);
        player.getWorld().spawnParticle(
                Particle.DUST,
                particleLoc,
                8,
                0.3, 0.3, 0.3,
                0.15,
                dustOptions
        );
    }

    private boolean isColliding(Player player) {
        Location loc = player.getLocation();
        Vector direction = loc.getDirection().normalize();
        Block block = loc.add(direction.multiply(1)).getBlock();
        return !block.isPassable();
    }
}