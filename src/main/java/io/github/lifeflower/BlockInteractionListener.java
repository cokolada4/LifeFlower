package io.github.lifeflower;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.UUID;

public class BlockInteractionListener implements Listener {
    private final LifeFlowerPlugin plugin;
    private final LifeFlowerManager manager;

    public BlockInteractionListener(LifeFlowerPlugin plugin, LifeFlowerManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (LifeFlowerUtils.isLifeFlower(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType().isInteractable() && !event.getPlayer().isSneaking()) return;

        ItemStack item = event.getItem();
        UUID ownerUuid = LifeFlowerUtils.getOwnerUuid(item);
        if (ownerUuid == null) return;

        Block clickedBlock = event.getClickedBlock();
        Block placeBlock = clickedBlock.getRelative(event.getBlockFace());

        if (!placeBlock.getType().isAir()) return;

        // Check if player has permission to build here (respects WorldGuard, Spawn protection, etc.)
        BlockBreakEvent breakEvent = new BlockBreakEvent(placeBlock, event.getPlayer());
        Bukkit.getPluginManager().callEvent(breakEvent);
        if (breakEvent.isCancelled()) return;

        event.setCancelled(true);

        Location spawnLoc = placeBlock.getLocation().add(0.5, 0.5, 0.5);
        ItemDisplay display = placeBlock.getWorld().spawn(spawnLoc, ItemDisplay.class, ent -> {
            ent.setItemStack(item.asQuantity(1));
            ent.setBillboard(ItemDisplay.Billboard.FIXED);

            Transformation trans = ent.getTransformation();
            trans.getScale().set(1.5f, 1.5f, 1.5f);
            ent.setTransformation(trans);

            ent.getPersistentDataContainer().set(LifeFlowerUtils.getOwnerKey(), org.bukkit.persistence.PersistentDataType.STRING, ownerUuid.toString());
        });

        manager.plantFlower(ownerUuid, placeBlock.getLocation(), display.getUniqueId());

        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }

        if (manager.getFlower(ownerUuid).isValid()) {
            player.sendMessage("LifeFlower planted securely on the surface.");
        } else {
            player.sendMessage("Warning: Your LifeFlower is NOT on the surface. It will not serve as an anchor!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        LifeFlower flower = manager.getFlowerAt(block.getLocation());
        if (flower != null) {
            event.setDropItems(false);
            handleFlowerBreak(flower, event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();

        // Handle ItemDisplay LifeFlowers when block below is removed
        for (LifeFlower flower : manager.getFlowers().values()) {
            if (flower.isPlanted() && flower.getLocation() != null && flower.getLocation().getBlock().equals(block)) {
                if (block.getRelative(0, -1, 0).getType().isAir()) {
                    handleFlowerBreak(flower, null);
                }
            }
        }
    }

    private void handleFlowerBreak(LifeFlower flower, Player breaker) {
        if (flower != null) {
            UUID ownerUuid = flower.getOwnerUniqueId();
            Location loc = flower.getLocation();

            if (flower.getEntityUuid() != null) {
                org.bukkit.entity.Entity entity = plugin.getServer().getEntity(flower.getEntityUuid());
                if (entity != null) entity.remove();
            }

            manager.pickupFlower(ownerUuid);

            ItemStack flowerItem = LifeFlowerUtils.createLifeFlowerItem(plugin, ownerUuid,
                    plugin.getServer().getOfflinePlayer(ownerUuid).getName());

            if (loc != null) {
                loc.getWorld().dropItemNaturally(loc, flowerItem);
            }

            if (breaker != null) {
                if (breaker.getUniqueId().equals(ownerUuid)) {
                    breaker.sendMessage("You picked up your LifeFlower. You are now in Hardcore mode until you replant it!");
                } else {
                    breaker.sendMessage("You picked up " + plugin.getServer().getOfflinePlayer(ownerUuid).getName() + "'s LifeFlower!");
                }
            }
        }
    }
}
