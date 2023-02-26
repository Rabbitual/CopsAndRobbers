package xyz.mauwh.message;

import org.jetbrains.annotations.NotNull;

public enum Message {
    PREFIX("plugin-prefix"),
    ENGINE_IS_HALTED("engine-is-halted"),
    CANDR_COMMAND_USAGE("candr-command-usage"),
    PLAYERS_ONLY_COMMAND("players-only-command"),
    ROBBERS_ONLY_COMMAND("robbers-only-command"),
    IN_GAME_ONLY_COMMAND("robbers-only-command"),
    NOT_TIME_FOR_COMMAND("not-time-for-command"),
    GAME_DOES_NOT_EXIST("game-does-not-exist"),
    GAME_CURRENTLY_FULL("game-currently-full"),
    JOINED_GAME("joined-game"),
    LEFT_GAME("left-game"),
    JAIL_COULD_USE_COPS("jail-could-use-cops"),
    FIRST_COP_SELECTED("first-cop-selected"),
    FIRST_COPS_SELECTED_2("first-cops-selected-2"),
    FIRST_COPS_SELECTED_3("first-cops-selected-3"),
    COP_RETIRED("cop-retired"),
    REPORT_BUGS("report-bugs"),
    VULNERABILITY_DETECTED("vulnerability-detected"),
    DOORS_MALFUNCTIONED("doors-malfunctioned"),
    ROBBER_ESCAPED("robber-escaped"),
    NO_ESCAPEES("no-escapees"),
    LOBBY_NOT_FOUND("lobby-not-found"),
    CELLS_NOT_FOUND("cells-not-found");

    private final String path;

    Message(@NotNull String path) {
        this.path = path;
    }

    @NotNull
    public String getPath() {
        return path;
    }

}
