package io.github.lifeflower;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class ReviveInventoryHolder implements InventoryHolder {
    @Override
    public @NotNull Inventory getInventory() {
        return null; // Not needed for identification
    }
}
