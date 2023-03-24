package xyz.mauwh.candr.engine;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.CopsAndRobbersPlugin;
import xyz.mauwh.candr.engine.configuration.EngineSettings;
import xyz.mauwh.candr.engine.configuration.RegionSerializer;
import xyz.mauwh.candr.game.GameRegion;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.SessionManager;
import xyz.mauwh.message.MessageHandler;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class CopsAndRobbersEngine {

    private final CopsAndRobbersPlugin plugin;
    private final Logger logger;
    private final EngineSettings settings;
    private final MessageHandler messageHandler;
    private final SessionManager sessionManager;

    public CopsAndRobbersEngine(@NotNull CopsAndRobbersPlugin plugin, @NotNull EngineSettings settings, @NotNull MessageHandler messageHandler) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.settings = settings;
        this.messageHandler = messageHandler;
        this.sessionManager = new SessionManager(plugin, this);
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
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    @NotNull
    public EngineSettings getSettings() {
        return settings;
    }

    public void initialize() {
        File file = new File(plugin.getDataFolder(), "regions.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains("regions")) {
            logger.info("No existing session configurations found, skipping setup");
            return;
        }

        RegionSerializer serializer = new RegionSerializer(logger);
        for (Map<?, ?> serializedRegion : config.getMapList("regions")) {
            GameRegion region = serializer.deserialize(serializedRegion);
            GameSession session = new GameSession(this, region);
            int id = session.getId();
            if (sessionManager.getSession(id) != null) {
                logger.warning("Unable to load region: game region with id '" + id + "' already exists");
                return;
            }
            sessionManager.addSession(session);
            sessionManager.start(session);
        }

        if (sessionManager.getSessions().isEmpty()) {
            logger.info("No existing session configurations found, skipping setup");
        }
    }

}
