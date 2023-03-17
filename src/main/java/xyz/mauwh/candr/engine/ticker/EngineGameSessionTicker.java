package xyz.mauwh.candr.engine.ticker;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.configuration.EngineSettings;
import xyz.mauwh.candr.game.DoorState;
import xyz.mauwh.candr.game.GameSession;
import xyz.mauwh.candr.game.PlayerState;
import xyz.mauwh.message.Message;
import xyz.mauwh.message.MessageHandler;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class EngineGameSessionTicker {

    private final GameSession session;
    private final MessageHandler messageHandler;
    private EngineSettings settings;
    private int gameTick;
    private int doorVulnerabilityTick;
    private int doorMalfunctionTick;

    public EngineGameSessionTicker(@NotNull GameSession session, @NotNull MessageHandler messageHandler) {
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
        gameTick++;
        if (gameTick >= settings.getMaxGameDuration()) {
            session.endGame(null, true);
            session.prepare();
            session.start();
            this.reset();
            return;
        }

        if (shouldDoorsBecomeVulnerable()) {
            session.makeDoorsVulnerable();
            doorVulnerabilityTick = settings.getDoorVulnerabilityDuration();
            return;
        }

        DoorState doorState = session.getDoorState();
        if (doorState == DoorState.VULNERABLE && doorVulnerabilityTick-- == 0) {
            session.restoreDoors();
        } else if (doorState == DoorState.MALFUNCTIONING && doorMalfunctionTick-- == 0) {
            session.restoreDoors();
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
        // while there are applicants and the session does not have max allowed cops
        while (!(session.getCopApplicants().isEmpty() || session.hasMaxAllowedCops())) {
            Player player = Objects.requireNonNull(selectRandomNewCop());
            BukkitAudiences audiences = messageHandler.getAudiences();
            messageHandler.sendMessage(audiences.all(), Message.FIRST_COP_SELECTED, true, player.getDisplayName(), session.getRegion().getId());
        }
    }

    private Player selectRandomNewCop() {
        List<Player> copApplicants = session.getCopApplicants();
        if (copApplicants.isEmpty()) {
            return null;
        }

        int index = 0;
        if (gameTick == settings.getCopsSelectionDelay()) {
            Random random = ThreadLocalRandom.current();
            int size = copApplicants.size();
            index = random.nextInt(size);
        }

        Player newCop = copApplicants.get(index);
        session.setPlayerState(newCop, PlayerState.COP);
        session.removeCopApplicant(newCop);
        session.teleportCopToMainRoom(newCop);
        return newCop;
    }

}
