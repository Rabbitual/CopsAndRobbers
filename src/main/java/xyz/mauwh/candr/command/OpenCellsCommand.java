package xyz.mauwh.candr.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.game.DoorState;
import xyz.mauwh.candr.game.GameSession;
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

        for (GameSession session : engine.getSessions().values()) {
            boolean isCop = session.isCop(player);
            boolean isRobber = session.isRobber(player);
            boolean doorsVulnerable = session.getDoorState() == DoorState.VULNERABLE;
            if (!isCop && !isRobber) {
                continue;
            }

            if (isCop) {
                Component message = messageHandler.getMessage(Message.ROBBERS_ONLY_COMMAND, true);
                senderAudience.sendMessage(message);
            } else if (!doorsVulnerable) {
                Component message = messageHandler.getMessage(Message.NOT_TIME_FOR_COMMAND, true);
                senderAudience.sendMessage(message);
            } else {
                session.malfunctionDoors();
            }
            return true;
        }

        senderAudience.sendMessage(Component.text("You have reached limbo?!", NamedTextColor.GRAY));
        return true;
    }

}
