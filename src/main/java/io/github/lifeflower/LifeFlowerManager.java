package io.github.lifeflower;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
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
        // 1. Remove from world (Block)
        LifeFlower flower = flowers.get(ownerUuid);
        if (flower != null && flower.isPlanted() && flower.getLocation() != null) {
            Block block = flower.getLocation().getBlock();
            if (LifeFlowerUtils.isFlowerMaterial(block.getType(), plugin)) {
                block.setType(Material.AIR);
            }
        }

        // 2. Search online players
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            removeFlowerFromInventory(player.getInventory(), ownerUuid);
            removeFlowerFromInventory(player.getEnderChest(), ownerUuid);
        }

        // 3. Search loaded dropped items
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item itemEntity) {
                    if (isOwnerOf(itemEntity.getItemStack(), ownerUuid)) {
                        entity.remove();
                    }
                }
                // 4. Search loaded containers (Blocks)
                // Note: Getting all tile entities in a world can be expensive, but here we only search loaded chunks.
            }

            for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                for (BlockState state : chunk.getTileEntities()) {
                    if (state instanceof Container container) {
                        removeFlowerFromInventory(container.getInventory(), ownerUuid);
                    }
                }
            }
        }

        flowers.remove(ownerUuid);
        save();
    }

    private void removeFlowerFromInventory(Inventory inv, UUID ownerUuid) {
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;

            if (isOwnerOf(item, ownerUuid)) {
                inv.setItem(i, null);
            } else if (item.getItemMeta() instanceof BlockStateMeta bsm) {
                // Search inside nested containers (Shulker Boxes)
                if (bsm.getBlockState() instanceof Container container) {
                    removeFlowerFromInventory(container.getInventory(), ownerUuid);
                    bsm.setBlockState(container);
                    item.setItemMeta(bsm);
                }
            }
        }
    }

    private boolean isOwnerOf(ItemStack item, UUID ownerUuid) {
        UUID actual = LifeFlowerUtils.getOwnerUuid(item);
        return ownerUuid.equals(actual);
    }

    public void plantFlower(UUID ownerUuid, Location location) {
        LifeFlower flower = flowers.get(ownerUuid);
        if (flower == null) return;

        flower.setLocation(location);
        flower.setPlanted(true);
        flower.setValid(isValidationEnabled() ? validateSurface(location) : true);
        save();
    }

    public void pickupFlower(UUID ownerUuid) {
        LifeFlower flower = flowers.get(ownerUuid);
        if (flower == null) return;

        flower.setLocation(null);
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
