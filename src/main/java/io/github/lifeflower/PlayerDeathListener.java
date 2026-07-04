package io.github.lifeflower;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class PlayerDeathListener implements Listener {
    private final LifeFlowerPlugin plugin;
    private final LifeFlowerManager manager;

    public PlayerDeathListener(LifeFlowerPlugin plugin, LifeFlowerManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.isPluginEnabled()) return;
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        LifeFlower flower = manager.getFlower(uuid);

        // Re-validate surface status at time of death
        boolean valid = flower != null && flower.isPlanted() && manager.validateSurface(flower.getLocation());

        if (!valid) {
            String punishment = plugin.getConfig().getString("punishment-type", "SPECTATOR");
            String message = plugin.getConfig().getString("elimination-message", "Your LifeFlower was missing or invalid! You have been eliminated.");

            if ("BAN".equalsIgnoreCase(punishment)) {
                player.banPlayer(message);
            } else {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(Component.text(message, NamedTextColor.RED));
            }
            pluginBroadcast(player.getName() + " has been eliminated because their LifeFlower was not valid!");
        }
    }

    private void pluginBroadcast(String message) {
        org.bukkit.Bukkit.broadcast(Component.text("[LifeFlower] ", NamedTextColor.LIGHT_PURPLE).append(Component.text(message, NamedTextColor.YELLOW)));
    }
}
