package xyz.mauwh.candr.game;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.mauwh.candr.CopsAndRobbersPlugin;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.engine.configuration.EngineSettings;
import xyz.mauwh.candr.engine.ticker.EngineGameSessionTicker;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class SessionManager {

    private final CopsAndRobbersPlugin plugin;
    private final CopsAndRobbersEngine engine;
    private final MessageHandler messageHandler;
    private final Logger logger;
    private final Map<Integer, GameSession> sessionsById = new HashMap<>();
    private final Map<GameSession, EngineGameSessionTicker> tickers = new HashMap<>();
    private boolean active;

    public SessionManager(@NotNull CopsAndRobbersPlugin plugin, @NotNull CopsAndRobbersEngine engine) {
        this.plugin = plugin;
        this.engine = engine;
        this.messageHandler = engine.getMessageHandler();
        this.logger = plugin.getLogger();
    }

    public void initializeTask() {
        active = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                tickers.forEach((session, ticker) -> {
                    if (!active) {
                        cancel();
                    } else if (session.isActive()) {
                        ticker.tick();
                    }
                });
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public boolean isActive() {
        return active;
    }

    public void addSession(@NotNull GameSession session) {
        int id = session.getId();
        if (sessionsById.containsKey(id)) {
            logger.warning("Unable to load region: game region with id '" + id + "' already exists");
            return;
        }
        sessionsById.put(id, session);
    }

    public void removeSession(@NotNull GameSession session) {
        sessionsById.remove(session.getId(), session);
        tickers.remove(session);
    }

    @Nullable
    public GameSession getSession(int id) {
        return sessionsById.get(id);
    }

    @Nullable
    public GameSession getSession(Player player) {
        for (GameSession session : sessionsById.values()) {
            if (session.isPlayer(player)) {
                return session;
            }
        }
        return null;
    }

    public Collection<GameSession> getSessions() {
        return new HashSet<>(tickers.keySet());
    }

    public void start(@NotNull GameSession session) {
        MessageHandler messageHandler = engine.getMessageHandler();
        EngineGameSessionTicker ticker = new EngineGameSessionTicker(this, session, messageHandler);
        ticker.reset();
        tickers.put(session, ticker);
        session.setActive(true);
        engine.getLogger().info("Successfully started game (id: " + session.getId() + ")");
    }

    public void stop(@NotNull GameSession session, @Nullable Player winner, boolean restart) {
        if (winner == null) {
            messageHandler.broadcast(Message.NO_ESCAPEES, true, session.getId());
        } else {
            messageHandler.broadcast(Message.ROBBER_ESCAPED, true, winner.getName(), session.getId());
        }

        session.getPlayers().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            Objects.requireNonNull(player);
            Location lobbySpawn = engine.getSettings().getLobbySpawn();
            if (lobbySpawn != null) {
                player.teleport(lobbySpawn);
            }
            player.getInventory().clear();
            session.removePlayer(player);
        });

        changeDoorState(session, DoorState.SECURE);

        if (!restart) {
            session.setActive(false);
            tickers.remove(session);
        }
    }

    public void stopAll() {
        sessionsById.values().forEach(session -> stop(session, null, false));
        active = false;
    }

    public void onJoin(@NotNull GameSession session, @NotNull Player player) {
        session.setPlayerState(player, PlayerState.ROBBER);
        teleportToRandomCell(session.getRegion(), player);
        messageHandler.sendMessage(player, Message.JOINED_GAME, true, session.getId());
        if (!session.hasMaxAllowedCops()) {
            messageHandler.sendMessage(player, Message.JAIL_COULD_USE_COPS, true);
        }
    }

    public void onQuit(@NotNull GameSession session, @NotNull Player player) {
        teleportToLobby(player);
        messageHandler.sendMessage(player, Message.LEFT_GAME, true, session.getId());

        PlayerState oldState = session.removePlayer(player);
        boolean wasCop = (oldState == PlayerState.COP);
        if (wasCop && !session.hasMaxAllowedCops()) {
            messageHandler.broadcast(Message.COP_RETIRED, true, session.getId());
        }
    }

    public void setChunksForceLoaded(@NotNull GameSession session, boolean loaded) {
        GameRegion region = session.getRegion();
        World world = region.getWorld();
        Chunk minChunk = region.getMinPos().getChunk();
        Chunk maxChunk = region.getMaxPos().getChunk();
        for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
            for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                Chunk chunk = world.getChunkAt(x, z);
                if (loaded) {
                    chunk.addPluginChunkTicket(plugin);
                } else {
                    chunk.removePluginChunkTicket(plugin);
                }
            }
        }
    }

    public void changeDoorState(@NotNull GameSession session, @NotNull DoorState state) {
        session.setDoorState(state);
        switch (state) {
            case SECURE -> setDoorsOpen(session.getRegion(), false);
            case VULNERABLE -> messageHandler.broadcast(Message.VULNERABILITY_DETECTED, false, session.getId());
            case MALFUNCTIONING -> {
                setDoorsOpen(session.getRegion(), true);
                tickers.get(session).setDoorMalfunctionTimer();
                messageHandler.broadcast(Message.DOORS_MALFUNCTIONED, false, session.getId());
            }
        }
    }

    private void setDoorsOpen(@NotNull GameRegion region, boolean open) {
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

    public void teleportToRandomCell(@NotNull GameRegion region, @NotNull Player player) {
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

    public void teleportToLobby(@NotNull Player player) {
        EngineSettings settings = engine.getSettings();
        Location lobbySpawn = settings.getLobbySpawn();
        if (lobbySpawn == null) {
            messageHandler.sendMessage(player, Message.LOBBY_NOT_FOUND, true);
            return;
        }
        player.teleport(lobbySpawn);
    }

}
