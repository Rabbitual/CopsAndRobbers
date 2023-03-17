package xyz.mauwh.candr.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.game.DoorState;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.PlayerState;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

public class OpenCellsCommand implements CommandExecutor {

    private final CopsAndRobbersEngine engine;
    private final MessageHandler messageHandler;

    public OpenCellsCommand(@NotNull CopsAndRobbersEngine engine) {
        this.engine = engine;
        this.messageHandler = engine.getMessageHandler();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            messageHandler.sendMessage(sender, Message.PLAYERS_ONLY_COMMAND, true);
            return true;
        }

        Player player = (Player)sender;
        GameSession session = engine.getGameSession(player);
        if (session == null) {
            messageHandler.sendMessage(player, Message.IN_GAME_ONLY_COMMAND, true);
            return true;
        }

        if (session.getPlayerState(player) == PlayerState.COP) {
            messageHandler.sendMessage(player, Message.ROBBERS_ONLY_COMMAND, true);
        } else if (session.getDoorState() != DoorState.VULNERABLE) {
            messageHandler.sendMessage(player, Message.NOT_TIME_FOR_COMMAND, true);
        } else {
            session.malfunctionDoors();
        }

        return true;
    }

}
