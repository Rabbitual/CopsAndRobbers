package xyz.mauwh.candr.engine.configuration;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

/**
 * A container for all settings relating to the Cops and Robber Engine's general functionality. These settings apply to
 * all game sessions across the engine, however, changes will only apply to a session after the next time it resets.
 */
public class EngineSettings {

    private final Logger logger;
    private int maxGameDuration;
    private int copsSelectionDelay;
    private double doorVulnerabilityChance;
    private int doorVulnerabilityInterval;
    private int doorVulnerabilityDuration;
    private int doorMalfunctionDuration;
    private int maxPlayers;
    private int minPlayersTwoCops;
    private int minPlayersThreeCops;
    private Material winMaterial;
    private List<ItemStack> copItems;
    private List<ItemStack> robberItems;
    private Location lobbySpawn;

    public EngineSettings(@NotNull Logger logger) {
        this.logger = logger;
    }

    /**
     * Loads all configured settings from the provided {@link org.bukkit.configuration.file.YamlConfiguration}
     * @param configuration - the configuration to load the engine settings from
     */
    public void load(@NotNull final YamlConfiguration configuration) {
        List<?> emptyList = Collections.emptyList();
        copItems = configuration.getList("cop-items", emptyList).stream().filter(e -> e instanceof ItemStack).map(e -> (ItemStack)e).toList();
        robberItems = configuration.getList("robber-items", emptyList).stream().filter(e -> e instanceof ItemStack).map(e -> (ItemStack)e).toList();
        minPlayersThreeCops = configuration.getInt("min-players-three-cops");
        minPlayersTwoCops = configuration.getInt("min-players-two-cops");
        maxPlayers = configuration.getInt("max-players");
        copsSelectionDelay = configuration.getInt("cops-selection-delay");
        doorMalfunctionDuration = configuration.getInt("door-malfunction-duration");
        doorVulnerabilityDuration = configuration.getInt("door-vulnerability-duration");
        doorVulnerabilityInterval = configuration.getInt("door-vulnerability-interval");
        doorVulnerabilityChance = configuration.getDouble("door-vulnerability-chance");
        maxGameDuration = configuration.getInt("max-game-duration");
        winMaterial = Material.valueOf(configuration.getString("win-material", "AIR").toUpperCase());
        lobbySpawn = configuration.getLocation("lobby-spawn");

        logSettings();
        if (!configuration.isConfigurationSection("lobby-spawn")) {
            logger.warning("Unable to set lobby spawn: no lobby configured");
        } else if (lobbySpawn == null) {
            logger.warning("Unable to set lobby: unable to find world with name '" + configuration.getString("lobby-spawn.world") + "'");
        }

        if (winMaterial == Material.AIR || !winMaterial.isBlock()) {
            logger.warning(String.format("Win material %s is not a valid block, this may affect the game's expected behavior", winMaterial));
        }
    }

    /**
     * Logs all configured settings to console
     */
    private void logSettings() {
        logger.info("-------- Cops And Robbers Engine Settings --------");
        logger.info("Max game duration: " + maxGameDuration);
        logger.info("Cops selection delay: " + copsSelectionDelay);
        logger.info("Door vulnerability chance: " + (doorVulnerabilityChance * 100) + "%");
        logger.info("Door vulnerability interval: " + doorVulnerabilityInterval);
        logger.info("Door vulnerability duration: " + doorVulnerabilityDuration);
        logger.info("Door malfunction duration: " + doorMalfunctionDuration);
        logger.info("Max players: " + maxPlayers);
        logger.info("Min players two cops: " + minPlayersTwoCops);
        logger.info("Min players three cops: " + minPlayersThreeCops);
        logger.info("Win material: " + winMaterial);
        logger.info("Cop items: " + copItems.size());
        logger.info("Robber items: " + robberItems.size());
        logger.info("Lobby spawnpoint: " + lobbySpawn);
    }

    /**
     * Gets the maximum allowed game duration
     * @return the max game duration
     */
    public int getMaxGameDuration() {
        return maxGameDuration;
    }

    /**
     * Gets the initial delay before cops are selected in a session
     * @return the delay before cops selection
     */
    public int getCopsSelectionDelay() {
        return copsSelectionDelay;
    }

    /**
     * Gets the chance that the doors will become vulnerable during their regular interval
     * @see #getDoorVulnerabilityInterval()
     * @return the door vulnerability chance
     */
    public double getDoorVulnerabilityChance() {
        return doorVulnerabilityChance;
    }

    /**
     * Gets the interval at which doors may become vulnerable, but will only do so at a certain chance
     * @see #getDoorVulnerabilityChance()
     * @return the door vulnerability interval
     */
    public int getDoorVulnerabilityInterval() {
        return doorVulnerabilityInterval;
    }

    /**
     * Gets the duration that the doors will remain vulnerable for
     * @return the door vulnerability duration
     */
    public int getDoorVulnerabilityDuration() {
        return doorVulnerabilityDuration;
    }

    /**
     * Gets the duration that the doors will malfunction for
     * @return the door malfunction duration
     */
    public int getDoorMalfunctionDuration() {
        return doorMalfunctionDuration;
    }

    /**
     * Gets the maximum allowed players in a session
     * @return the max players for a session
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Gets the minimum required players to allow for two cops
     * @return the minimum players for two cops
     */
    public int getMinPlayersTwoCops() {
        return minPlayersTwoCops;
    }

    /**
     * Gets the minimum required players to allow for three cops
     * @return the minimum players for three cops
     */
    public int getMinPlayersThreeCops() {
        return minPlayersThreeCops;
    }

    /**
     * Gets the block material that a robber must interact with to win the game
     * @return the win material
     */
    @NotNull
    public Material getWinMaterial() {
        return winMaterial;
    }

    /**
     * Gets a list of the items that are given to cops upon their selection
     * @return the items given to cops
     */
    @NotNull
    public List<ItemStack> getCopItems() {
        return copItems.stream().map(ItemStack::clone).toList();
    }

    /**
     * Gets a list of the items that are given to robbers upon joining
     * @return the items given to robbers
     */
    @NotNull
    public List<ItemStack> getRobberItems() {
        return robberItems.stream().map(ItemStack::clone).toList();
    }

    /**
     * The lobby spawn point, which players are teleported to when a game ends or when they leave
     * @return the lobby spawn point
     */
    @Nullable
    public Location getLobbySpawn() {
        return lobbySpawn;
    }

}
