package xyz.mauwh.candr.region;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.message.MessageHandler;

public abstract class RegionNode {

    protected final Location location;
    protected final Action action;

    protected RegionNode(@NotNull Location location, @NotNull Action action) {
        this.location = location;
        this.action = action;
    }

    public abstract void handle(@NotNull GameSession session, @NotNull Player player, @NotNull MessageHandler messageHandler);

    public boolean canBeUsed(@NotNull GameSession session, @NotNull Player player, @NotNull Location usedBlockLocation, @NotNull Action action) {
        return this.action == action && isMatchingLocation(this.location, usedBlockLocation);
    }

    private boolean isMatchingLocation(@NotNull Location location, @NotNull Location other) {
        return location.getBlockX() == other.getBlockX() &&
                location.getBlockY() == other.getBlockY() &&
                location.getBlockZ() == other.getBlockZ();
    }

}
