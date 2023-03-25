package xyz.mauwh.candr.command.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.SessionManager;

@CommandAlias("candr")
@Subcommand("admin|a")
public class AdminKickCommand extends BaseCommand {

    private final SessionManager sessionManager;

    public AdminKickCommand(@NotNull SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Subcommand("kick")
    @CommandPermission("copsandrobbers.admin.kick")
    public void onKick(CommandIssuer issuer, OnlinePlayer targetWrapper, @Optional String reason) {
        Player target = targetWrapper.getPlayer();
        GameSession session = sessionManager.getSession(target);
        if (session == null) {
            issuer.sendMessage(ChatColor.RED + "That player is not currently in a game");
            return;
        }

        sessionManager.onQuit(session, target);
        target.sendMessage(ChatColor.RED + "You were kicked from the game by an administrator");
        if (reason != null) {
            target.sendMessage(ChatColor.RED + "Cited reason: '" + reason + "'");
        }
    }

}
