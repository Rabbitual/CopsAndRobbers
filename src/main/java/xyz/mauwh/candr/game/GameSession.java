package xyz.mauwh.candr.game;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.engine.configuration.EngineSettings;

import java.util.*;

public class GameSession {

    private final CopsAndRobbersEngine engine;
    private final GameRegion region;
    private final EngineSettings settings;
    private boolean active;

    private final Map<UUID, PlayerState> playerStates;
    private final Set<UUID> copApplicants;
    private DoorState doorState = DoorState.SECURE;

    public GameSession(@NotNull CopsAndRobbersEngine engine, @NotNull GameRegion region) {
        this.engine = engine;
        this.settings = engine.getSettings();
        this.region = region;
        this.playerStates = new HashMap<>();
        this.copApplicants = new HashSet<>();
    }

    @NotNull
    public CopsAndRobbersEngine getEngine() {
        return engine;
    }

    @NotNull
    public EngineSettings getSettings() {
        return settings;
    }

    @NotNull
    public GameRegion getRegion() {
        return region;
    }

    public int getId() {
        return region.getId();
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public boolean setPlayerState(@NotNull Player player, PlayerState state) {
        UUID uuid = player.getUniqueId();
        if (state == null) {
            return playerStates.remove(uuid) != null;
        }
        return state != playerStates.put(uuid, state);
    }

    public boolean removePlayer(@NotNull Player player) {
        return playerStates.remove(player.getUniqueId()) != null;
    }

    public PlayerState getPlayerState(@NotNull Player player) {
        return playerStates.get(player.getUniqueId());
    }

    public boolean isPlayer(@NotNull Player player) {
        return playerStates.containsKey(player.getUniqueId());
    }

    @NotNull
    public List<UUID> getPlayers() {
        return new ArrayList<>(playerStates.keySet());
    }

    public int getPlayerCount() {
        return playerStates.size();
    }

    public void setDoorState(@NotNull DoorState doorState) {
        this.doorState = doorState;
    }

    public DoorState getDoorState() {
        return doorState;
    }

    public boolean addCopApplicant(@NotNull Player player) {
        if (isPlayer(player) && !hasMaxAllowedCops()) {
            return copApplicants.add(player.getUniqueId());
        }
        return false;
    }

    public void removeCopApplicant(@NotNull Player player) {
        copApplicants.remove(player.getUniqueId());
    }

    @NotNull
    public List<UUID> getCopApplicants() {
        return List.copyOf(copApplicants);
    }

    public boolean hasMaxPlayers() {
        return playerStates.size() >= settings.getMaxPlayers();
    }

    public boolean hasMaxAllowedCops() {
        long cops = playerStates.entrySet().stream()
                .filter(entry -> entry.getValue() == PlayerState.COP)
                .count();
        if (getPlayerCount() >= settings.getMinPlayersThreeCops()) {
            return cops >= 3;
        } else if (getPlayerCount() >= settings.getMinPlayersTwoCops()) {
            return cops >= 2;
        } else return cops == 1;
    }

}
