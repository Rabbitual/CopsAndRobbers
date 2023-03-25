package xyz.mauwh.candr.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.SessionManager;

public class PlayerJoinQuitListener implements Listener {

    private final SessionManager sessionManager;

    public PlayerJoinQuitListener(@NotNull SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getInventory().clear();
        sessionManager.teleportToLobby(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GameSession session = sessionManager.getSession(player);
        if (session != null) {
            sessionManager.onQuit(session, player);
        }
    }

}
