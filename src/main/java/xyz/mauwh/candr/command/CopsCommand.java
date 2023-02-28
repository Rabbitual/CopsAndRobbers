package xyz.mauwh.candr.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

public class CopsCommand implements CommandExecutor {

    private final CopsAndRobbersEngine engine;
    private final MessageHandler messageHandler;

    public CopsCommand(@NotNull CopsAndRobbersEngine engine) {
        this.engine = engine;
        this.messageHandler = engine.getMessageHandler();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        BukkitAudiences audiences = messageHandler.getAudiences();
        Audience audience = audiences.sender(sender);
        if (!(sender instanceof Player)) {
            messageHandler.sendMessage(audience, Message.PLAYERS_ONLY_COMMAND, true);
            return true;
        }

        Player player = (Player)sender;
        GameSession session = engine.getGameSession(player);
        if (session == null) {
            messageHandler.sendMessage(audience, Message.IN_GAME_ONLY_COMMAND, true);
            return true;
        }

        Component prefix = messageHandler.getMessage(Message.PREFIX, false);
        if (session.isCop(player)) {
            // Todo: Add message - "<red>You are already a cop"
            Component message = Component.text("You are already a cop", NamedTextColor.RED);
            Component prefixed = Component.text().append(prefix).append(message).build();
            audience.sendMessage(prefixed);
            return true;
        }

        if (session.hasMaxAllowedCops()) {
            // Todo: Add message - "<red>New cop applications are not currently being accepted"
            Component message = Component.text("New cop applications are not currently being accepted", NamedTextColor.RED);
            Component prefixed = Component.text().append(prefix).append(message).build();
            audience.sendMessage(prefixed);
            return true;
        }

        if (!session.addCopApplicant(player)) {
            Component message = Component.text("You already applied to be a cop", NamedTextColor.RED);
            Component prefixed = Component.text().append(prefix).append(message).build();
            audience.sendMessage(prefixed);
            return true;
        }

        // Todo: Add message - "<blue>You have applied to be a cop"
        Component message = Component.text("You have applied to be a cop", NamedTextColor.BLUE);
        Component prefixed = Component.text().append(prefix).append(message).build();
        audience.sendMessage(prefixed);
        return true;
    }

}
