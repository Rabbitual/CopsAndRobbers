package xyz.mauwh.candr;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.command.CandrCommand;
import xyz.mauwh.candr.command.OpenCellsCommand;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.engine.configuration.EngineSettings;
import xyz.mauwh.message.MessageHandler;

import java.io.*;
import java.util.Objects;
import java.util.logging.Level;

import static xyz.mauwh.message.ColoredConsoleStringBuilder.builder;

public final class CopsAndRobbersPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        EngineSettings settings = new EngineSettings(getLogger());
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        settings.load((YamlConfiguration)getConfig(), true);

        BukkitAudiences audiences = BukkitAudiences.create(this);
        MessageHandler messageHandler = new MessageHandler(audiences, MiniMessage.miniMessage());

        YamlConfiguration messagesConfig = loadMessagesConfig();
        if (!loadAndSetDefaultMessages(messagesConfig)) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        messageHandler.loadMessages(messagesConfig);

        CopsAndRobbersEngine engine = new CopsAndRobbersEngine(this, settings, messageHandler);

        getCommand("candr").setExecutor(new CandrCommand(engine));
        getCommand("open").setExecutor(new OpenCellsCommand(engine));

        engine.initialize();
    }

    @NotNull
    private YamlConfiguration loadMessagesConfig() {
        File file = new File(getDataFolder(), "messages.yml");
        if (!file.exists()) {
            saveResource("messages.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.options().copyDefaults(true);
        return config;
    }

    private boolean loadAndSetDefaultMessages(@NotNull YamlConfiguration config) {
        YamlConfiguration messagesDefaults;
        try (InputStream defaultsStream = getResource("messages.yml")) {
            Objects.requireNonNull(defaultsStream, "null messages.yml resource (this is most likely a developer error)");
            try (InputStreamReader reader = new InputStreamReader(defaultsStream)) {
                messagesDefaults = YamlConfiguration.loadConfiguration(reader);
                config.setDefaults(messagesDefaults);
                return true;
            }
        } catch (IOException err) {
            builder().red("Fatal: Unable to load default messages").post(getLogger(), Level.SEVERE);
            err.printStackTrace();
            return false;
        }
    }

    @NotNull
    @Override
    public PluginCommand getCommand(@NotNull String name) throws NullPointerException {
        PluginCommand command = super.getCommand(name);
        if (command != null) {
            return command;
        }
        builder().red("Fatal exception! No command found with name '" + name + "' (this is most likely a developer error!)").reset().post(getLogger(), Level.SEVERE);
        throw new NullPointerException("null command");
    }

}
