package xyz.mauwh.candr.engine.configuration;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.mauwh.candr.game.GameRegion;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static xyz.mauwh.message.ColoredConsoleStringBuilder.builder;

public class RegionSerializer {

    private final Logger logger;

    public RegionSerializer(@NotNull Logger logger) {
        this.logger = logger;
    }

    /**
     * Deserializes a region from the provided map
     * @param serializedRegion - the serialized region
     * @return the deserialized region
     * @throws IllegalArgumentException if the serialized region has an invalid id or world
     */
    @NotNull
    public GameRegion deserialize(@NotNull Map<?, ?> serializedRegion) throws IllegalArgumentException {
        Object id = serializedRegion.get("id");
        Preconditions.checkArgument(id != null, "Unable to deserialize region: no region id");
        Preconditions.checkArgument(id instanceof Integer, "Unable to deserialize region: region id must be an integer");

        Object worldName = serializedRegion.get("world");
        World world = worldName == null ? null : Bukkit.getWorld(String.valueOf(worldName));
        if (world == null) {
            throw new IllegalArgumentException("Unable to deserialize region: invalid world ('" + worldName + "'");
        }

        Object minPosObj = serializedRegion.get("min-pos");
        Object maxPosObj = serializedRegion.get("max-pos");
        Object copSpawnPointObj = serializedRegion.get("cop-spawn-point");
        Preconditions.checkArgument(minPosObj instanceof Map<?, ?>, "Unable to deserialize region: invalid min position (%s)", minPosObj);
        Preconditions.checkArgument(maxPosObj instanceof Map<?, ?>, "Unable to deserialize region: invalid max position (%s)", maxPosObj);

        Location minPos = deserializeLocationFromCoordinateMap(world, (Map<?, ?>)minPosObj, "Unable to deserialize region: invalid min position");
        Location maxPos = deserializeLocationFromCoordinateMap(world, (Map<?, ?>)maxPosObj, "Unable to deserialize region: invalid max position");

        Object robberSpawnPointsObj = serializedRegion.get("robber-spawn-points");
        Object doorLocationsObj = serializedRegion.get("door-locations");
        Preconditions.checkArgument(robberSpawnPointsObj instanceof List<?>, "Unable to deserialize region: invalid robber spawn points");
        Preconditions.checkArgument(doorLocationsObj instanceof List<?>, "Unable to deserialize region: invalid door locations");
        List<Location> robberSpawnPoints = deserializeLocationListFromMapList(world, (List<?>)robberSpawnPointsObj, "Unable to deserialize cop spawn point for region id " + id + "(x: %s, y: %s, z: %s)");
        List<Location> doorLocations = deserializeLocationListFromMapList(world, (List<?>)doorLocationsObj, "Unable to deserialize cop spawn point for region id " + id + "(x: %s, y: %s, z: %s)");

        GameRegion region = new GameRegion((int)id, world, minPos, maxPos);

        checkArgumentSafely(!robberSpawnPoints.isEmpty(), "Missing robber spawn points, expected behavior may be altered (id: " + id + ")");
        checkArgumentSafely(!doorLocations.isEmpty(), "Missing door locations, expected behavior may be altered (id: " + id + ")");
        if (checkArgumentSafely(copSpawnPointObj instanceof Map<?, ?>, "Missing or invalid cop spawn point, expected behavior may be altered (id: " + id + ")")) {
            //noinspection ConstantConditions
            Location copSpawnPoint = deserializeLocationFromCoordinateMap(world, (Map<?, ?>)copSpawnPointObj, "");
            region.setCopSpawnPoint(copSpawnPoint);
        }

        region.setRobberSpawnPoints(robberSpawnPoints);
        region.setDoorPositions(doorLocations);
        return region;
    }

    /**
     * Serializes the provided region as a map
     * @param region - the region to be serialized
     * @return the region serialized as a map
     */
    @NotNull
    public Map<String, Object> serialize(@NotNull GameRegion region) {
        Map<String, Object> regionMap = new HashMap<>();
        regionMap.put("world", region.getWorld().getName());
        regionMap.put("min-pos", region.getMinPos());
        regionMap.put("max-pos", region.getMaxPos());
        regionMap.put("cop-spawn-point", region.getCopSpawnPoint());
        regionMap.put("robber-spawn-points", region.getRobberSpawnPoints());
        regionMap.put("door-locations", region.getDoorLocations());
        return regionMap;
    }

    /**
     * Attempts to deserialize a location from the provided world and map of coordinates
     * @param world - the world to create a location from
     * @param map - the map to be deserialized as a location
     * @param errMessage - the message to be used in the case that an exception is thrown
     * @return the deserialized location
     */
    @NotNull
    private Location deserializeLocationFromCoordinateMap(@NotNull World world, @NotNull Map<?, ?> map, @NotNull String errMessage) {
        Object x = map.get("x");
        Object y = map.get("y");
        Object z = map.get("z");
        if (x instanceof Number && y instanceof Number && z instanceof Number) {
            return new Location(world, ((Number)x).doubleValue(), ((Number)y).doubleValue(), ((Number)z).doubleValue());
        }
        throw new IllegalArgumentException(String.format(errMessage, x, y, z));
    }

    /**
     *
     * @param mapList - the map list to deserialize as a list of locations
     * @return the deserialized list of locations
     */
    @NotNull
    private List<Location> deserializeLocationListFromMapList(@NotNull World world, @Nullable List<?> mapList, @NotNull String warningMessage) {
        if (mapList == null) {
            return Collections.emptyList();
        }
        return mapList.stream().filter(e -> e instanceof Map<?, ?>).map(e -> {
            try {
                return deserializeLocationFromCoordinateMap(world, (Map<?, ?>)e, warningMessage);
            } catch (IllegalArgumentException err) {
                builder().yellow(warningMessage).post(logger, Level.WARNING);
                return null;
            }
        }).filter(Objects::nonNull).toList();
    }

    /**
     * Logs a warning if the provided expression evaluates to false
     * @param expression - the expression to assert as true
     * @param message - the warning message used if the provided expression evaluates to false
     * @return true if the provided expression evaluates to true
     */
    private boolean checkArgumentSafely(boolean expression, String message) {
        if (!expression) {
            builder().yellow(message).post(logger, Level.WARNING);
            return false;
        }
        return true;
    }

}
