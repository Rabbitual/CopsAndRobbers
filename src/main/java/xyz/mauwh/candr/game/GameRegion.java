package xyz.mauwh.candr.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public Location getCopSpawnPoint() {
        return copSpawnPoint == null ? null : copSpawnPoint.clone();
    }

    public void setCopSpawnPoint(@Nullable Location location) {
        this.copSpawnPoint = location == null ? null : location.clone();
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
