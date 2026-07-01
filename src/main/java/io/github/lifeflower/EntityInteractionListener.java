package io.github.lifeflower;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class EntityInteractionListener implements Listener {
    private final LifeFlowerPlugin plugin;
    private final LifeFlowerManager manager;

    public EntityInteractionListener(LifeFlowerPlugin plugin, LifeFlowerManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof ItemDisplay display)) return;

        UUID ownerUuid = getFlowerOwner(display);
        if (ownerUuid == null) return;

        event.setCancelled(true);
        handleFlowerBreak(display, player, ownerUuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemDisplay display)) return;

        UUID ownerUuid = getFlowerOwner(display);
        if (ownerUuid == null) return;

        event.setCancelled(true);
        // Interaction logic could be added here later (e.g. status GUI)
    }

    private UUID getFlowerOwner(Entity entity) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        String uuidStr = pdc.get(LifeFlowerUtils.getOwnerKey(), PersistentDataType.STRING);
        if (uuidStr == null) return null;
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void handleFlowerBreak(ItemDisplay display, Player breaker, UUID ownerUuid) {
        manager.pickupFlower(ownerUuid);
        display.remove();

        ItemStack flowerItem = LifeFlowerUtils.createLifeFlowerItem(plugin, ownerUuid,
                Bukkit.getOfflinePlayer(ownerUuid).getName());

        display.getWorld().dropItemNaturally(display.getLocation(), flowerItem);

        if (breaker != null) {
            if (breaker.getUniqueId().equals(ownerUuid)) {
                breaker.sendMessage("You picked up your LifeFlower. You are now in Hardcore mode until you replant it!");
            } else {
                breaker.sendMessage("You picked up " + Bukkit.getOfflinePlayer(ownerUuid).getName() + "'s LifeFlower!");
            }
        }
    }
}
