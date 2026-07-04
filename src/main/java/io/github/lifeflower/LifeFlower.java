package io.github.lifeflower;

import org.bukkit.Location;
import java.util.UUID;

public class LifeFlower {
    private final UUID ownerUniqueId;
    private UUID flowerUuid;
    private Location location;
    private boolean planted;
    private boolean valid;
    private boolean awaitingRevivalItem;
    private boolean eliminated;

    public LifeFlower(UUID ownerUniqueId) {
        this.ownerUniqueId = ownerUniqueId;
        this.flowerUuid = UUID.randomUUID();
        this.planted = false;
        this.valid = false;
        this.awaitingRevivalItem = false;
        this.eliminated = false;
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

    public boolean isAwaitingRevivalItem() {
        return awaitingRevivalItem;
    }

    public void setAwaitingRevivalItem(boolean awaitingRevivalItem) {
        this.awaitingRevivalItem = awaitingRevivalItem;
    }

    public UUID getFlowerUuid() {
        return flowerUuid;
    }

    public void setFlowerUuid(UUID flowerUuid) {
        this.flowerUuid = flowerUuid;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }
}
