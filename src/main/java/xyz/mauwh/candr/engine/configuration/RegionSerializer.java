package xyz.mauwh.candr.engine.configuration;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.game.GameRegion;
import xyz.mauwh.candr.region.AccessNode;
import xyz.mauwh.candr.region.RegionNode;
import xyz.mauwh.candr.region.TeleportNode;

import java.util.*;
import java.util.logging.Logger;

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
        var idRaw = serializedRegion.get("id");
        if (!(idRaw instanceof Integer)) {
            throw new IllegalArgumentException(String.format("Invalid region id '%s', must be an integer", idRaw));
        }

        int id = (int)idRaw;
        String worldName = String.valueOf(serializedRegion.get("world"));
        World world = worldName == null ? null : Bukkit.getWorld(worldName);
        if (world == null) {
            throw new IllegalArgumentException(String.format("Invalid region world name '%s' (id: %s)", worldName, id));
        }

        Location minPos = deserializeLocationAtPath(world, serializedRegion, "min-pos");
        Location maxPos = deserializeLocationAtPath(world, serializedRegion, "max-pos");
        Location copSpawnPoint = deserializeLocationAtPath(world, serializedRegion, "cop-spawn-point");

        List<Map<?, ?>> robberSpawnPointsMap = SerializationUtils.<List<Map<?, ?>>>castDubiously(serializedRegion.get("robber-spawn-points")).orElse(Collections.emptyList());
        List<Map<?, ?>> doorLocationsMap = SerializationUtils.<List<Map<?, ?>>>castDubiously(serializedRegion.get("door-locations")).orElse(Collections.emptyList());
        List<Location> robberSpawnPoints = SerializationUtils.deserializeLocationList(world, robberSpawnPointsMap, logger, "Skipping invalid robber spawn point (id: " + id + ")");
        List<Location> doorLocations = SerializationUtils.deserializeLocationList(world, doorLocationsMap, logger, "Skipping invalid door location (id: " + id + ")");

        if (!robberSpawnPoints.isEmpty()) {
            logger.warning("Missing robber spawn points, expected behavior may be altered (id: " + id + ")");
        } else if (!doorLocations.isEmpty()) {
            logger.warning("Missing door locations, expected behavior may be altered (id: " + id + ")");
        }

        List<Map<?, ?>> accessNodesMapList = SerializationUtils.<List<Map<?, ?>>>castDubiously(serializedRegion.get("access-nodes")).orElse(Collections.emptyList());
        List<Map<?, ?>> teleportNodesMapList = SerializationUtils.<List<Map<?, ?>>>castDubiously(serializedRegion.get("teleport-nodes")).orElse(Collections.emptyList());
        List<RegionNode> accessNodes = SerializationUtils.deserializeNodeList(world, accessNodesMapList, AccessNode::deserialize, logger, "Skipping invalid access node (id: " + id + ")");
        List<RegionNode> teleportNodes = SerializationUtils.deserializeNodeList(world, teleportNodesMapList, TeleportNode::deserialize, logger, "Skipping invalid teleport node (id: " + id + ")");

        GameRegion region = new GameRegion(id, world, minPos, maxPos);
        region.setCopSpawnPoint(copSpawnPoint);
        region.setRobberSpawnPoints(robberSpawnPoints);
        region.setDoorPositions(doorLocations);

        List<RegionNode> allNodes = new ArrayList<>();
        allNodes.addAll(accessNodes);
        allNodes.addAll(teleportNodes);
        region.setNodes(allNodes);

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
        regionMap.put("id", region.getId());
        regionMap.put("world", region.getWorld().getName());
        regionMap.put("min-pos", region.getMinPos());
        regionMap.put("max-pos", region.getMaxPos());
        regionMap.put("cop-spawn-point", region.getCopSpawnPoint());
        regionMap.put("robber-spawn-points", region.getRobberSpawnPoints());
        regionMap.put("door-locations", region.getDoorLocations());
        return regionMap;
    }

    @NotNull
    private Location deserializeLocationAtPath(@NotNull World world, @NotNull Map<?, ?> serializedRegion, @NotNull String path) {
        return SerializationUtils.castDubiously(serializedRegion.get(path), Map.class)
                .map(value -> SerializationUtils.deserializeLocation(world, value))
                .orElseThrow(() -> new IllegalArgumentException("Unable to deserialize location from path '" + path + "'"));
    }

}
