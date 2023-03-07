package xyz.mauwh.candr.listener;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.region.RegionNode;
//import xyz.mauwh.candr.region.debug.DebugAccessNode;
//import xyz.mauwh.candr.region.debug.DebugTeleportNode;
import xyz.mauwh.message.MessageHandler;

//import java.util.ArrayList;
//import java.util.List;

public class PrisonAccessHandler implements Listener {

    private final CopsAndRobbersEngine engine;

    public PrisonAccessHandler(@NotNull CopsAndRobbersEngine engine) {
        this.engine = engine;
    }

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Player player = event.getPlayer();
        GameSession session = engine.getGameSession(player);
        if (session == null) {
            return;
        }

        Location location = block.getLocation();
        Action action = event.getAction();
        session.getRegion().getNodes().forEach(node -> {
            player.sendMessage(ChatColor.YELLOW + "testing...");
            if (node.canBeUsed(session, player, location, action)) {
                player.sendMessage(ChatColor.GREEN + "true");
                node.handle(session, player, engine.getMessageHandler());
            }
        });
    }

}
