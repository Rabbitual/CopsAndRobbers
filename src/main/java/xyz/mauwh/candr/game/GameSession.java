package xyz.mauwh.candr.game;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import java.util.logging.Level;

import static xyz.mauwh.message.ColoredConsoleStringBuilder.builder;

public class GameSession {

    private final CopsAndRobbersEngine engine;
    private final MessageHandler messageHandler;
    private final GameRegion region;
    private EngineSettings settings;
    private EngineGameSessionTicker ticker;
    private boolean active;

    private final Set<Player> cops;
    private final Set<Player> robbers;
    private final Set<Player> copApplicants;
    private final Set<Player> prisonAccessGrantees;
    private DoorState doorState = DoorState.SECURE;

    public GameSession(@NotNull CopsAndRobbersEngine engine, @NotNull GameRegion region) {
        this.engine = engine;
        this.settings = engine.getSettings();
        this.messageHandler = engine.getMessageHandler();
        this.region = region;
        this.cops = new HashSet<>();
        this.robbers = new HashSet<>();
        this.copApplicants = new HashSet<>();
        this.prisonAccessGrantees = new HashSet<>();
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

    public boolean addRobber(@NotNull Player player) {
        return !isFull() && robbers.add(player);
    }

    public boolean removeRobber(@NotNull Player player) {
        return robbers.remove(player);
    }

    public boolean isRobber(@NotNull Player player) {
        return robbers.contains(player);
    }

    @NotNull
    public Set<Player> getRobbers() {
        return Collections.unmodifiableSet(robbers);
    }

    public void addCop(@NotNull Player player) {
        cops.add(player);
    }

    public boolean removeCop(@NotNull Player player) {
        return cops.remove(player);
    }

    public boolean isCop(@NotNull Player player) {
        return cops.contains(player);
    }

    @NotNull
    public Set<Player> getCops() {
        return Collections.unmodifiableSet(cops);
    }

    public boolean hasMaxAllowedCops() {
        if (getPlayerCount() >= settings.getMinPlayersThreeCops()) {
            return cops.size() >= 3;
        } else if (getPlayerCount() >= settings.getMinPlayersTwoCops()) {
            return cops.size() >= 2;
        }
        return cops.size() != 0;
    }

    public boolean addCopApplicant(@NotNull Player player) {
        if (isPlayer(player) && !hasMaxAllowedCops()) {
            return copApplicants.add(player);
        }
        return false;
    }

    public void removeCopApplicant(@NotNull Player player) {
        copApplicants.remove(player);
    }

    @NotNull
    public List<Player> getCopApplicants() {
        return List.copyOf(copApplicants);
    }

    public boolean isPrisonAccessGrantee(@NotNull Player player) {
        return prisonAccessGrantees.contains(player);
    }

    public boolean addPrisonAccessGrantee(@NotNull Player player) {
        return isRobber(player) && prisonAccessGrantees.add(player);
    }

    public void removePrisonAccessGrantee(@NotNull Player player) {
        prisonAccessGrantees.remove(player);
    }

    public boolean isPlayer(@NotNull Player player) {
        return isRobber(player) || isCop(player);
    }

    public boolean isFull() {
        return (cops.size() + robbers.size()) >= settings.getMaxPlayers();
    }

    @NotNull
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        players.addAll(robbers);
        players.addAll(cops);
        return players;
    }

    public int getPlayerCount() {
        return robbers.size() + cops.size();
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
        builder().green("Successfully started game (id: " + region.getId() + ")").reset().post(engine.getLogger(), Level.INFO);
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
        getPlayers().forEach(this::teleportPlayerToLobby);
        cops.clear();
        robbers.clear();
        prisonAccessGrantees.clear();
        restoreDoors();
    }

    public void makeDoorsVulnerable() {
        doorState = DoorState.VULNERABLE;
        Component message = messageHandler.getMessage(Message.VULNERABILITY_DETECTED, false, region.getId());
        BukkitAudiences audiences = messageHandler.getAudiences();
        audiences.all().sendMessage(message);
    }

    public void malfunctionDoors() {
        if (doorState != DoorState.VULNERABLE) {
            return;
        }

        setDoorsOpen(true);
        doorState = DoorState.MALFUNCTIONING;
        ticker.setDoorMalfunctionTimer();

        Component message = messageHandler.getMessage(Message.DOORS_MALFUNCTIONED, false, region.getId());
        BukkitAudiences audiences = messageHandler.getAudiences();
        audiences.all().sendMessage(message);
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
            Component message = messageHandler.getMessage(Message.CELLS_NOT_FOUND, true);
            Audience audience = messageHandler.getAudiences().player(player);
            audience.sendMessage(message);
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
            Audience audience = messageHandler.getAudiences().player(player);
            audience.sendMessage(messageHandler.getMessage(Message.LOBBY_NOT_FOUND, true));
            return;
        }
        player.teleport(lobbySpawn);
    }

}
