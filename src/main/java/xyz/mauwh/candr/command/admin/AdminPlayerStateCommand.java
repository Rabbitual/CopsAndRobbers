package xyz.mauwh.candr.command.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.PlayerState;
import xyz.mauwh.candr.game.SessionManager;

@CommandAlias("candr")
@Subcommand("admin|a playerstate|player|ps")
@CommandPermission("copsandrobbers.admin.playerstate")
public class AdminPlayerStateCommand extends BaseCommand {

    private final SessionManager sessionManager;

    public AdminPlayerStateCommand(@NotNull SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Subcommand("get")
    public void onGet(CommandSender sender, Player other, GameSession session) {
        sender.sendMessage(ChatColor.YELLOW + other.getDisplayName() + "'s player state: " + session.getPlayerState(other) + "(session id: " + session.getId() + ")");
    }

    @Subcommand("set")
    public void onSet(CommandSender sender, @Flags("other") Player other, PlayerState playerState) {
        GameSession session = sessionManager.getSession(other);
        if (session == null) {
            sender.sendMessage(ChatColor.RED + "That player is not in a game");
            return;
        }

        PlayerState oldState = session.getPlayerState(other);
        session.setPlayerState(other, playerState);
        if (oldState != playerState) {
            sender.sendMessage(ChatColor.YELLOW + "Updated " + other.getDisplayName() + "'s player state in session #" + session.getId() + " (" + oldState + " -> " + playerState + ")");
        } else {
            sender.sendMessage(ChatColor.GRAY + "Player state did not change");
        }
    }

}
