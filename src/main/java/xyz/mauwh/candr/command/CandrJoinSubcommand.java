package xyz.mauwh.candr.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

public class CandrJoinSubcommand {

    private final CopsAndRobbersEngine engine;
    private final MessageHandler messageHandler;
    private final BukkitAudiences audiences;

    public CandrJoinSubcommand(@NotNull CopsAndRobbersEngine engine, @NotNull MessageHandler messageHandler) {
        this.engine = engine;
        this.messageHandler = messageHandler;
        this.audiences = messageHandler.getAudiences();
    }

    /**
     * Handles game join logic for the provided player and with the specified command arguments
     * @param player - the player issuing this command
     * @param args - the arguments this command was issued with
     */
    public void execute(@NotNull Player player, @NotNull String[] args) {
        Audience playerAudience = audiences.player(player);
        String[] ids = engine.getSessionIDsAsStringArray();
        String joinedIDs = String.join("|", ids);
        Component usage = messageHandler.getMessage(Message.CANDR_COMMAND_USAGE, true, joinedIDs);

        if (args.length < 2) {
            playerAudience.sendMessage(usage);
            return;
        }

        String strId = args[1];
        GameSession session;
        try {
            int id = Integer.parseInt(strId);
            session = engine.getSession(id);
        } catch (IllegalArgumentException err) {
            session = null;
        }

        if (session == null) {
            Component noGameMessage = messageHandler.getMessage(Message.GAME_DOES_NOT_EXIST, true);
            playerAudience.sendMessage(noGameMessage);
            playerAudience.sendMessage(usage);
            return;
        }

        if (!session.addRobber(player)) {
            Component fullGameMessage = messageHandler.getMessage(Message.GAME_CURRENTLY_FULL, true);
            playerAudience.sendMessage(fullGameMessage);
            return;
        }

        session.teleportRobberToCell(player);
        Component message = messageHandler.getMessage(Message.JOINED_GAME, true, strId);
        playerAudience.sendMessage(message);
        if (session.hasMaxAllowedCops()) {
            return;
        }

        Component copsMessage = messageHandler.getMessage(Message.JAIL_COULD_USE_COPS, true);
        playerAudience.sendMessage(copsMessage);
    }

}
