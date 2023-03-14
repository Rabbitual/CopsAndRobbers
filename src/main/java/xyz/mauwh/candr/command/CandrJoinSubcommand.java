package xyz.mauwh.candr.command;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

public class CandrJoinSubcommand {

    private final CopsAndRobbersEngine engine;
    private final MessageHandler messageHandler;

    public CandrJoinSubcommand(@NotNull CopsAndRobbersEngine engine, @NotNull MessageHandler messageHandler) {
        this.engine = engine;
        this.messageHandler = messageHandler;
    }

    /**
     * Handles game join logic for the provided player and with the specified command arguments
     * @param player - the player issuing this command
     * @param args - the arguments this command was issued with
     */
    public void execute(@NotNull Player player, @NotNull String[] args) {
        String[] ids = engine.getSessionIDsAsStringArray();
        String joinedIDs = String.join("|", ids);
        if (args.length < 2) {
            messageHandler.sendMessage(player, Message.CANDR_COMMAND_USAGE, true, joinedIDs);
            return;
        }

        String strId = args[1];
        GameSession session = null;
        if (NumberUtils.isDigits(strId)) {
            int id = Integer.parseInt(strId);
            session = engine.getSession(id);
        }

        if (session == null) {
            messageHandler.sendMessage(player, Message.GAME_DOES_NOT_EXIST, true);
            return;
        } else if (!session.addRobber(player)) {
            messageHandler.sendMessage(player, Message.ALREADY_IN_GAME, true);
            return;
        }

        session.teleportRobberToCell(player);
        messageHandler.sendMessage(player, Message.JOINED_GAME, true, strId);
        if (!session.hasMaxAllowedCops()) {
            messageHandler.sendMessage(player, Message.JAIL_COULD_USE_COPS, true);
        }
    }

}
