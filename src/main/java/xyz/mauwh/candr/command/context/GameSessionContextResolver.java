package xyz.mauwh.candr.command.context;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

public class GameSessionContextResolver implements IssuerAwareContextResolver<GameSession, BukkitCommandExecutionContext> {

    private final CopsAndRobbersEngine engine;
    private final MessageHandler messageHandler;

    public GameSessionContextResolver(@NotNull CopsAndRobbersEngine engine) {
        this.engine = engine;
        this.messageHandler = engine.getMessageHandler();
    }

    @Override
    public GameSession getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        Player player = context.getPlayer();
        if (context.hasFlag("noArg")) {
            return engine.getGameSession(player);
        }

        String sessionArg = context.popFirstArg();
        GameSession session;
        try {
            int id = Integer.parseInt(sessionArg);
            session = engine.getSession(id);
        } catch (NumberFormatException err) {
            session = null;
        }

        if (session == null) {
            if (sessionArg != null) {
                messageHandler.sendMessage(player, Message.GAME_DOES_NOT_EXIST, true);
            }
            throw new InvalidCommandArgument(sessionArg != null);
        }

        return session;
    }

}
