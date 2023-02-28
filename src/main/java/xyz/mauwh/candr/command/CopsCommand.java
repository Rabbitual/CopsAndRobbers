package xyz.mauwh.candr.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
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
        Message message;
        if (session == null) {
            message = Message.IN_GAME_ONLY_COMMAND;
        } else if (session.isCop(player)) {
            message = Message.ALREADY_A_COP;
        } else if (session.hasMaxAllowedCops()) {
            message = Message.NOT_ACCEPTING_APPLICATIONS;
        } else if (!session.addCopApplicant(player)) {
            message = Message.ALREADY_APPLIED_FOR_COP;
        } else {
            message = Message.APPLIED_FOR_COP;
        }

        messageHandler.sendMessage(audience, message, true);
        return true;
    }

}
