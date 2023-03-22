package xyz.mauwh.candr.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
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
        messageHandler.sendMessage(sender, Message.RELOADED_ENGINE_SETTINGS, true);
    }

}
