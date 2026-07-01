package io.github.lifeflower;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Iterator;

public class FlowerProtectionListener implements Listener {
    private final LifeFlowerManager manager;

    public FlowerProtectionListener(LifeFlowerManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (isLifeFlowerLocation(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (isLifeFlowerLocation(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            if (isLifeFlowerLocation(it.next())) {
                it.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosionDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof ItemDisplay display && isLifeFlowerEntity(display)) {
            if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        if (LifeFlowerUtils.isLifeFlower(event.getEntity().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            if (isLifeFlowerLocation(it.next())) {
                it.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        if (isLifeFlowerLocation(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (isLifeFlowerLocation(event.getToBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (isLifeFlowerLocation(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (isLifeFlowerLocation(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Item item) {
            if (LifeFlowerUtils.isLifeFlower(item.getItemStack())) {
                event.setCancelled(true);
            }
        } else if (event.getEntity() instanceof ItemDisplay display) {
            if (isLifeFlowerEntity(display)) {
                // EntityInteractionListener handles player damage.
                // Here we cancel all other damage.
                if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isLifeFlower(Block block) {
        return manager.getFlowerAt(block.getLocation()) != null;
    }

    private boolean isLifeFlowerLocation(Block block) {
        for (LifeFlower flower : manager.getFlowers().values()) {
            if (flower.isPlanted() && flower.getLocation() != null && flower.getLocation().getBlock().equals(block)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLifeFlowerEntity(Entity entity) {
        return entity.getPersistentDataContainer().has(LifeFlowerUtils.getOwnerKey(), PersistentDataType.STRING);
    }
}
