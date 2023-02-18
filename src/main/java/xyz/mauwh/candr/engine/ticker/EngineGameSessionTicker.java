package xyz.mauwh.candr.engine.ticker;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.engine.configuration.EngineSettings;
import xyz.mauwh.candr.game.DoorState;
import xyz.mauwh.candr.game.GameSession;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class EngineGameSessionTicker {

    private final CopsAndRobbersEngine engine;
    private final EngineSettings settings;
    private final GameSession session;
    private int gameTick;
    private int copsSelectionTick;
    private int doorVulnerabilityIntervalTick;
    private int doorVulnerabilityTick;
    private int doorMalfunctionTick;

    public EngineGameSessionTicker(@NotNull CopsAndRobbersEngine engine, @NotNull GameSession session) {
        this.engine = engine;
        this.session = session;
        this.settings = session.getSettings();
    }

    public void reset() {
        gameTick = settings.getMaxGameDuration();
        resetDoorVulnerabilityIntervalTick();
        resetDoorVulnerabilityTick();
        resetDoorMalfunctionTick();
        resetCopsSelectionTick();
    }

    /**
     * Resets the cops selection tick to its configured default value
     */
    public void resetCopsSelectionTick() {
        copsSelectionTick = settings.getCopsSelectionDelay();
    }

    /**
     * Resets the door vulnerability interval tick to its configured default value
     */
    public void resetDoorVulnerabilityIntervalTick() {
        doorVulnerabilityIntervalTick = settings.getDoorVulnerabilityInterval();
    }

    /**
     * Resets the door vulnerability tick to its configured default value
     */
    public void resetDoorVulnerabilityTick() {
        doorVulnerabilityTick = settings.getDoorVulnerabilityDuration();
    }

    /**
     * Resets the door malfunction tick to its configured default value
     */
    public void resetDoorMalfunctionTick() {
        doorMalfunctionTick = settings.getDoorMalfunctionDuration();
    }

    /**
     * The main heartbeat of any individual game session
     */
    public void tick() {
        gameTick--;
        if (gameTick <= 0) {
            session.endGame(null, true);
            session.prepare();
            session.start();
            this.reset();
            return;
        }

        tickCopsSelection();

        if (shouldDoorsBecomeVulnerable()) {
            resetDoorVulnerabilityIntervalTick();
            session.makeDoorsVulnerable();
            return;
        }

        if (session.getDoorState() == DoorState.VULNERABLE) {
            tickDoorVulnerability();
        } else if (session.getDoorState() == DoorState.MALFUNCTIONING) {
            tickDoorMalfunction();
        }

        doorVulnerabilityIntervalTick--;
    }

    /**
     * Ticks initial cop selection. When the tick is equal to 0, cop applicants will be selected and made cops randomly
     * until the game can no longer have any. When less than 0, if a game needs more cops, then applicants will be made
     * cops automatically until the game can no longer have any.
     */
    private void tickCopsSelection() {
        if (copsSelectionTick > 0) {
            copsSelectionTick--;
            return;
        }

        int index = 0;
        while (canSelectCop()) {
            List<Player> copApplicants = session.getCopApplicants();
            if (copsSelectionTick == 0) {
                Random random = ThreadLocalRandom.current();
                int size = copApplicants.size();
                index = random.nextInt(size);
            }
            Player newCop = copApplicants.get(index);
            session.addCop(newCop);
            session.removeCopApplicant(newCop);
            //session.teleportCopToMainRoom(newCop);
        }
        copsSelectionTick--;
    }

    /**
     * Ticks the door vulnerability duration. The doors will remain vulnerable while the tick is greater than 0.
     */
    private void tickDoorVulnerability() {
        DoorState doorState = session.getDoorState();
        if (doorState == DoorState.VULNERABLE && doorVulnerabilityTick-- == 0) {
            session.restoreDoors();
        }
    }

    /**
     * Ticks the door malfunction duration. The doors will remain open while the tick is greater than 0.
     */
    public void tickDoorMalfunction() {
        if (doorMalfunctionTick-- == 0) {
            session.restoreDoors();
        }
    }

    /**
     * Checks if this ticker's game session currently allows more cops
     * @return true if this ticker's session may have more cops
     */
    private boolean canSelectCop() {
        return !(session.getCopApplicants().isEmpty() || session.hasMaxAllowedCops());
    }

    /**
     * Checks at a certain weighted chance if the doors may become vulnerable
     * @return true if the doors should become vulnerable
     */
    private boolean shouldDoorsBecomeVulnerable() {
        if (session.getDoorState() != DoorState.SECURE) {
            return false;
        }

        EngineSettings settings = session.getSettings();
        double doorVulnerabilityChance = settings.getDoorVulnerabilityChance();
        double num = ThreadLocalRandom.current().nextDouble();
        boolean result = false;
        if (doorVulnerabilityIntervalTick <= 0) {
            result = num > (1.0 - doorVulnerabilityChance);
        }
        return result;
    }

}
