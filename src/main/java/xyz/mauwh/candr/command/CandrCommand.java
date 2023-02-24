package xyz.mauwh.candr.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
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
        BukkitAudiences audiences = messageHandler.getAudiences();
        Audience senderAudience = audiences.sender(sender);
        if (!engine.isActive()) {
            Component engineHaltedMessage = messageHandler.getMessage(Message.ENGINE_IS_HALTED, true);
            senderAudience.sendMessage(engineHaltedMessage);
            return true;
        }

        if (!(sender instanceof Player)) {
            Component message = messageHandler.getMessage(Message.PLAYERS_ONLY_COMMAND, true);
            senderAudience.sendMessage(message);
            return true;
        }

        String[] ids = engine.getSessionIDsAsStringArray();
        Component helpMessage = messageHandler.getMessage(Message.CANDR_COMMAND_USAGE, true, String.join("|", ids));
        if (args.length == 0) {
            senderAudience.sendMessage(helpMessage);
            return true;
        }

        Player player = (Player)sender;
        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "leave", "quit" -> executeLeaveCommand(player);
            case "join" -> joinSubcommand.execute(player, args);
            default -> senderAudience.sendMessage(helpMessage);
        }

        return true;
    }

    /**
     * Leaves the game for the specified player, and announces their retirement if they are a cop
     * @param player - the player leaving the game
     */
    private void executeLeaveCommand(@NotNull Player player) {
        BukkitAudiences audiences = messageHandler.getAudiences();
        for (GameSession session : engine.getSessions().values()) {
            if (!session.isPlayer(player)) {
                continue;
            }

            session.teleportPlayerToLobby(player);
            Audience playerAudience = audiences.player(player);
            Component leftGameMessage = messageHandler.getMessage(Message.LEFT_GAME, true, session.getRegion().getId());
            playerAudience.sendMessage(leftGameMessage);
            if (session.removeRobber(player)) {
                return;
            }

            boolean needsReplacementCop = session.removeCop(player) && !session.hasMaxAllowedCops();
            if (!needsReplacementCop) {
                return;
            }

            int id = session.getRegion().getId();
            Component copRetiredMessage = messageHandler.getMessage(Message.COP_RETIRED, true, id);
            audiences.all().sendMessage(copRetiredMessage);
        }
    }

}
