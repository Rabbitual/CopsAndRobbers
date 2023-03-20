package xyz.mauwh.candr.engine;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.PlayerState;

import java.util.Objects;

public class PrisonInteractionsHandler {

    private final CopsAndRobbersEngine engine;

    public PrisonInteractionsHandler(@NotNull CopsAndRobbersEngine engine) {
        this.engine = engine;
    }

    public void onBlockInteraction(PlayerInteractEvent event) {
        Block block = Objects.requireNonNull(event.getClickedBlock());
        Location location = block.getLocation();
        Action action = event.getAction();

        Player player = event.getPlayer();
        GameSession session = engine.getGameSession(player);
        if (session == null) {
            return;
        }

        boolean isWinMaterial = block.getType() == session.getSettings().getWinMaterial();
        boolean isRobber = session.getPlayerState(player) != PlayerState.COP;
        boolean canWin = (isWinMaterial && isRobber) && action == Action.RIGHT_CLICK_BLOCK;
        if (canWin) {
            session.endGame(player, true);
            session.start();
            return;
        }

        session.getRegion().getNodes().forEach(node -> {
            if (node.canBeUsed(session, player, location, action)) {
                node.handle(session, player, engine.getMessageHandler());
            }
        });
    }

}
