package xyz.mauwh.candr.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.game.DoorState;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.PlayerState;
import xyz.mauwh.candr.game.SessionManager;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

@CommandAlias("open")
public class OpenCellsCommand extends BaseCommand {

    private final SessionManager sessionManager;
    private final MessageHandler messageHandler;

    public OpenCellsCommand(@NotNull SessionManager sessionManager, @NotNull MessageHandler messageHandler) {
        this.sessionManager = sessionManager;
        this.messageHandler = messageHandler;
    }

    @Subcommand("cells")
    @CommandPermission("copsandrobbers.opencells")
    @Description("Opens the cell doors in your jail when they are vulnerable, for robbers")
    public void onOpenCells(Player player, @Conditions("isPlayer") @Flags("noArg") GameSession session) {
        if (session.getPlayerState(player) == PlayerState.COP) {
            messageHandler.sendMessage(player, Message.ROBBERS_ONLY_COMMAND, true);
        } else if (session.getDoorState() != DoorState.VULNERABLE) {
            messageHandler.sendMessage(player, Message.NOT_TIME_FOR_COMMAND, true);
        } else {
            sessionManager.changeDoorState(session, DoorState.MALFUNCTIONING);
        }
    }

}
