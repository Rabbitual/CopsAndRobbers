package xyz.mauwh.candr.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.PlayerState;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

@CommandAlias("candr")
public class CandrCommand extends BaseCommand {

    private final MessageHandler messageHandler;

    public CandrCommand(@NotNull MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Subcommand("join")
    @Description("Joins the specified game of cops and robbers if it is active")
    @Syntax("<gameId>")
    @CommandPermission("copsandrobbers.candr.join")
    public void onJoin(CommandSender sender, GameSession session) {
        if (!(sender instanceof Player)) {
            messageHandler.sendMessage(sender, Message.PLAYERS_ONLY_COMMAND, true);
            return;
        }

        Player player = (Player)sender;
        session.setPlayerState(player, PlayerState.ROBBER);
        session.teleportRobberToCell(player);
        messageHandler.sendMessage(sender, Message.JOINED_GAME, true, session.getRegion().getId());
        if (!session.hasMaxAllowedCops()) {
            messageHandler.sendMessage(sender, Message.JAIL_COULD_USE_COPS, true);
        }
    }

    @Subcommand("leave|quit")
    @Description("Leaves your current game of cops and robbers")
    @CommandPermission("copsandrobbers.candr.leave")
    @Syntax("[]")
    public void onLeave(Player player, @Conditions("isPlayer") @Flags("noArg") GameSession session) {
        if (session == null) {
            messageHandler.sendMessage(player, Message.IN_GAME_ONLY_COMMAND, true);
            return;
        }

        session.teleportPlayerToLobby(player);
        int id = session.getRegion().getId();
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
