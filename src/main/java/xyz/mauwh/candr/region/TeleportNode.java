package xyz.mauwh.candr.region;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.configuration.SerializationUtils;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.message.MessageHandler;

import java.util.Arrays;
import java.util.Map;

public class TeleportNode extends RegionNode {

    private final Location destination;

    public TeleportNode(@NotNull Location location, @NotNull Location destination, @NotNull Action action) {
        super(location, action);
        this.destination = destination;
    }

    @Override
    public void handle(@NotNull GameSession session, @NotNull Player player, @NotNull MessageHandler messageHandler) {
        destination.setYaw(player.getEyeLocation().getYaw());
        player.teleport(destination);
    }

    @Override
    public boolean canBeUsed(@NotNull GameSession session, @NotNull Player player, @NotNull Location usedBlockLocation, @NotNull Action action) {
        return super.canBeUsed(session, player, usedBlockLocation, action) && session.isPrisonAccessGrantee(player);
    }

    @NotNull
    public Map<String, Object> serialize() {
        return Map.of(
                "type", "teleport",
                "action", action,
                "location", Map.of("x", location.getBlockX(), "y", location.getBlockY(), "z", location.getBlockZ()),
                "destination", Map.of("x", destination.getX(), "y", destination.getY(), "z", destination.getZ())
        );
    }

    @NotNull
    public static TeleportNode deserialize(@NotNull World regionWorld, @NotNull Map<?, ?> map) throws IllegalArgumentException {
        Location location = SerializationUtils.<Map<?, ?>>castDubiously(map.get("location")).map(map1 -> {
            try {
                return SerializationUtils.deserializeLocation(regionWorld, map1);
            } catch (IllegalArgumentException err) {
                return null;
            }
        }).orElseThrow(() -> new IllegalArgumentException("Unable to deserialize teleport node, invalid location"));

        Location destination = SerializationUtils.<Map<?, ?>>castDubiously(map.get("destination")).map(map1 -> {
            try {
                return SerializationUtils.deserializeLocation(regionWorld, map1);
            } catch (IllegalArgumentException err) {
                return null;
            }
        }).orElseThrow(() -> new IllegalArgumentException("Unable to deserialize teleport node, invalid destination"));

        String actionStr = String.valueOf(map.get("action"));
        Action action;
        try {
            action = Action.valueOf(actionStr);
        } catch (IllegalArgumentException err) {
            String joined = String.join(", ", Arrays.stream(Action.values()).map(Object::toString).toArray(String[]::new));
            throw new IllegalArgumentException("Unable to deserialize teleport node, invalid action, must be one of: " + joined, err);
        }

        return new TeleportNode(location, destination, action);
    }

}
