package io.github.lifeflower;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleTask extends BukkitRunnable {
    private final LifeFlowerPlugin plugin;
    private final LifeFlowerManager manager;

    public ParticleTask(LifeFlowerPlugin plugin, LifeFlowerManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public void run() {
        if (!plugin.isPluginEnabled()) return;
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("particles.enabled", true)) return;

        String typeName = config.getString("particles.type", "CHERRY_LEAVES");
        Particle particle;
        try {
            particle = Particle.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return;
        }

        int count = config.getInt("particles.count", 3);
        double spreadX = config.getDouble("particles.spread-x", 0.3);
        double spreadY = config.getDouble("particles.spread-y", 0.5);
        double spreadZ = config.getDouble("particles.spread-z", 0.3);
        double speed = config.getDouble("particles.speed", 0.05);

        for (LifeFlower flower : manager.getFlowers().values()) {
            if (flower.isPlanted() && flower.getLocation() != null) {
                Location loc = flower.getLocation().clone().add(0.5, 0.5, 0.5);
                loc.getWorld().spawnParticle(particle, loc, count, spreadX, spreadY, spreadZ, speed);
            }
        }
    }
}
