package xyz.mauwh.candr;

import org.bukkit.plugin.PluginLogger;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static xyz.mauwh.message.ColoredConsoleStringBuilder.builder;

public class CopsAndRobbersLogger extends PluginLogger {

    private final String pluginName;

    protected CopsAndRobbersLogger(@NotNull CopsAndRobbersPlugin plugin) {
        super(plugin);
        String prefix = plugin.getDescription().getPrefix();
        prefix = prefix != null ? prefix : plugin.getDescription().getName();
        pluginName = "[" + prefix + "] ";
    }

    @Override
    public void log(@NotNull LogRecord logRecord) {
        Level level = logRecord.getLevel();
        String pluginPrefix = "[" + pluginName + "] ";
        if (level == Level.SEVERE) {
            String formatted = builder().red(pluginPrefix + logRecord.getMessage()).toString();
            logRecord.setMessage(formatted);
        } else if (level == Level.WARNING) {
            String formatted = builder().yellow(pluginPrefix + logRecord.getMessage()).toString();
            logRecord.setMessage(formatted);
        } else {
            super.log(logRecord);
        }
    }

}
