package xyz.mauwh.candr.region;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.configuration.SerializationUtils;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.PlayerState;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

import java.util.Arrays;
import java.util.Map;

public class AccessNode extends RegionNode {

    public AccessNode(@NotNull Location location, @NotNull Action action) {
        super(location, action);
    }

    @Override
    public void handle(@NotNull GameSession session, @NotNull Player player, @NotNull MessageHandler messageHandler) {
        if (session.getPlayerState(player).isRobber()) {
            session.setPlayerState(player, PlayerState.ROBBER_UNRESTRICTED);
            messageHandler.sendMessage(player, Message.PRISON_ACCESS_GRANTED, true);
        }
    }

    @NotNull
    public Map<String, Object> serialize() {
        return Map.of(
                "type", "teleport",
                "action", action,
                "location", Map.of("x", location.getBlockX(), "y", location.getBlockY(), "z", location.getBlockZ())
        );
    }

    @NotNull
    public static AccessNode deserialize(@NotNull World regionWorld, @NotNull Map<?, ?> map) throws IllegalArgumentException {
        Location location = SerializationUtils.<Map<?, ?>>castDubiously(map.get("location")).map(map1 -> {
            try {
                return SerializationUtils.deserializeLocation(regionWorld, map1);
            } catch (IllegalArgumentException err) {
                return null;
            }
        }).orElseThrow(() -> new IllegalArgumentException("Unable to deserialize access node, invalid location"));

        String actionStr = String.valueOf(map.get("action"));
        Action action;
        try {
            action = Action.valueOf(actionStr);
        } catch (IllegalArgumentException err) {
            String joined = String.join(", ", Arrays.stream(Action.values()).map(Object::toString).toArray(String[]::new));
            throw new IllegalArgumentException("Unable to deserialize access node, invalid action, must be one of: " + joined, err);
        }

        return new AccessNode(location, action);
    }

}
