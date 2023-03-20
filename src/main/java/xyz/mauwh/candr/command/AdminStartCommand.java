package xyz.mauwh.candr.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

@CommandAlias("candr")
@Subcommand("admin|a start|resume")
public class AdminStartCommand extends BaseCommand {

    private final CopsAndRobbersEngine engine;
    private final MessageHandler messageHandler;

    public AdminStartCommand(@NotNull CopsAndRobbersEngine engine) {
        this.engine = engine;
        this.messageHandler = engine.getMessageHandler();
    }

    @Subcommand("engine")
    @Description("Starts the CopsAndRobbers engine if it is not already active")
    public void onEngineStart(CommandSender sender) {
        if (!engine.isActive()) {
            engine.start();
            messageHandler.broadcast(Message.ENGINE_NOW_RESUMED, true);
        } else {
            messageHandler.sendMessage(sender, Message.ENGINE_NOT_HALTED, true);
        }
    }

    @Subcommand("session")
    @Syntax("[sessionId]")
    @Description("Starts the specified Cops and Robbers session if it is not already active")
    public void onSessionStart(CommandSender sender) {
        if (!engine.isActive()) {
            engine.start();
            messageHandler.broadcast(Message.SESSION_NOW_RESUMED, true);
        } else {
            messageHandler.sendMessage(sender, Message.SESSION_NOT_HALTED, true);
        }
    }

}
