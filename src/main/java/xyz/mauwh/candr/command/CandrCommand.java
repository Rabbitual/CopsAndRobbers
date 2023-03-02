package xyz.mauwh.candr.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

public class CandrCommand implements CommandExecutor {

    private final CopsAndRobbersEngine engine;
    private final MessageHandler messageHandler;
    private final CandrJoinSubcommand joinSubcommand;

    public CandrCommand(@NotNull CopsAndRobbersEngine engine) {
        this.engine = engine;
        this.messageHandler = engine.getMessageHandler();
        this.joinSubcommand = new CandrJoinSubcommand(engine, messageHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!engine.isActive()) {
            messageHandler.sendMessage(sender, Message.ENGINE_IS_HALTED, true);
            return true;
        }

        if (!(sender instanceof Player)) {
            messageHandler.sendMessage(sender, Message.PLAYERS_ONLY_COMMAND, true);
            return true;
        }

        String[] ids = engine.getSessionIDsAsStringArray();
        String joinedIDs = String.join("|", ids);
        if (args.length == 0) {
            messageHandler.sendMessage(sender, Message.CANDR_COMMAND_USAGE, true, joinedIDs);
            return true;
        }

        Player player = (Player)sender;
        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "leave", "quit" -> executeLeaveCommand(player);
            case "join" -> joinSubcommand.execute(player, args);
            default -> messageHandler.sendMessage(sender, Message.CANDR_COMMAND_USAGE, true, joinedIDs);
        }

        return true;
    }

    /**
     * Leaves the game for the specified player, and announces their retirement if they are a cop
     * @param player - the player leaving the game
     */
    private void executeLeaveCommand(@NotNull Player player) {
        GameSession session = engine.getGameSession(player);
        if (session == null) {
            messageHandler.sendMessage(player, Message.GAME_DOES_NOT_EXIST, true);
            return;
        }

        session.teleportPlayerToLobby(player);
        int id = session.getRegion().getId();
        messageHandler.sendMessage(player, Message.LEFT_GAME, true, id);
        if (session.removeRobber(player)) {
            return;
        }

        boolean needsReplacementCop = session.removeCop(player) && !session.hasMaxAllowedCops();
        if (needsReplacementCop) {
            messageHandler.broadcast(Message.COP_RETIRED, true, id);
        }
    }

}
