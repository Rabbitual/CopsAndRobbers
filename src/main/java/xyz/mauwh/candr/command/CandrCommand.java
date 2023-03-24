package xyz.mauwh.candr.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.PlayerState;
import xyz.mauwh.candr.game.SessionManager;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

@CommandAlias("candr")
public class CandrCommand extends BaseCommand {

    private final SessionManager sessionManager;
    private final MessageHandler messageHandler;

    public CandrCommand(@NotNull CopsAndRobbersEngine engine) {
        this.sessionManager = engine.getSessionManager();
        this.messageHandler = engine.getMessageHandler();
    }

    @Subcommand("join")
    @Description("Joins the specified game of cops and robbers if it is active")
    @Syntax("<gameId>")
    @CommandPermission("copsandrobbers.candr")
    public void onJoin(Player player, GameSession session) {
        session.setPlayerState(player, PlayerState.ROBBER);
        sessionManager.teleportToRandomCell(session.getRegion(), player);
        messageHandler.sendMessage(player, Message.JOINED_GAME, true, session.getId());
        if (!session.hasMaxAllowedCops()) {
            messageHandler.sendMessage(player, Message.JAIL_COULD_USE_COPS, true);
        }
    }

    @Subcommand("leave|quit")
    @Description("Leaves your current game of cops and robbers")
    @CommandPermission("copsandrobbers.candr")
    public void onLeave(Player player, @Conditions("isPlayer") @Flags("noArg") GameSession session) {
        sessionManager.teleportToLobby(player);
        int id = session.getId();
        messageHandler.sendMessage(player, Message.LEFT_GAME, true, id);

        boolean wasRobber = session.getPlayerState(player).isRobber();
        session.removePlayer(player);
        if (wasRobber) {
            return;
        }

        if (!session.hasMaxAllowedCops()) {
            messageHandler.broadcast(Message.COP_RETIRED, true, id);
        }
    }

    @HelpCommand
    public void onHelp(@NotNull CommandSender sender, CommandHelp help) {
        sender.sendMessage(ChatColor.BLUE + "===== Cops and Robbers Help =====");
        help.showHelp();
    }

}
