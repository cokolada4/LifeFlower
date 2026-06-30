package io.github.lifeflower;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

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
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        LifeFlower flower = manager.getFlower(uuid);
        if (flower == null) {
            manager.createFlower(uuid);
            ItemStack flowerItem = LifeFlowerUtils.createLifeFlowerItem(plugin, uuid, player.getName());
            player.getInventory().addItem(flowerItem);
            player.sendMessage("You have received your LifeFlower! Keep it safe.");
        }
    }
}
