package xyz.mauwh.candr.command;

import net.kyori.adventure.audience.Audience;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

import java.util.Collection;

public class OpenCellsCommand implements CommandExecutor {

    private final CopsAndRobbersEngine engine;
    private final MessageHandler messageHandler;

    public OpenCellsCommand(@NotNull CopsAndRobbersEngine engine) {
        this.engine = engine;
        this.messageHandler = engine.getMessageHandler();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Audience senderAudience = messageHandler.getAudiences().sender(sender);
        if (!(sender instanceof Player)) {
            senderAudience.sendMessage(messageHandler.getMessage(Message.PLAYERS_ONLY_COMMAND, true));
            return true;
        }

        Player player = (Player)sender;
        if (!engine.isPlayer(player)) {
            senderAudience.sendMessage(messageHandler.getMessage(Message.IN_GAME_ONLY_COMMAND, true));
            return true;
        }

        Collection<GameSession> sessions = engine.getSessions().values();
        for (GameSession session : sessions) {
            if (session.isCop(player)) {
                senderAudience.sendMessage(messageHandler.getMessage(Message.ROBBERS_ONLY_COMMAND, true));
            } else if (session.isRobber(player)) {
                session.malfunctionDoors();
            } else {
                senderAudience.sendMessage(messageHandler.getMessage(Message.NOT_TIME_FOR_COMMAND, true));
            }
        }
        return true;
    }

}
