package io.github.lifeflower;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class ReviveListener implements Listener {
    private final LifeFlowerPlugin plugin;
    private final LifeFlowerManager manager;

    public ReviveListener(LifeFlowerPlugin plugin, LifeFlowerManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.isPluginEnabled()) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (LifeFlowerUtils.isReviveBeacon(item)) {
            event.setCancelled(true);
            ReviveGUI.open(event.getPlayer(), plugin, manager);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.isPluginEnabled()) return;
        if (!(event.getInventory().getHolder() instanceof ReviveInventoryHolder)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() != Material.PLAYER_HEAD) return;

        Player reviver = (Player) event.getWhoClicked();
        ItemStack head = event.getCurrentItem();
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null || meta.getOwningPlayer() == null) return;

        OfflinePlayer target = meta.getOwningPlayer();
        String targetName = target.getName();

        LifeFlower flower = manager.getFlower(target.getUniqueId());
        if (flower != null && flower.isEliminated()) {
            if (target.getName() != null) {
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(target.getName());
            }
            manager.revivePlayer(target.getUniqueId());

            // Consume beacon use
            ItemStack main = reviver.getInventory().getItemInMainHand();
            ItemStack off = reviver.getInventory().getItemInOffHand();
            ItemStack beacon = LifeFlowerUtils.isReviveBeacon(main) ? main : LifeFlowerUtils.isReviveBeacon(off) ? off : null;

            if (beacon != null) {
                int uses = LifeFlowerUtils.getReviveBeaconUses(beacon);
                uses--;
                if (uses <= 0) {
                    beacon.setAmount(0);
                    reviver.playSound(reviver.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                } else {
                    LifeFlowerUtils.setReviveBeaconUses(beacon, uses, plugin);
                }
            }

            String successMsg = plugin.getConfig().getString("revive-beacon.revive-success-sender", "<green>You have revived %player%!</green>");
            reviver.sendMessage(MiniMessage.miniMessage().deserialize(successMsg.replace("%player%", targetName)));
            reviver.closeInventory();
            reviver.playSound(reviver.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<light_purple>[LifeFlower] <yellow>" + targetName + " has been revived by " + reviver.getName() + "!</yellow>"));
        }
    }
}
