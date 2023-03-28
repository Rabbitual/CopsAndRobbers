package xyz.mauwh.candr.engine.ticker;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.configuration.EngineSettings;
import xyz.mauwh.candr.game.DoorState;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.PlayerState;
import xyz.mauwh.candr.game.SessionManager;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class EngineGameSessionTicker {

    private final SessionManager sessionManager;
    private final GameSession session;
    private final MessageHandler messageHandler;
    private EngineSettings settings;
    private int gameTick;
    private int doorVulnerabilityTick;
    private int doorMalfunctionTick;

    public EngineGameSessionTicker(@NotNull SessionManager sessionManager, @NotNull GameSession session, @NotNull MessageHandler messageHandler) {
        this.sessionManager = sessionManager;
        this.session = session;
        this.messageHandler = messageHandler;
        this.settings = session.getSettings();
    }

    public void reset() {
        settings = session.getSettings();
        gameTick = 0;
        doorVulnerabilityTick = 0;
        doorMalfunctionTick = 0;
    }

    /**
     * The main heartbeat of any individual game session
     */
    public void tick() {
        if (!sessionManager.isActive() || !session.isActive()) {
            return;
        }

        gameTick++;
        if (gameTick >= settings.getMaxGameDuration()) {
            sessionManager.stop(session, null, true);
            return;
        }

        if (shouldDoorsBecomeVulnerable()) {
            sessionManager.changeDoorState(session, DoorState.VULNERABLE);
            doorVulnerabilityTick = settings.getDoorVulnerabilityDuration();
            return;
        }

        DoorState doorState = session.getDoorState();
        if (doorState == DoorState.VULNERABLE && doorVulnerabilityTick-- == 0) {
            sessionManager.changeDoorState(session, DoorState.SECURE);
        } else if (doorState == DoorState.MALFUNCTIONING && doorMalfunctionTick-- == 0) {
            sessionManager.changeDoorState(session, DoorState.SECURE);
        }

        handleCopsSelection();
    }

    public void setDoorMalfunctionTimer() {
        doorMalfunctionTick = settings.getDoorMalfunctionDuration();
    }

    /**
     * Checks at a certain weighted chance if the doors may become vulnerable
     * @return true if the doors should become vulnerable
     */
    private boolean shouldDoorsBecomeVulnerable() {
        if (session.getDoorState() != DoorState.SECURE) {
            return false;
        } else if (gameTick % settings.getDoorVulnerabilityInterval() != 0) {
            return false;
        }

        double doorVulnerabilityChance = settings.getDoorVulnerabilityChance();
        double num = ThreadLocalRandom.current().nextDouble();
        return num > (1.0 - doorVulnerabilityChance);
    }

    /**
     * Handles initial cop selection. When the tick is equal to 0, cop applicants will be selected and made cops randomly
     * until the game can no longer have any. When less than 0, if a game needs more cops, then applicants will be made
     * cops automatically until the game can no longer have any.
     */
    private void handleCopsSelection() {
        if (gameTick < settings.getCopsSelectionDelay()) {
            return;
        }
        while (!session.getCopApplicants().isEmpty() && !session.hasMaxAllowedCops()) {
            Player player = Objects.requireNonNull(selectRandomNewCop());
            session.setPlayerState(player, PlayerState.COP);
            BukkitAudiences audiences = messageHandler.getAudiences();
            messageHandler.sendMessage(audiences.all(), Message.FIRST_COP_SELECTED, true, player.getDisplayName(), session.getId());
        }
    }

    private Player selectRandomNewCop() {
        List<UUID> copApplicants = session.getCopApplicants();
        if (copApplicants.isEmpty()) {
            return null;
        }

        Random random = ThreadLocalRandom.current();
        int size = copApplicants.size();
        int index = random.nextInt(size);
        UUID uuid = copApplicants.get(index);
        Player newCop = Bukkit.getPlayer(uuid);
        Objects.requireNonNull(newCop, "Attempted to select null as new cop");

        session.setPlayerState(newCop, PlayerState.COP);
        session.removeCopApplicant(newCop);
        session.getSettings().getCopItems().forEach(newCop.getInventory()::addItem);

        Location mainRoom = session.getRegion().getCopSpawnPoint();
        newCop.teleport(mainRoom);
        return newCop;
    }

}
