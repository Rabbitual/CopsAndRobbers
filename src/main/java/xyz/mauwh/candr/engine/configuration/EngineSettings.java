package xyz.mauwh.candr.engine.configuration;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * A container for all settings relating to the Cops and Robber Engine's general functionality. These settings apply to
 * all game sessions across the engine, however, changes will only apply to a session after the next time it resets.
 */
public class EngineSettings {

    private final File file;
    private final Logger logger;
    private YamlConfiguration configuration;

    public EngineSettings(@NotNull File file, @NotNull Logger logger) {
        this.file = file;
        this.logger = logger;
    }

    /**
     * Loads all configured settings from the provided {@link org.bukkit.configuration.file.YamlConfiguration}
     */
    public void reload() {
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            configuration.save(file);
        } catch (IOException err) {
            logger.severe("An unexpected error occurred while attempting to save CopsAndRobbers engine settings:");
            err.printStackTrace();
        }
    }

    /**
     * Logs all configured settings to console
     */
    public void logSettings() {
        logger.info("-------- Cops And Robbers Engine Settings --------");
        logger.info("Max game duration: " + getMaxGameDuration());
        logger.info("Cops selection delay: " + getCopsSelectionDelay());
        logger.info("Door vulnerability chance: " + (getDoorVulnerabilityChance() * 100) + "%");
        logger.info("Door vulnerability interval: " + getDoorVulnerabilityInterval());
        logger.info("Door vulnerability duration: " + getDoorVulnerabilityDuration());
        logger.info("Door malfunction duration: " + getDoorMalfunctionDuration());
        logger.info("Max players: " + getMaxPlayers());
        logger.info("Min players two cops: " + getMinPlayersTwoCops());
        logger.info("Min players three cops: " + getMinPlayersThreeCops());
        logger.info("Win material: " + getWinMaterial());
        logger.info("Cop items: " + getCopItems().size());
        logger.info("Robber items: " + getRobberItems().size());
        logger.info("Lobby spawnpoint: " + getLobbySpawn());

        if (!configuration.isConfigurationSection("lobby-spawn")) {
            logger.warning("Unable to set lobby spawn: no lobby configured");
        } else if (getLobbySpawn() == null) {
            logger.warning("Unable to set lobby: unable to find world with name '" + configuration.getString("lobby-spawn.world") + "'");
        }

        Material winMaterial = getWinMaterial();
        if (winMaterial.isAir() || !winMaterial.isBlock()) {
            logger.warning(String.format("Win material %s is not a valid block, this may affect the game's expected behavior", winMaterial));
        }
    }

    /**
     * Gets the maximum allowed game duration
     * @return the max game duration
     */
    public int getMaxGameDuration() {
        return configuration.getInt("max-game-duration");
    }

    public void setMaxGameDuration(int maxGameDuration) {
        configuration.set("max-game-duration", maxGameDuration);
    }

    /**
     * Gets the initial delay before cops are selected in a session
     * @return the delay before cops selection
     */
    public int getCopsSelectionDelay() {
        return configuration.getInt("cops-selection-delay");
    }

    public void setCopsSelectionDelay( int copsSelectionDelay) {
        configuration.set("cops-selection-delay", copsSelectionDelay);
    }

    /**
     * Gets the chance that the doors will become vulnerable during their regular interval
     * @see #getDoorVulnerabilityInterval()
     * @return the door vulnerability chance
     */
    public double getDoorVulnerabilityChance() {
        return configuration.getDouble("door-vulnerability-chance");
    }

    public void setDoorVulnerabilityChance(int doorVulnerabilityChance) {
        configuration.set("door-vulnerability-chance", doorVulnerabilityChance);
    }

    /**
     * Gets the interval at which doors may become vulnerable, but will only do so at a certain chance
     * @see #getDoorVulnerabilityChance()
     * @return the door vulnerability interval
     */
    public int getDoorVulnerabilityInterval() {
        return configuration.getInt("door-vulnerability-interval");
    }

    public void setDoorVulnerabilityInterval(int doorVulnerabilityInterval) {
        configuration.set("door-vulnerability-interval", doorVulnerabilityInterval);
    }

    /**
     * Gets the duration that the doors will remain vulnerable for
     * @return the door vulnerability duration
     */
    public int getDoorVulnerabilityDuration() {
        return configuration.getInt("door-vulnerability-duration");
    }

    public void setDoorVulnerabilityDuration(int doorVulnerabilityDuration) {
        configuration.set("door-vulnerability-duration", doorVulnerabilityDuration);
    }

    /**
     * Gets the duration that the doors will malfunction for
     * @return the door malfunction duration
     */
    public int getDoorMalfunctionDuration() {
        return configuration.getInt("door-malfunction-duration");
    }

    public void setDoorMalfunctionDuration(int doorMalfunctionDuration) {
        configuration.set("door-malfunction-duration", doorMalfunctionDuration);
    }

    /**
     * Gets the maximum allowed players in a session
     * @return the max players for a session
     */
    public int getMaxPlayers() {
        return configuration.getInt("max-players");
    }

    public void setMaxPlayers(int maxPlayers) {
        configuration.set("max-players", maxPlayers);
    }

    /**
     * Gets the minimum required players to allow for two cops
     * @return the minimum players for two cops
     */
    public int getMinPlayersTwoCops() {
        return configuration.getInt("min-players-two-cops");
    }

    public void setMinPlayersThreeCops(int minPlayersThreeCops) {
        configuration.set("min-players-three-cops", minPlayersThreeCops);
    }

    /**
     * Gets the minimum required players to allow for three cops
     * @return the minimum players for three cops
     */
    public int getMinPlayersThreeCops() {
        return configuration.getInt("min-players-three-cops");
    }

    public void setMinPlayersTwoCops(int minPlayersTwoCops) {
        configuration.set("min-players-two-cops", minPlayersTwoCops);
    }

    /**
     * Gets the block material that a robber must interact with to win the game
     * @return the win material
     */
    @NotNull
    public Material getWinMaterial() {
        String materialName = configuration.getString("win-material", "AIR");
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException err) {
            return Material.AIR;
        }
    }

    public void setWinMaterial(@NotNull Material winMaterial) {
        configuration.set("win-material", winMaterial.toString());
    }

    /**
     * Gets a list of the items that are given to cops upon their selection
     * @return the items given to cops
     */
    @NotNull
    public List<ItemStack> getCopItems() {
        return configuration.getList("cop-items", Collections.emptyList()).stream().
                filter(e -> e instanceof ItemStack).map(e -> (ItemStack)e).toList();
    }

    public void setCopItems(@NotNull List<ItemStack> copItems) {
        configuration.set("cop-items", copItems);
    }

    /**
     * Gets a list of the items that are given to robbers upon joining
     * @return the items given to robbers
     */
    @NotNull
    public List<ItemStack> getRobberItems() {
        return configuration.getList("robber-items", Collections.emptyList()).stream().
                filter(e -> e instanceof ItemStack).map(e -> (ItemStack)e).toList();
    }

    public void setRobberItems(@NotNull List<ItemStack> robberItems) {
        configuration.set("robber-items", robberItems);
    }

    /**
     * The lobby spawn point, which players are teleported to when a game ends or when they leave
     * @return the lobby spawn point
     */
    @Nullable
    public Location getLobbySpawn() {
        return configuration.getLocation("lobby-spawn");
    }

    public void setLobbySpawn(@NotNull Location lobbySpawn) {
        configuration.set("lobby-spawn", lobbySpawn);
    }

}
