package xyz.mauwh.candr.command.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.SessionManager;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

@CommandAlias("candr")
@Subcommand("admin|a start|resume")
public class AdminStartCommand extends BaseCommand {

    private final SessionManager sessionManager;
    private final MessageHandler messageHandler;

    public AdminStartCommand(@NotNull SessionManager sessionManager, @NotNull MessageHandler messageHandler) {
        this.sessionManager = sessionManager;
        this.messageHandler = messageHandler;
    }

    @Subcommand("engine")
    @CommandPermission("copsandrobbers.admin.start")
    @Description("Starts the CopsAndRobbers engine if it is not already active")
    public void onEngineStart(CommandSender sender) {
        if (!sessionManager.isActive()) {
            sessionManager.initializeTask();
            messageHandler.broadcast(Message.ENGINE_NOW_RESUMED, true);
        } else {
            messageHandler.sendMessage(sender, Message.ENGINE_NOT_HALTED, true);
        }
    }

    @Subcommand("session")
    @Syntax("[sessionId]")
    @CommandPermission("copsandrobbers.admin.start")
    @Description("Starts the specified Cops and Robbers session if it is not already active")
    public void onSessionStart(CommandSender sender, GameSession session) {
        if (!sessionManager.isActive()) {
            messageHandler.sendMessage(sender, Message.ENGINE_IS_HALTED, true);
        } else if (!session.isActive()) {
            sessionManager.start(session);
            messageHandler.broadcast(Message.SESSION_NOW_RESUMED, true);
        } else {
            messageHandler.sendMessage(sender, Message.SESSION_NOT_HALTED, true);
        }
    }

}
