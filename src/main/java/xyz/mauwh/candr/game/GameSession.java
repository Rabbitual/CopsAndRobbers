package xyz.mauwh.candr.game;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.engine.configuration.EngineSettings;
import xyz.mauwh.candr.engine.ticker.EngineGameSessionTicker;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GameSession {

    private final CopsAndRobbersEngine engine;
    private final MessageHandler messageHandler;
    private final GameRegion region;
    private EngineSettings settings;
    private EngineGameSessionTicker ticker;
    private boolean active;

    private final Map<UUID, PlayerState> playerStates;
    private final Set<UUID> copApplicants;
    private DoorState doorState = DoorState.SECURE;

    public GameSession(@NotNull CopsAndRobbersEngine engine, @NotNull GameRegion region) {
        this.engine = engine;
        this.settings = engine.getSettings();
        this.messageHandler = engine.getMessageHandler();
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

    public boolean setPlayerState(@NotNull Player player, PlayerState state) {
        return state != playerStates.put(player.getUniqueId(), state);
    }

    public boolean removePlayer(@NotNull Player player) {
        return playerStates.remove(player.getUniqueId()) != null;
    }

    public PlayerState getPlayerState(@NotNull Player player) {
        return playerStates.get(player.getUniqueId());
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

    public boolean isPlayer(@NotNull Player player) {
        return playerStates.containsKey(player.getUniqueId());
    }

    public boolean isFull() {
        return playerStates.size() >= settings.getMaxPlayers();
    }

    @NotNull
    public List<UUID> getPlayers() {
        return new ArrayList<>(playerStates.keySet());
    }

    public int getPlayerCount() {
        return playerStates.size();
    }

    @NotNull
    public DoorState getDoorState() {
        return doorState;
    }

    public boolean isActive() {
        return active;
    }

    public void prepare() {
        World world = region.getWorld();
        Chunk minChunk = region.getMinPos().getChunk();
        Chunk maxChunk = region.getMaxPos().getChunk();
        for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
            for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                Chunk chunk = world.getChunkAt(x, z);
                chunk.addPluginChunkTicket(engine.getPlugin());
            }
        }
    }

    public void start() {
        settings = engine.getSettings();
        if (ticker == null) {
            ticker = new EngineGameSessionTicker(this, messageHandler);
        }
        ticker.reset();
        active = true;
        engine.getLogger().info("Successfully started game (id: " + region.getId() + ")");
    }

    public void tick() {
        if (this.isActive()) {
            ticker.tick();
        }
    }

    public void endGame(@Nullable Player winner, boolean broadcast) {
        if (winner == null && broadcast) {
            messageHandler.broadcast(Message.NO_ESCAPEES, true, region.getId());
        } else if (broadcast) {
            messageHandler.broadcast(Message.ROBBER_ESCAPED, true, winner.getName(), region.getId());
        }

        getPlayers().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.getInventory().clear();
                teleportPlayerToLobby(player);
            }
        });

        playerStates.clear();
        restoreDoors();
        active = false;
    }

    public void makeDoorsVulnerable() {
        doorState = DoorState.VULNERABLE;
        messageHandler.broadcast(Message.VULNERABILITY_DETECTED, false, region.getId());
    }

    public void malfunctionDoors() {
        if (doorState != DoorState.VULNERABLE) {
            return;
        }

        setDoorsOpen(true);
        doorState = DoorState.MALFUNCTIONING;
        ticker.setDoorMalfunctionTimer();
        messageHandler.broadcast(Message.DOORS_MALFUNCTIONED, false, region.getId());
    }

    public void restoreDoors() {
        if (doorState == DoorState.SECURE) {
            return;
        }
        setDoorsOpen(false);
        doorState = DoorState.SECURE;
    }

    private void setDoorsOpen(boolean open) {
        World world = region.getWorld();
        for (Location location : region.getDoorLocations()) {
            Block block = world.getBlockAt(location);
            if (block.getType() == Material.IRON_DOOR) {
                BlockData blockData = block.getBlockData();
                ((Openable)blockData).setOpen(open);
                block.setBlockData(blockData);
            }
        }
    }

    public void teleportRobberToCell(@NotNull Player player) {
        List<Location> cellLocations = region.getRobberSpawnPoints();
        if (cellLocations.isEmpty()) {
            messageHandler.sendMessage(player, Message.CELLS_NOT_FOUND, true);
            return;
        }

        Random random = ThreadLocalRandom.current();
        int num = random.nextInt(cellLocations.size());
        Location spawnPos = cellLocations.get(num);
        player.teleport(spawnPos);
    }

    public void teleportCopToMainRoom(@NotNull Player player) {
        player.teleport(region.getCopSpawnPoint());
    }

    public void teleportPlayerToLobby(@NotNull Player player) {
        Location lobbySpawn = settings.getLobbySpawn();
        if (lobbySpawn == null) {
            messageHandler.sendMessage(player, Message.LOBBY_NOT_FOUND, true);
            return;
        }
        player.teleport(lobbySpawn);
    }

}
