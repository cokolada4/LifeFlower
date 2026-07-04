package io.github.lifeflower;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class PlayerJoinListener implements Listener {
    private final LifeFlowerPlugin plugin;
    private final LifeFlowerManager manager;

    public PlayerJoinListener(LifeFlowerPlugin plugin, LifeFlowerManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.isPluginEnabled()) return;
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        LifeFlower flower = manager.getFlower(uuid);
        if (flower == null || flower.isAwaitingRevivalItem()) {
            if (flower == null) {
                flower = manager.createFlower(uuid);
            } else {
                flower.setAwaitingRevivalItem(false);
                manager.save();
            }

            ItemStack flowerItem = LifeFlowerUtils.createLifeFlowerItem(plugin, uuid, player.getName(), flower.getFlowerUuid());
            Map<Integer, ItemStack> remaining = player.getInventory().addItem(flowerItem);
            if (!remaining.isEmpty()) {
                for (ItemStack item : remaining.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
            player.sendMessage("You have received your LifeFlower! Keep it safe.");
        }
    }
}
