package xyz.mauwh.candr.command.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.engine.configuration.EngineSettings;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

@CommandAlias("candr")
@Subcommand("admin|a settings")
public class AdminSettingsCommand extends BaseCommand {

    private final CopsAndRobbersEngine engine;
    private final MessageHandler messageHandler;

    public AdminSettingsCommand(@NotNull CopsAndRobbersEngine engine) {
        this.engine = engine;
        this.messageHandler = engine.getMessageHandler();
    }

    @Subcommand("set")
    @CommandPermission("copsandrobbers.admin.settings.set")
    @CommandCompletion("@engineSetting")
    @Syntax("<setting> [value]")
    @Description("Sets a new value for the specified CopsAndRobbers engine setting")
    public void onSet(CommandSender sender, String setting, Number value) {
        EngineSettings settings = engine.getSettings();
        switch (setting.toLowerCase()) {
            case "max-game-duration" -> settings.setMaxGameDuration(value.intValue());
            case "max-players" -> settings.setMaxPlayers(value.intValue());
            case "cops-selection-delay" -> settings.setCopsSelectionDelay(value.intValue());
            case "min-players-two-cops" -> settings.setMinPlayersTwoCops(value.intValue());
            case "min-players-three-cops" -> settings.setMinPlayersThreeCops(value.intValue());
            case "door-vulnerability-chance" -> settings.setDoorVulnerabilityChance(value.doubleValue());
            case "door-vulnerability-interval" -> settings.setDoorVulnerabilityInterval(value.intValue());
            case "door-vulnerability-duration" -> settings.setDoorVulnerabilityDuration(value.intValue());
            case "door-malfunction-duration" -> settings.setDoorMalfunctionDuration(value.intValue());
        }
        settings.save();
        sender.sendMessage(String.format(ChatColor.YELLOW + "Updated config.yml with new settings (%s: %s)", setting, value));
    }

    @Subcommand("set lobby-spawn")
    @CommandPermission("copsandrobbers.admin.settings.set")
    @Description("Sets a new value for the specified CopsAndRobbers engine setting")
    public void onSet(CommandSender sender, Location location) {
        EngineSettings settings = engine.getSettings();
        settings.setLobbySpawn(location);
        settings.save();
        sender.sendMessage(ChatColor.YELLOW + "Updated config.yml with new settings");
    }

    @Subcommand("set win-material")
    @CommandPermission("copsandrobbers.admin.settings.set")
    @CommandCompletion("@nonAirBlockMaterial")
    @Description("Sets a new value for the specified CopsAndRobbers engine setting")
    public void onSet(CommandSender sender, @Conditions("nonAirBlock") Material material) {
        EngineSettings settings = engine.getSettings();
        settings.setWinMaterial(material);
        settings.save();
        sender.sendMessage(ChatColor.YELLOW + "Updated config.yml with new settings (win-material: " + material + ")");
    }

    @Subcommand("save")
    @CommandPermission("copsandrobbers.admin.settings.save")
    @Description("Saves any changes made in-game to the CopsAndRobbers engine's settings")
    public void onSave(CommandSender sender) {
        engine.getSettings().save();
        messageHandler.sendMessage(sender, Message.SAVED_ENGINE_SETTINGS, true);
    }

    @Subcommand("reload")
    @CommandPermission("copsandrobbers.admin.settings.reload")
    @Description("Reloads all settings for the CopsAndRobbers engine from file")
    public void onReload(CommandSender sender) {
        engine.getSettings().reload();
        engine.getSettings().logSettings();
        messageHandler.sendMessage(sender, Message.RELOADED_ENGINE_SETTINGS, true);
    }

}
