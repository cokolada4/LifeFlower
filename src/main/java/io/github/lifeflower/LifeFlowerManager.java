package io.github.lifeflower;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LifeFlowerManager {
    private final LifeFlowerPlugin plugin;

    public LifeFlowerPlugin getPlugin() {
        return plugin;
    }

    private final LifeFlowerStore store;
    private final Map<UUID, LifeFlower> flowers;

    public LifeFlowerManager(LifeFlowerPlugin plugin, LifeFlowerStore store) {
        this.plugin = plugin;
        this.store = store;
        this.flowers = new HashMap<>();
    }

    public Map<UUID, LifeFlower> getFlowers() {
        return flowers;
    }

    public void load() {
        store.load();
        flowers.clear();
        flowers.putAll(store.loadAll());
    }

    public void save() {
        store.save(flowers);
    }

    public LifeFlower getFlower(UUID ownerUuid) {
        return flowers.get(ownerUuid);
    }

    public LifeFlower createFlower(UUID ownerUuid) {
        if (flowers.containsKey(ownerUuid)) {
            return flowers.get(ownerUuid);
        }
        LifeFlower flower = new LifeFlower(ownerUuid);
        flowers.put(ownerUuid, flower);
        save();
        return flower;
    }

    public void resetFlower(UUID ownerUuid) {
        flowers.remove(ownerUuid);
        save();
    }

    public void plantFlower(UUID ownerUuid, Location location, UUID entityUuid) {
        LifeFlower flower = flowers.get(ownerUuid);
        if (flower == null) return;

        flower.setLocation(location);
        flower.setEntityUuid(entityUuid);
        flower.setPlanted(true);
        flower.setValid(isValidationEnabled() ? validateSurface(location) : true);
        save();
    }

    public void pickupFlower(UUID ownerUuid) {
        LifeFlower flower = flowers.get(ownerUuid);
        if (flower == null) return;

        flower.setLocation(null);
        flower.setEntityUuid(null);
        flower.setPlanted(false);
        flower.setValid(false);
        save();
    }

    public boolean validateSurface(Location location) {
        if (!isValidationEnabled()) return true;
        if (location == null) return false;
        World world = location.getWorld();
        if (world == null) return false;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        for (int i = y + 1; i <= world.getMaxHeight(); i++) {
            Block block = world.getBlockAt(x, i, z);
            if (isOccluding(block)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidationEnabled() {
        return plugin.getConfig().getBoolean("require-sky-access", true);
    }

    protected boolean isOccluding(Block block) {
        return block.getType().isOccluding();
    }

    public LifeFlower getFlowerAt(Location location) {
        for (LifeFlower flower : flowers.values()) {
            if (flower.isPlanted() && flower.getLocation() != null && flower.getLocation().equals(location)) {
                return flower;
            }
        }
        return null;
    }

    public void updateAllValidations() {
        boolean changed = false;
        for (LifeFlower flower : flowers.values()) {
            if (flower.isPlanted() && flower.getLocation() != null) {
                boolean nowValid = validateSurface(flower.getLocation());
                if (nowValid != flower.isValid()) {
                    flower.setValid(nowValid);
                    changed = true;
                }
            }
        }
        if (changed) {
            save();
        }
    }
}
