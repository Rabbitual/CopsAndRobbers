package xyz.mauwh.candr.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerInteractListener implements Listener {

    private final PrisonInteractionsHandler prisonInteractionsHandler;

    public PlayerInteractListener(@NotNull PrisonInteractionsHandler prisonInteractionsHandler) {
        this.prisonInteractionsHandler = prisonInteractionsHandler;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasBlock()) {
            prisonInteractionsHandler.onBlockInteraction(event);
        }
    }

}
