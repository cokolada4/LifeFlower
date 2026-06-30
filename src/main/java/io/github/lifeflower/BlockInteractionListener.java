package io.github.lifeflower;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class BlockInteractionListener implements Listener {
    private final LifeFlowerPlugin plugin;
    private final LifeFlowerManager manager;

    public BlockInteractionListener(LifeFlowerPlugin plugin, LifeFlowerManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        UUID ownerUuid = LifeFlowerUtils.getOwnerUuid(item);

        if (ownerUuid != null) {
            manager.plantFlower(ownerUuid, event.getBlock().getLocation());
            Player player = event.getPlayer();
            if (manager.getFlower(ownerUuid).isValid()) {
                player.sendMessage("LifeFlower planted securely on the surface.");
            } else {
                player.sendMessage("Warning: Your LifeFlower is NOT on the surface. It will not serve as an anchor!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.WITHER_ROSE) {
            LifeFlower flower = manager.getFlowerAt(block.getLocation());
            if (flower != null) {
                event.setDropItems(false);
                manager.pickupFlower(flower.getOwnerUniqueId());

                ItemStack flowerItem = LifeFlowerUtils.createLifeFlowerItem(plugin, flower.getOwnerUniqueId(),
                        plugin.getServer().getOfflinePlayer(flower.getOwnerUniqueId()).getName());

                block.getWorld().dropItemNaturally(block.getLocation(), flowerItem);

                Player player = event.getPlayer();
                if (player.getUniqueId().equals(flower.getOwnerUniqueId())) {
                    player.sendMessage("You picked up your LifeFlower. You are now in Hardcore mode until you replant it!");
                } else {
                    player.sendMessage("You picked up " + plugin.getServer().getOfflinePlayer(flower.getOwnerUniqueId()).getName() + "'s LifeFlower!");
                }
            }
        }
    }
}
