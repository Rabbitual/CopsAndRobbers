package xyz.mauwh.candr.engine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.CopsAndRobbersPlugin;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.PlayerState;
import xyz.mauwh.candr.game.SessionManager;
import xyz.mauwh.message.MessageHandler;

import java.util.Objects;

public class PrisonInteractionsHandler {

    private final CopsAndRobbersPlugin plugin;
    private final SessionManager sessionManager;
    private final MessageHandler messageHandler;

    public PrisonInteractionsHandler(@NotNull CopsAndRobbersPlugin plugin, @NotNull SessionManager sessionManager, @NotNull MessageHandler messageHandler) {
        this.plugin = plugin;
        this.sessionManager = sessionManager;
        this.messageHandler = messageHandler;
    }

    public void onBlockInteraction(PlayerInteractEvent event) {
        Block block = Objects.requireNonNull(event.getClickedBlock());
        Location location = block.getLocation();
        Action action = event.getAction();

        Player player = event.getPlayer();
        GameSession session = sessionManager.getSession(player);
        if (session == null) {
            return;
        }

        boolean isWinMaterial = block.getType() == session.getSettings().getWinMaterial();
        boolean isRobber = session.getPlayerState(player) != PlayerState.COP;
        boolean canWin = (isWinMaterial && isRobber) && action == Action.RIGHT_CLICK_BLOCK;
        if (canWin) {
            sessionManager.stop(session, player, true);
            sessionManager.start(session);
            return;
        }

        session.getRegion().getNodes().forEach(node -> {
            if (node.canBeUsed(session, player, location, action)) {
                Bukkit.getScheduler().runTask(plugin, () -> node.handle(session, player, messageHandler));
            }
        });
    }

}
