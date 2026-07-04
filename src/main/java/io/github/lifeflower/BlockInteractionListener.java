package io.github.lifeflower;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
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
        if (!plugin.isPluginEnabled()) return;
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
        if (!plugin.isPluginEnabled()) return;
        Block block = event.getBlock();
        if (LifeFlowerUtils.isFlowerMaterial(block.getType(), plugin)) {
            LifeFlower flower = manager.getFlowerAt(block.getLocation());
            if (flower != null) {
                Player player = event.getPlayer();

                // If the player is NOT the owner, they MUST use the raid tool
                if (!player.getUniqueId().equals(flower.getOwnerUniqueId())) {
                    ItemStack tool = player.getInventory().getItemInMainHand();
                    if (!LifeFlowerUtils.isRaidTool(tool, plugin)) {
                        event.setCancelled(true);

                        String msg = plugin.getConfig().getString("raid-tool.denied-message");
                        if (msg != null && !msg.isEmpty()) {
                            player.sendMessage(MiniMessage.miniMessage().deserialize(msg));
                        }

                        String soundName = plugin.getConfig().getString("raid-tool.denied-sound");
                        if (soundName != null && !soundName.isEmpty()) {
                            try {
                                player.playSound(player.getLocation(), Sound.valueOf(soundName), 1.0f, 1.0f);
                            } catch (IllegalArgumentException ignored) {}
                        }
                        return;
                    } else {
                        // Consume durability
                        int durability = LifeFlowerUtils.getRaidToolDurability(tool);
                        durability--;
                        if (durability <= 0) {
                            tool.setAmount(0);
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                        } else {
                            LifeFlowerUtils.setRaidToolDurability(tool, durability, plugin);
                        }
                    }
                }

                event.setDropItems(false);
                handleFlowerBreak(block, player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (!plugin.isPluginEnabled()) return;
        Block block = event.getBlock();
        if (LifeFlowerUtils.isFlowerMaterial(block.getType(), plugin)) {
            // Check if the flower is still supported
            if (block.getRelative(0, -1, 0).getType().isAir()) {
                handleFlowerBreak(block, null);
                block.setType(Material.AIR);
            }
        }
    }

    private void handleFlowerBreak(Block block, Player breaker) {
        LifeFlower flower = manager.getFlowerAt(block.getLocation());
        if (flower != null) {
            manager.pickupFlower(flower.getOwnerUniqueId());

            ItemStack flowerItem = LifeFlowerUtils.createLifeFlowerItem(plugin, flower.getOwnerUniqueId(),
                    plugin.getServer().getOfflinePlayer(flower.getOwnerUniqueId()).getName());

            block.getWorld().dropItemNaturally(block.getLocation(), flowerItem);

            if (breaker != null) {
                if (breaker.getUniqueId().equals(flower.getOwnerUniqueId())) {
                    breaker.sendMessage("You picked up your LifeFlower. You are now in Hardcore mode until you replant it!");
                } else {
                    breaker.sendMessage("You picked up " + plugin.getServer().getOfflinePlayer(flower.getOwnerUniqueId()).getName() + "'s LifeFlower!");
                }
            }
        }
    }
}
