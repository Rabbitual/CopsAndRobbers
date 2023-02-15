package xyz.mauwh.candr.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GameRegion {

    private final int id;
    private final World world;
    private final Location minPos;
    private final Location maxPos;
    private List<Location> copSpawnPoints;
    private List<Location> robberSpawnPoints;
    private List<Location> doorPositions;

    public GameRegion(int id, @NotNull World world, @NotNull Location minPos, @NotNull Location maxPos) {
        this.id = id;
        this.world = world;
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.copSpawnPoints = new ArrayList<>();
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
    public List<Location> getCopSpawnPoints() {
        return List.copyOf(copSpawnPoints);
    }

    public void setCopSpawnPoints(@NotNull List<Location> copSpawnPoints) {
        this.copSpawnPoints = List.copyOf(copSpawnPoints);
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

}
