package io.github.lifeflower;

import org.bukkit.Location;
import java.util.UUID;

public class LifeFlower {
    private final UUID ownerUniqueId;
    private Location location;
    private boolean planted;
    private boolean valid;

    public LifeFlower(UUID ownerUniqueId) {
        this.ownerUniqueId = ownerUniqueId;
        this.planted = false;
        this.valid = false;
    }

    public UUID getOwnerUniqueId() {
        return ownerUniqueId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isPlanted() {
        return planted;
    }

    public void setPlanted(boolean planted) {
        this.planted = planted;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
