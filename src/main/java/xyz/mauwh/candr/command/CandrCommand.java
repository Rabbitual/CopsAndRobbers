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
        if (sessionManager.getSession(player) != null) {
            messageHandler.sendMessage(player, Message.ALREADY_IN_GAME, true);
            return;
        }
        sessionManager.onJoin(session, player);
    }

    @Subcommand("leave|quit")
    @Description("Leaves your current game of cops and robbers")
    @CommandPermission("copsandrobbers.candr")
    public void onLeave(Player player, @Conditions("isPlayer") @Flags("noArg") GameSession session) {
        sessionManager.onQuit(session, player);
    }

    @HelpCommand
    public void onHelp(@NotNull CommandSender sender, CommandHelp help) {
        sender.sendMessage(ChatColor.BLUE + "===== Cops and Robbers Help =====");
        help.showHelp();
    }

}
