package xyz.mauwh.candr.command.context;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.SessionManager;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

public class GameSessionContextResolver implements IssuerAwareContextResolver<GameSession, BukkitCommandExecutionContext> {

    private final SessionManager sessionManager;
    private final MessageHandler messageHandler;

    public GameSessionContextResolver(@NotNull SessionManager sessionManager, MessageHandler messageHandler) {
        this.sessionManager = sessionManager;
        this.messageHandler = messageHandler;
    }

    @Override
    public GameSession getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        Player player = context.getPlayer();
        if (context.hasFlag("noArg")) {
            return sessionManager.getSession(player);
        }

        String sessionArg = context.popFirstArg();
        GameSession session;
        try {
            int id = Integer.parseInt(sessionArg);
            session = sessionManager.getSession(id);
        } catch (NumberFormatException err) {
            session = null;
        }

        if (session == null) {
            if (sessionArg != null) {
                messageHandler.sendMessage(player, Message.GAME_DOES_NOT_EXIST, true);
            }
            throw new InvalidCommandArgument(sessionArg == null);
        }

        return session;
    }

}
