package io.github.lifeflower;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.entity.Item;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;

import java.util.Iterator;

public class FlowerProtectionListener implements Listener {
    private final LifeFlowerManager manager;

    public FlowerProtectionListener(LifeFlowerManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (!manager.getPlugin().isPluginEnabled()) return;
        if (isLifeFlower(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (!manager.getPlugin().isPluginEnabled()) return;
        if (isLifeFlower(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!manager.getPlugin().isPluginEnabled()) return;
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            if (isLifeFlower(it.next())) {
                it.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        if (!manager.getPlugin().isPluginEnabled()) return;
        if (LifeFlowerUtils.isLifeFlower(event.getEntity().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!manager.getPlugin().isPluginEnabled()) return;
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            if (isLifeFlower(it.next())) {
                it.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        if (!manager.getPlugin().isPluginEnabled()) return;
        if (isLifeFlower(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!manager.getPlugin().isPluginEnabled()) return;
        if (isLifeFlower(event.getToBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (!manager.getPlugin().isPluginEnabled()) return;
        for (Block block : event.getBlocks()) {
            if (isLifeFlower(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (!manager.getPlugin().isPluginEnabled()) return;
        for (Block block : event.getBlocks()) {
            if (isLifeFlower(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!manager.getPlugin().isPluginEnabled()) return;
        if (event.getEntity() instanceof Item item) {
            if (LifeFlowerUtils.isLifeFlower(item.getItemStack())) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isLifeFlower(Block block) {
        if (!LifeFlowerUtils.isFlowerMaterial(block.getType(), manager.getPlugin())) return false;
        return manager.getFlowerAt(block.getLocation()) != null;
    }
}
