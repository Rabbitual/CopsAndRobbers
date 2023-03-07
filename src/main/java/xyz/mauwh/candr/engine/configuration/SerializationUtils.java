package xyz.mauwh.candr.engine.configuration;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.region.RegionNode;

import java.util.*;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import static xyz.mauwh.message.ColoredConsoleStringBuilder.builder;

public final class SerializationUtils {

    private SerializationUtils() {}

    @NotNull
    public static <T> Optional<T> castDubiously(@NotNull Object o, @NotNull Class<T> clazz) {
        return clazz.isAssignableFrom(o.getClass()) ? Optional.of(clazz.cast(o)) : Optional.empty();
    }

    @NotNull
    public static <T> Optional<T> castDubiously(@NotNull Object o) {
        try {
            //noinspection unchecked
            return Optional.of((T)o);
        } catch (ClassCastException err) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to deserialize a location from the provided world and map of coordinates
     * @param world - the world to create a location from
     * @param map - the map to be deserialized as a location
     * @return the deserialized location
     */
    @NotNull
    public static Location deserializeLocation(@NotNull World world, @NotNull Map<?, ?> map) throws IllegalArgumentException {
        Map<String, Object> copy = new HashMap<>();
        map.forEach((key, value) -> copy.put(String.valueOf(key), value));
        copy.put("world", world.getName());
        return Location.deserialize(copy);
    }

    @NotNull
    public static List<Location> deserializeLocationList(@NotNull World world, @NotNull List<Map<?, ?>> mapList, @NotNull Logger logger, @NotNull String warningMessage) {
        if (mapList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Location> deserialized = new ArrayList<>();
        mapList.forEach(e -> {
            Optional<Map<?, ?>> serializedLocation = SerializationUtils.castDubiously(e);
            try {
                Preconditions.checkArgument(serializedLocation.isPresent());
                Location location = SerializationUtils.deserializeLocation(world, serializedLocation.get());
                deserialized.add(location);
            } catch (IllegalArgumentException err) {
                builder().red(err.getMessage()).post(logger, Level.WARNING);
                builder().yellow(warningMessage).post(logger, Level.WARNING);
            }
        });
        return deserialized;
    }

    @NotNull
    public static List<RegionNode> deserializeNodeList(@NotNull World world, @NotNull List<Map<?, ?>> mapList,
                                                       @NotNull BiFunction<World, Map<?, ?>, RegionNode> function,
                                                       @NotNull Logger logger, @NotNull String warningMessage) {
        if (mapList.isEmpty()) {
            return Collections.emptyList();
        }

        List<RegionNode> deserialized = new ArrayList<>();
        mapList.forEach(e -> {
            try {
                RegionNode node = function.apply(world, e);
                deserialized.add(node);
            } catch (IllegalArgumentException err) {
                builder().red(err.getMessage()).post(logger, Level.WARNING);
                builder().yellow(warningMessage).post(logger, Level.WARNING);
            }
        });
        return deserialized;
    }

}
