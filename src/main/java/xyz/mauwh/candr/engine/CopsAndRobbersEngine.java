package xyz.mauwh.candr.engine;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.mauwh.candr.CopsAndRobbersPlugin;
import xyz.mauwh.candr.engine.configuration.EngineSettings;
import xyz.mauwh.candr.engine.configuration.RegionSerializer;
import xyz.mauwh.candr.game.GameRegion;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.message.MessageHandler;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static xyz.mauwh.message.ColoredConsoleStringBuilder.builder;

public class CopsAndRobbersEngine {

    private final CopsAndRobbersPlugin plugin;
    private final Logger logger;
    private final EngineSettings settings;
    private final MessageHandler messageHandler;
    private final Map<Integer, GameSession> sessions;
    private boolean active;
    private BukkitRunnable runnable;

    public CopsAndRobbersEngine(@NotNull CopsAndRobbersPlugin plugin, @NotNull EngineSettings settings, @NotNull MessageHandler messageHandler) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.settings = settings;
        this.messageHandler = messageHandler;
        this.sessions = new HashMap<>();
    }

    @NotNull
    public CopsAndRobbersPlugin getPlugin() {
        return plugin;
    }

    @NotNull
    public Logger getLogger() {
        return logger;
    }

    @NotNull
    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    @NotNull
    public Map<Integer, GameSession> getSessions() {
        return Collections.unmodifiableMap(sessions);
    }

    @Nullable
    public GameSession getSession(int id) {
        return sessions.get(id);
    }

    @NotNull
    public EngineSettings getSettings() {
        return settings;
    }

    public boolean isPlayer(@NotNull Player player) {
        return getGameSession(player) != null;
    }

    @Nullable
    public GameSession getGameSession(@NotNull Player player) {
        for (GameSession session : sessions.values()) {
            if (session.isPlayer(player)) {
                return session;
            }
        }
        return null;
    }

    public void initialize() {
        File file = new File(plugin.getDataFolder(), "regions.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains("regions")) {
            logger.info("No existing session configurations found, skipping setup");
            return;
        }

        RegionSerializer serializer = new RegionSerializer(logger);
        List<Map<?, ?>> serializedRegions = config.getMapList("regions");

        serializedRegions.stream().map(serializedRegion -> {
            GameRegion region = serializer.deserialize(serializedRegion);
            return new GameSession(this, region);
        }).forEach(session -> {
            int id = session.getRegion().getId();
            if (sessions.containsKey(id)) {
                builder().yellow("Unable to load region: game region with id '" + id + "' already exists").reset().post(logger, Level.WARNING);
                return;
            }
            sessions.put(id, session);
            session.start();
        });

        if (sessions.isEmpty()) {
            logger.info("No existing session configurations found, skipping setup");
            return;
        }

        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                sessions.values().forEach(GameSession::tick);
                if (!active) {
                    cancel();
                }
            }
        };
        runnable.runTaskTimer(plugin, 0L, 20L);
        active = true;
    }

    public void halt() {
        if (!active) {
            return;
        }

        if (!runnable.isCancelled()) {
            runnable.cancel();
        }

        sessions.values().forEach(session -> session.endGame(null, false));
        sessions.clear();
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    @NotNull
    public String[] getSessionIDsAsStringArray() {
        return sessions.values().stream()
                .map(session -> session.getRegion().getId()).sorted()
                .map(String::valueOf).toArray(String[]::new);
    }

}
