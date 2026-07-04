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

    public static void open(Player player, LifeFlowerPlugin plugin) {
        List<OfflinePlayer> bannedPlayers = new ArrayList<>();
        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            if (Bukkit.getBanList(org.bukkit.BanList.Type.NAME).isBanned(offline.getName())) {
                bannedPlayers.add(offline);
            }
        }

        if (bannedPlayers.isEmpty()) {
            String msg = plugin.getConfig().getString("revive-beacon.no-dead-players-message", "<red>There are no dead players to revive!</red>");
            player.sendMessage(MiniMessage.miniMessage().deserialize(msg));
            return;
        }

        int size = ((bannedPlayers.size() / 9) + 1) * 9;
        if (size > 54) size = 54;

        String title = plugin.getConfig().getString("revive-beacon.gui-title", "Select a player to revive");
        Inventory inv = Bukkit.createInventory(null, size, MiniMessage.miniMessage().deserialize(title));

        for (int i = 0; i < bannedPlayers.size() && i < 54; i++) {
            OfflinePlayer target = bannedPlayers.get(i);
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
