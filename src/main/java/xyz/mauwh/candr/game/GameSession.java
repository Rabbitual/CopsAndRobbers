package xyz.mauwh.candr.game;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
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
    private final EngineSettings settings;
    private final MessageHandler messageHandler;
    private final GameRegion region;
    private EngineGameSessionTicker ticker;
    private boolean active;

    private final List<Player> cops;
    private final List<Player> robbers;
    private final List<Player> copApplicants;
    private DoorState doorState = DoorState.SECURE;

    public GameSession(@NotNull CopsAndRobbersEngine engine, @NotNull GameRegion region) {
        this.engine = engine;
        this.settings = engine.getSettings();
        this.messageHandler = engine.getMessageHandler();
        this.region = region;
        this.cops = new ArrayList<>();
        this.robbers = new ArrayList<>();
        this.copApplicants = new ArrayList<>();
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
        if (isFull()) {
            return false;
        }
        robbers.add(player);
        return true;
    }

    public boolean removeRobber(@NotNull Player player) {
        return robbers.remove(player);
    }

    public boolean isRobber(@NotNull Player player) {
        return robbers.contains(player);
    }

    @NotNull
    public List<Player> getRobbers() {
        return Collections.unmodifiableList(robbers);
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
    public List<Player> getCops() {
        return Collections.unmodifiableList(cops);
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
            copApplicants.add(player);
            return true;
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
        if (ticker == null) {
            ticker = new EngineGameSessionTicker(this);
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
        BukkitAudiences audiences = messageHandler.getAudiences();
        if (winner == null && broadcast) {
            Component message = messageHandler.getMessage(Message.NO_ESCAPEES, true, region.getId());
            audiences.all().sendMessage(message);
        } else if (broadcast) {
            Component message = messageHandler.getMessage(Message.ROBBER_ESCAPED, true, winner.getName(), region.getId());
            audiences.all().sendMessage(message);
        }

        for (List<Player> players : List.of(robbers, cops)) {
            Iterator<Player> iter = players.iterator();
            while (iter.hasNext()) {
                Player player = iter.next();
                teleportPlayerToLobby(player);
                iter.remove();
            }
        }
        restoreDoors();
    }

    public void makeDoorsVulnerable() {
        doorState = DoorState.VULNERABLE;
        Bukkit.broadcastMessage(String.format("%1$s[%2$s!%1$s] %3$sA vulnerability in jail %4$s#%5$s%3$s's security has been detected! Robbers, type '/open cells' and make your jailbreak!",
                ChatColor.DARK_GRAY, ChatColor.DARK_RED, ChatColor.YELLOW, ChatColor.GOLD, region.getId()));
    }

    public void malfunctionDoors() {
        if (doorState != DoorState.VULNERABLE) {
            return;
        }

        setDoorsOpen(true);
        doorState = DoorState.MALFUNCTIONING;
        ticker.resetDoorMalfunctionTick();
        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "!" + ChatColor.DARK_GRAY + "] "
                + ChatColor.RED + "The doors in jail " + ChatColor.GOLD + "#" + region.getId() + ChatColor.RED + " are malfunctioning! Robbers, now's your chance!");
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
            player.sendMessage(ChatColor.DARK_RED + "[CopsAndRobbers] " + ChatColor.RED + "Unfinished (GameSession.java/246)");
            return;
        }

        Random random = ThreadLocalRandom.current();
        int num = random.nextInt(cellLocations.size());
        Location spawnPos = cellLocations.get(num);
        player.teleport(spawnPos);
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
