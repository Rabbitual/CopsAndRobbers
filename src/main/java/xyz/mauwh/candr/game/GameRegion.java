package xyz.mauwh.candr.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.region.RegionNode;

import java.util.ArrayList;
import java.util.List;

public class GameRegion {

    private final int id;
    private final World world;
    private final Location minPos;
    private final Location maxPos;
    private Location copSpawnPoint;
    private List<Location> robberSpawnPoints;
    private List<Location> doorPositions;
    private List<RegionNode> nodes;

    public GameRegion(int id, @NotNull World world, @NotNull Location minPos, @NotNull Location maxPos) {
        this.id = id;
        this.world = world;
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.robberSpawnPoints = new ArrayList<>();
        this.doorPositions = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    @NotNull
    public World getWorld() {
        return world;
    }

    @NotNull
    public Location getMinPos() {
        return minPos.clone();
    }

    @NotNull
    public Location getMaxPos() {
        return maxPos.clone();
    }

    @NotNull
    public Location getCopSpawnPoint() {
        return copSpawnPoint.clone();
    }

    public void setCopSpawnPoint(@NotNull Location location) {
        this.copSpawnPoint = location.clone();
    }

    @NotNull
    public List<Location> getRobberSpawnPoints() {
        return List.copyOf(robberSpawnPoints);
    }

    public void setRobberSpawnPoints(@NotNull List<Location> robberSpawnPoints) {
        this.robberSpawnPoints = List.copyOf(robberSpawnPoints);
    }

    @NotNull
    public List<Location> getDoorLocations() {
        return List.copyOf(doorPositions);
    }

    public void setDoorPositions(@NotNull List<Location> doorPositions) {
        this.doorPositions = List.copyOf(doorPositions);
    }

    @NotNull
    public List<RegionNode> getNodes() {
        return List.copyOf(nodes);
    }

    public void setNodes(@NotNull List<RegionNode> nodes) {
        this.nodes = List.copyOf(nodes);
    }

}
