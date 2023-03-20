package xyz.mauwh.candr.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Flags;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.PlayerState;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

public class CopsCommand extends BaseCommand {

    private final MessageHandler messageHandler;

    public CopsCommand(@NotNull MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @CommandAlias("cops")
    public void onCops(@NotNull Player player, @Flags("noArg") GameSession session) {
        Message message;
        if (session == null) {
            message = Message.IN_GAME_ONLY_COMMAND;
        } else if (session.getPlayerState(player) == PlayerState.COP) {
            message = Message.ALREADY_A_COP;
        } else if (session.hasMaxAllowedCops()) {
            message = Message.NOT_ACCEPTING_APPLICATIONS;
        } else if (session.addCopApplicant(player)) {
            message = Message.APPLIED_FOR_COP;
        } else {
            message = Message.ALREADY_APPLIED_FOR_COP;
        }

        messageHandler.sendMessage(player, message, true);
    }

}
