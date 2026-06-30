package io.github.lifeflower;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LifeFlowerUtils {
    private static final NamespacedKey OWNER_KEY = new NamespacedKey("lifeflower", "owner");

    public static ItemStack createLifeFlowerItem(LifeFlowerPlugin plugin, UUID ownerUuid, String ownerName) {
        ItemStack item = new ItemStack(Material.WITHER_ROSE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = (ownerName != null) ? ownerName : "Unknown";
            meta.displayName(Component.text(name + "'s LifeFlower", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("This is your life anchor.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Keep it planted on the surface to stay safe.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);

            meta.getPersistentDataContainer().set(OWNER_KEY, PersistentDataType.STRING, ownerUuid.toString());
            item.setItemMeta(meta);
        }
        return item;
    }

    public static UUID getOwnerUuid(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        String uuidStr = meta.getPersistentDataContainer().get(OWNER_KEY, PersistentDataType.STRING);
        if (uuidStr == null) return null;

        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean isLifeFlower(ItemStack item) {
        return getOwnerUuid(item) != null;
    }
}
