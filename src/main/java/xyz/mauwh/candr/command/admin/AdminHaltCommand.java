package xyz.mauwh.candr.command.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

@CommandAlias("candr")
@Subcommand("admin|a halt|stop")
public class AdminHaltCommand extends BaseCommand {

    private final CopsAndRobbersEngine engine;
    private final MessageHandler messageHandler;

    public AdminHaltCommand(@NotNull CopsAndRobbersEngine engine) {
        this.engine = engine;
        this.messageHandler = engine.getMessageHandler();
    }

    @Subcommand("engine")
    @CommandPermission("copsandrobbers.admin.halt")
    @Description("Halts the CopsAndRobbers engine, forcefully stopping all sessions until manually resumed")
    public void onHaltEngine(CommandSender sender) {
        if (engine.isActive()) {
            engine.halt();
            messageHandler.broadcast(Message.ENGINE_NOW_HALTED, true);
        } else {
            messageHandler.sendMessage(sender, Message.ENGINE_ALREADY_HALTED, true);
        }
    }

    @Subcommand("session")
    @CommandPermission("copsandrobbers.admin.halt")
    @Syntax("[sessionId]")
    @Description("Halts the specified Cops and Robbers session, forcefully stopping it until manually resumed")
    public void onHaltSession(CommandSender sender, GameSession session) {
        if (engine.isActive() && session.isActive()) {
            session.endGame(null, true);
            messageHandler.broadcast(Message.SESSION_NOW_HALTED, true, session.getRegion().getId());
        } else {
            messageHandler.sendMessage(sender, Message.SESSION_ALREADY_HALTED, true);
        }
    }

}
