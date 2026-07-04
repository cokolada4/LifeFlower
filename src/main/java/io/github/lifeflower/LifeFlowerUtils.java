package io.github.lifeflower;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LifeFlowerUtils {
    private static final NamespacedKey OWNER_KEY = new NamespacedKey("lifeflower", "owner");
    private static final NamespacedKey FLOWER_ID_KEY = new NamespacedKey("lifeflower", "flower_id");
    private static final NamespacedKey RAID_TOOL_KEY = new NamespacedKey("lifeflower", "raid_tool");
    private static final NamespacedKey DURABILITY_KEY = new NamespacedKey("lifeflower", "durability");
    private static final NamespacedKey REVIVE_BEACON_KEY = new NamespacedKey("lifeflower", "revive_beacon");
    private static final NamespacedKey REVIVE_USES_KEY = new NamespacedKey("lifeflower", "revive_uses");

    public static NamespacedKey getOwnerKey() {
        return OWNER_KEY;
    }

    public static ItemStack createLifeFlowerItem(LifeFlowerPlugin plugin, UUID ownerUuid, String ownerName, UUID flowerUuid) {
        String materialName = plugin.getConfig().getString("flower-material", "POPPY");
        Material material = Material.matchMaterial(materialName);
        if (material == null) material = Material.POPPY;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            int customModelData = plugin.getConfig().getInt("custom-model-data", 0);
            if (customModelData != 0) {
                meta.setCustomModelData(customModelData);
            }

            String name = (ownerName != null) ? ownerName : "Unknown";
            meta.displayName(Component.text(name + "'s LifeFlower", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("This is your life anchor.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Keep it planted on the surface to stay safe.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);

            meta.getPersistentDataContainer().set(OWNER_KEY, PersistentDataType.STRING, ownerUuid.toString());
            meta.getPersistentDataContainer().set(FLOWER_ID_KEY, PersistentDataType.STRING, flowerUuid.toString());
            item.setItemMeta(meta);
        }
        return item;
    }

    public static UUID getFlowerUuid(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        String uuidStr = meta.getPersistentDataContainer().get(FLOWER_ID_KEY, PersistentDataType.STRING);
        if (uuidStr == null) return null;

        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
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

    public static boolean isFlowerMaterial(Material material, LifeFlowerPlugin plugin) {
        String materialName = plugin.getConfig().getString("flower-material", "POPPY");
        Material flowerMaterial = Material.matchMaterial(materialName);
        if (flowerMaterial == null) flowerMaterial = Material.POPPY;
        return material == flowerMaterial;
    }

    public static ItemStack createRaidTool(LifeFlowerPlugin plugin) {
        String materialName = plugin.getConfig().getString("raid-tool.material", "SHEARS");
        Material material = Material.matchMaterial(materialName);
        if (material == null) material = Material.SHEARS;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = plugin.getConfig().getString("raid-tool.name");
            if (name != null) {
                meta.displayName(MiniMessage.miniMessage().deserialize(name).decoration(TextDecoration.ITALIC, false));
            }

            int durability = plugin.getConfig().getInt("raid-tool.durability", 3);
            meta.getPersistentDataContainer().set(DURABILITY_KEY, PersistentDataType.INTEGER, durability);

            updateRaidToolLore(meta, plugin);

            String enchName = plugin.getConfig().getString("raid-tool.enchantment", "FORTUNE");
            Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchName.toLowerCase()));
            if (enchantment != null) {
                meta.addEnchant(enchantment, plugin.getConfig().getInt("raid-tool.level", 1), true);
            }

            meta.getPersistentDataContainer().set(RAID_TOOL_KEY, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void updateRaidToolLore(ItemMeta meta, LifeFlowerPlugin plugin) {
        List<String> loreLines = plugin.getConfig().getStringList("raid-tool.lore");
        List<Component> lore = new ArrayList<>();

        for (String line : loreLines) {
            lore.add(MiniMessage.miniMessage().deserialize(line).decoration(TextDecoration.ITALIC, false));
        }

        if (plugin.getConfig().getBoolean("raid-tool.show-durability-in-lore", true)) {
            int durability = meta.getPersistentDataContainer().getOrDefault(DURABILITY_KEY, PersistentDataType.INTEGER, 0);
            String durFormat = plugin.getConfig().getString("raid-tool.durability-lore-format", "<gray>Remaining uses: <yellow>%uses%</yellow></gray>");
            lore.add(MiniMessage.miniMessage().deserialize(durFormat.replace("%uses%", String.valueOf(durability))).decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);
    }

    public static int getRaidToolDurability(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(DURABILITY_KEY, PersistentDataType.INTEGER, 0);
    }

    public static void setRaidToolDurability(ItemStack item, int durability, LifeFlowerPlugin plugin) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(DURABILITY_KEY, PersistentDataType.INTEGER, durability);
        updateRaidToolLore(meta, plugin);
        item.setItemMeta(meta);
    }

    public static ItemStack createReviveBeacon(LifeFlowerPlugin plugin) {
        ItemStack item = new ItemStack(Material.BEACON);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = plugin.getConfig().getString("revive-beacon.name");
            if (name != null) {
                meta.displayName(MiniMessage.miniMessage().deserialize(name).decoration(TextDecoration.ITALIC, false));
            }

            int uses = plugin.getConfig().getInt("revive-beacon.uses", 1);
            meta.getPersistentDataContainer().set(REVIVE_USES_KEY, PersistentDataType.INTEGER, uses);

            updateReviveBeaconLore(meta, plugin);

            meta.getPersistentDataContainer().set(REVIVE_BEACON_KEY, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void updateReviveBeaconLore(ItemMeta meta, LifeFlowerPlugin plugin) {
        List<String> loreLines = plugin.getConfig().getStringList("revive-beacon.lore");
        List<Component> lore = new ArrayList<>();

        for (String line : loreLines) {
            lore.add(MiniMessage.miniMessage().deserialize(line).decoration(TextDecoration.ITALIC, false));
        }

        if (plugin.getConfig().getBoolean("revive-beacon.show-uses-in-lore", true)) {
            int uses = meta.getPersistentDataContainer().getOrDefault(REVIVE_USES_KEY, PersistentDataType.INTEGER, 0);
            String format = plugin.getConfig().getString("revive-beacon.uses-lore-format", "<gray>Remaining uses: <yellow>%uses%</yellow></gray>");
            lore.add(MiniMessage.miniMessage().deserialize(format.replace("%uses%", String.valueOf(uses))).decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);
    }

    public static boolean isReviveBeacon(ItemStack item) {
        if (item == null || item.getType() != Material.BEACON) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(REVIVE_BEACON_KEY, PersistentDataType.BYTE);
    }

    public static int getReviveBeaconUses(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(REVIVE_USES_KEY, PersistentDataType.INTEGER, 0);
    }

    public static void setReviveBeaconUses(ItemStack item, int uses, LifeFlowerPlugin plugin) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(REVIVE_USES_KEY, PersistentDataType.INTEGER, uses);
        updateReviveBeaconLore(meta, plugin);
        item.setItemMeta(meta);
    }

    public static boolean isRaidTool(ItemStack item, LifeFlowerPlugin plugin) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        if (!meta.getPersistentDataContainer().has(RAID_TOOL_KEY, PersistentDataType.BYTE)) return false;

        // Optionally check enchantments if configured
        String enchName = plugin.getConfig().getString("raid-tool.enchantment");
        if (enchName != null) {
            Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchName.toLowerCase()));
            if (enchantment != null) {
                int requiredLevel = plugin.getConfig().getInt("raid-tool.level", 1);
                int currentLevel = meta.getEnchantLevel(enchantment);
                boolean exactMatch = plugin.getConfig().getBoolean("raid-tool.exact-match", false);

                if (exactMatch) {
                    if (currentLevel != requiredLevel) return false;
                } else {
                    if (currentLevel < requiredLevel) return false;
                }
            }
        }

        return true;
    }
}
