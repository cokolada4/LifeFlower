package io.github.lifeflower;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ReviveGUI {

    public static void open(Player player, LifeFlowerPlugin plugin, LifeFlowerManager manager) {
        List<OfflinePlayer> deadPlayers = new ArrayList<>();
        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            LifeFlower flower = manager.getFlower(offline.getUniqueId());
            if (flower != null && flower.isEliminated()) {
                deadPlayers.add(offline);
            }
        }

        if (deadPlayers.isEmpty()) {
            String msg = plugin.getConfig().getString("revive-beacon.no-dead-players-message", "<red>There are no dead players to revive!</red>");
            player.sendMessage(MiniMessage.miniMessage().deserialize(msg));
            return;
        }

        int size = ((deadPlayers.size() / 9) + 1) * 9;
        if (size > 54) size = 54;

        String title = plugin.getConfig().getString("revive-beacon.gui-title", "Select a player to revive");
        Inventory inv = Bukkit.createInventory(new ReviveInventoryHolder(), size, MiniMessage.miniMessage().deserialize(title));

        for (int i = 0; i < deadPlayers.size() && i < 54; i++) {
            OfflinePlayer target = deadPlayers.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                meta.displayName(Component.text(target.getName() != null ? target.getName() : "Unknown"));
                head.setItemMeta(meta);
            }
            inv.setItem(i, head);
        }

        player.openInventory(inv);
    }
}
