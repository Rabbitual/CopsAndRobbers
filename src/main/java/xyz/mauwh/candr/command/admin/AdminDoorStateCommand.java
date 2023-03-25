package xyz.mauwh.candr.command.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.game.DoorState;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.SessionManager;

@CommandAlias("candr")
@Subcommand("admin|a doorstate|door|ds")
@CommandPermission("copsandrobbers.admin.doorstate")
public class AdminDoorStateCommand extends BaseCommand {

    private final SessionManager sessionManager;

    public AdminDoorStateCommand(@NotNull SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Subcommand("get")
    public void onGet(CommandSender sender, GameSession session) {
        sender.sendMessage(ChatColor.YELLOW + "Session " + session.getId() + "'s door state: " + session.getDoorState());
    }

    @Subcommand("set")
    public void onSet(CommandSender sender, GameSession session, DoorState doorState) {
        DoorState oldState = session.getDoorState();
        sessionManager.changeDoorState(session, doorState);
        if (oldState != doorState) {
            sender.sendMessage(ChatColor.YELLOW + "Updated session's door state (" + oldState + " -> " + doorState + ")");
        } else {
            sender.sendMessage(ChatColor.GRAY + "Door state did not change");
        }
    }

}
