package io.github.lifeflower;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class LifeFlowerStore {
    private final LifeFlowerPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    public LifeFlowerStore(LifeFlowerPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "flowers.yml");
    }

    public void load() {
        if (!file.exists()) {
            config = new YamlConfiguration();
            return;
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public Map<UUID, LifeFlower> loadAll() {
        Map<UUID, LifeFlower> flowers = new HashMap<>();
        if (config == null) return flowers;

        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section == null) continue;

                LifeFlower flower = new LifeFlower(uuid);
                flower.setPlanted(section.getBoolean("planted"));
                flower.setValid(section.getBoolean("valid"));
                flower.setAwaitingRevivalItem(section.getBoolean("awaitingRevivalItem", false));
                flower.setEliminated(section.getBoolean("eliminated", false));
                String flowerUuidStr = section.getString("flowerUuid");
                if (flowerUuidStr != null) {
                    flower.setFlowerUuid(UUID.fromString(flowerUuidStr));
                }
                flower.setLocation(section.getLocation("location"));

                flowers.put(uuid, flower);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load LifeFlower for UUID: " + key, e);
            }
        }
        return flowers;
    }

    public void save(Map<UUID, LifeFlower> flowers) {
        config = new YamlConfiguration();
        for (Map.Entry<UUID, LifeFlower> entry : flowers.entrySet()) {
            UUID uuid = entry.getKey();
            LifeFlower flower = entry.getValue();
            ConfigurationSection section = config.createSection(uuid.toString());
            section.set("planted", flower.isPlanted());
            section.set("valid", flower.isValid());
            section.set("awaitingRevivalItem", flower.isAwaitingRevivalItem());
            section.set("eliminated", flower.isEliminated());
            section.set("flowerUuid", flower.getFlowerUuid().toString());
            section.set("location", flower.getLocation());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save flowers to " + file, e);
        }
    }
}
