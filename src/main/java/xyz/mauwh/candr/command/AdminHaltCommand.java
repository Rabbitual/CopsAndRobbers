package xyz.mauwh.candr.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
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
    @CommandPermission("candr.admin.halt")
    public void onHaltEngine(CommandSender sender) {
        if (engine.isActive()) {
            engine.halt();
            messageHandler.broadcast(Message.ENGINE_NOW_HALTED, true);
        } else {
            messageHandler.sendMessage(sender, Message.ENGINE_ALREADY_HALTED, true);
        }
    }

    @Subcommand("session")
    @CommandPermission("candr.admin.halt")
    public void onHaltSession(CommandSender sender, GameSession session) {
        if (engine.isActive() && session.isActive()) {
            session.endGame(null, true);
            messageHandler.broadcast(Message.SESSION_NOW_HALTED, true, session.getRegion().getId());
        } else {
            messageHandler.sendMessage(sender, Message.SESSION_ALREADY_HALTED, true);
        }
    }

}