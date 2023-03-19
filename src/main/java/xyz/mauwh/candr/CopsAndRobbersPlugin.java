package xyz.mauwh.candr;

import co.aikar.commands.*;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.command.CandrCommand;
import xyz.mauwh.candr.command.CopsCommand;
import xyz.mauwh.candr.command.OpenCellsCommand;
import xyz.mauwh.candr.command.context.GameSessionContextResolver;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.engine.configuration.EngineSettings;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.listener.PlayerInteractListener;
import xyz.mauwh.candr.listener.PrisonInteractionsHandler;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

import java.io.*;
import java.util.logging.Logger;

public final class CopsAndRobbersPlugin extends JavaPlugin {

    private final Logger logger;

    public CopsAndRobbersPlugin() {
        this.logger = new CopsAndRobbersLogger(this);
    }

    @Override
    public void onEnable() {
        EngineSettings settings = new EngineSettings(logger);
        YamlConfiguration config = (YamlConfiguration)getConfig();
        settings.load(config);

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

        getCommand("open").setExecutor(new OpenCellsCommand(engine));

        BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.enableUnstableAPI("help");

        var conditions = commandManager.getCommandConditions();
        conditions.addCondition(GameSession.class, "isPlayer", (context, cmdContext, session) -> {
            if (session == null || !session.isPlayer(cmdContext.getPlayer())) {
                messageHandler.sendMessage(cmdContext.getPlayer(), Message.IN_GAME_ONLY_COMMAND, true);
                throw new ConditionFailedException();
            }
        });

        BukkitCommandContexts contexts = (BukkitCommandContexts)commandManager.getCommandContexts();
        contexts.registerIssuerOnlyContext(ItemStack.class, context -> context.getPlayer().getInventory().getItemInMainHand());
        contexts.registerIssuerAwareContext(GameSession.class, new GameSessionContextResolver(engine));

        commandManager.registerCommand(new CandrCommand(messageHandler));
        commandManager.registerCommand(new CopsCommand(messageHandler));

        PrisonInteractionsHandler prisonInteractionsHandler = new PrisonInteractionsHandler(engine);
        PlayerInteractListener listener = new PlayerInteractListener(prisonInteractionsHandler);
        getServer().getPluginManager().registerEvents(listener, this);
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
