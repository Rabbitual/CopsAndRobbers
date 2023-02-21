package xyz.mauwh.message;

import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ColoredConsoleStringBuilder {

    private static final Ansi ANSI = Ansi.ansi();
    private static final Ansi YELLOW = ANSI.fgYellow().boldOff();
    private static final Ansi RED = ANSI.fgRed().boldOff();
    private static final Ansi GREEN = ANSI.fgGreen().boldOff();
    private static final Ansi BLUE = ANSI.fgBlue().boldOff();

    private final StringBuilder builder = new StringBuilder();

    @NotNull
    public static ColoredConsoleStringBuilder builder() {
        return new ColoredConsoleStringBuilder();
    }

    @NotNull
    public ColoredConsoleStringBuilder yellow(@NotNull String text) {
        return append(YELLOW).append(text).reset();
    }

    @NotNull
    public ColoredConsoleStringBuilder red(@NotNull String text) {
        return append(RED).append(text).reset();
    }

    @NotNull
    public ColoredConsoleStringBuilder green(@NotNull String text) {
        return append(GREEN).append(text).reset();
    }

    @NotNull
    public ColoredConsoleStringBuilder blue(@NotNull String text) {
        return append(BLUE).append(text).reset();
    }

    @NotNull
    public ColoredConsoleStringBuilder reset() {
        return append(ANSI.reset().toString());
    }

    @NotNull
    public ColoredConsoleStringBuilder append(@NotNull Object obj) {
        builder.append(obj);
        return this;
    }

    /**
     * Logs the built message to the provided logger, with the provided level
     * @param logger - the logger
     * @param level - the logging level
     */
    public void post(@NotNull Logger logger, @NotNull Level level) {
        logger.log(level, builder.toString());
    }

    @Override
    @NotNull
    public String toString() {
        return builder.toString();
    }

}
