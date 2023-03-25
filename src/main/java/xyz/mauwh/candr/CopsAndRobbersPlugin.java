package xyz.mauwh.candr;

import co.aikar.commands.*;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.command.*;
import xyz.mauwh.candr.command.admin.AdminHaltCommand;
import xyz.mauwh.candr.command.admin.AdminItemCommand;
import xyz.mauwh.candr.command.admin.AdminSettingsCommand;
import xyz.mauwh.candr.command.admin.AdminStartCommand;
import xyz.mauwh.candr.command.context.GameSessionContextResolver;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.engine.configuration.EngineSettings;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.SessionManager;
import xyz.mauwh.candr.listener.PlayerInteractListener;
import xyz.mauwh.candr.engine.PrisonInteractionsHandler;
import xyz.mauwh.candr.listener.PlayerJoinQuitListener;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public final class CopsAndRobbersPlugin extends JavaPlugin {

    private final Logger logger;

    public CopsAndRobbersPlugin() {
        this.logger = new CopsAndRobbersLogger(this);
    }

    @Override
    public void onEnable() {
        File settingsFile = new File(getDataFolder(), "config.yml");
        EngineSettings settings = new EngineSettings(settingsFile, logger);
        settings.reload();
        settings.logSettings();

        BukkitAudiences audiences = BukkitAudiences.create(this);
        MessageHandler messageHandler = new MessageHandler(audiences, MiniMessage.miniMessage());
        YamlConfiguration messagesConfig = loadConfig("messages.yml");
        if (!loadAndSetDefaults("messages.yml", messagesConfig)) {
            logger.severe("Unable to load resource messages.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        messageHandler.loadMessages(messagesConfig);
        CopsAndRobbersEngine engine = new CopsAndRobbersEngine(this, settings, messageHandler);
        engine.initialize();

        BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.enableUnstableAPI("help");

        var conditions = commandManager.getCommandConditions();
        conditions.addCondition(GameSession.class, "isPlayer", (context, cmdContext, session) -> {
            if (session == null || !session.isPlayer(cmdContext.getPlayer())) {
                messageHandler.sendMessage(cmdContext.getPlayer(), Message.IN_GAME_ONLY_COMMAND, true);
                throw new ConditionFailedException();
            }
        });

        conditions.addCondition(Material.class, "nonAirBlock", (context, cmdContext, material) -> {
            if (material.isAir() || !material.isBlock()) {
                cmdContext.getIssuer().sendMessage(ChatColor.RED + "Material must be a non-air block");
                throw new InvalidCommandArgument();
            }
        });

        CommandCompletions<?> completions = commandManager.getCommandCompletions();
        completions.registerCompletion("nonAirBlockMaterial",
                context -> Arrays.stream(Material.values())
                        .filter(material -> material.isBlock() && !material.isAir())
                        .map(Object::toString).toList());

        completions.registerStaticCompletion("engineSetting", List.of(
                "max-game-duration",
                "cops-selection-delay",
                "door-vulnerability-chance",
                "door-vulnerability-interval",
                "door-vulnerability-duration",
                "door-malfunction-duration",
                "max-players",
                "min-players-two-cops",
                "min-players-three-cops",
                "win-material",
                "lobby-spawn"
        ));

        SessionManager sessionManager = engine.getSessionManager();
        BukkitCommandContexts contexts = (BukkitCommandContexts)commandManager.getCommandContexts();
        contexts.registerIssuerAwareContext(GameSession.class, new GameSessionContextResolver(sessionManager, messageHandler));

        commandManager.registerCommand(new CandrCommand(engine));
        commandManager.registerCommand(new CopsCommand(messageHandler));
        commandManager.registerCommand(new OpenCellsCommand(sessionManager, messageHandler));
        commandManager.registerCommand(new AdminItemCommand(engine));
        commandManager.registerCommand(new AdminHaltCommand(sessionManager, messageHandler));
        commandManager.registerCommand(new AdminStartCommand(sessionManager, messageHandler));
        commandManager.registerCommand(new AdminSettingsCommand(engine));

        PrisonInteractionsHandler prisonInteractionsHandler = new PrisonInteractionsHandler(sessionManager, messageHandler);
        PlayerInteractListener interactListener = new PlayerInteractListener(prisonInteractionsHandler);

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(interactListener, this);
        pluginManager.registerEvents(new PlayerJoinQuitListener(sessionManager), this);

        sessionManager.initializeTask();
    }

    @NotNull
    private YamlConfiguration loadConfig(@NotNull String path) {
        File file = new File(getDataFolder(), path);
        boolean fileNotExists = !file.exists();
        if (fileNotExists) {
            saveResource(path, false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.options().copyDefaults(true);
        return config;
    }

    private boolean loadAndSetDefaults(@NotNull String path, @NotNull YamlConfiguration config) {
        InputStream defaultsStream = getResource(path);
        if (defaultsStream == null) {
            return false;
        }

        try (InputStreamReader reader = new InputStreamReader(defaultsStream)) {
            YamlConfiguration messagesDefaults = YamlConfiguration.loadConfiguration(reader);
            config.setDefaults(messagesDefaults);
            return true;
        } catch (IOException err) {
            return false;
        }
    }

    @NotNull
    @Override
    public PluginCommand getCommand(@NotNull String name) throws IllegalArgumentException {
        PluginCommand command = super.getCommand(name);
        if (command != null) {
            return command;
        }
        logger.severe("Fatal exception! No command found with name '" + name + "' (this is most likely a developer error!)");
        throw new IllegalArgumentException();
    }

    @Override
    @NotNull
    public Logger getLogger() {
        return logger;
    }

}
