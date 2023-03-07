package xyz.mauwh.candr;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.command.CandrCommand;
import xyz.mauwh.candr.command.CopsCommand;
import xyz.mauwh.candr.command.OpenCellsCommand;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.engine.configuration.EngineSettings;
import xyz.mauwh.candr.listener.PrisonAccessHandler;
import xyz.mauwh.message.MessageHandler;

import java.io.*;
import java.util.logging.Level;

import static xyz.mauwh.message.ColoredConsoleStringBuilder.builder;

public final class CopsAndRobbersPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        EngineSettings settings = new EngineSettings(getLogger());
        YamlConfiguration config = (YamlConfiguration)getConfig();
        loadAndSetDefaults("config.yml", config);
        settings.load(config, true);

        BukkitAudiences audiences = BukkitAudiences.create(this);
        MessageHandler messageHandler = new MessageHandler(audiences, MiniMessage.miniMessage());
        YamlConfiguration messagesConfig = loadConfig("messages.yml");
        if (!loadAndSetDefaults("messages.yml", messagesConfig)) {
            builder().red("Unable to load resource messages.yml").post(getLogger(), Level.SEVERE);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        messageHandler.loadMessages(messagesConfig);
        CopsAndRobbersEngine engine = new CopsAndRobbersEngine(this, settings, messageHandler);
        engine.initialize();

        getCommand("candr").setExecutor(new CandrCommand(engine));
        getCommand("cops").setExecutor(new CopsCommand(engine));
        getCommand("open").setExecutor(new OpenCellsCommand(engine));

        getServer().getPluginManager().registerEvents(new PrisonAccessHandler(engine), this);
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
        builder().red("Fatal exception! No command found with name '" + name + "' (this is most likely a developer error!)").post(getLogger(), Level.SEVERE);
        throw new IllegalArgumentException();
    }

}
