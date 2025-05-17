package me.xingchen.XingchenExpansion.ability;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.Particle;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class WeavingBuff {
    private final JavaPlugin plugin;
    private final BuffManager buffManager;
    private final double radius = 7.0; // 7 格半径
    private final int slownessDuration = 5 * 20; // 5 秒
    private final int slownessLevel = 10; // 缓慢 X
    private final long interval = 10 * 20; // 15 秒

    public WeavingBuff(JavaPlugin plugin, BuffManager buffManager) {
        this.plugin = plugin;
        this.buffManager = buffManager;
        startWeavingTask();
    }

    private void startWeavingTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (buffManager.hasBuff(player, "WEAVING")) {
                        applyWeavingEffect(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, interval); // 每 15 秒触发
    }

    private void applyWeavingEffect(Player player) {
        Location location = player.getLocation();
        for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (entity instanceof Monster && entity != player) {
                ((Monster) entity).addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOWNESS,
                        slownessDuration,
                        slownessLevel - 1, // 等级从 0 开始，10 表示 X
                        false,
                        true
                ));
            }
        }
        location.getWorld().spawnParticle(
                Particle.CLOUD, // 中性烟雾粒子
                location.add(0, 1, 0), // 玩家腰部
                50, // 50 个粒子，增强可见性
                radius / 2, radius / 2, radius / 2, // 扩散 3.5 格
                0.2 // 速度 0.2，模拟炸开
        );
        location.getWorld().playSound(
                location,
                Sound.BLOCK_COBWEB_PLACE, // 蛛网放置音效
                0.5F,
                1.0F
        );
    }
}