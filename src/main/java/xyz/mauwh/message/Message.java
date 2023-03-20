package xyz.mauwh.message;

import org.jetbrains.annotations.NotNull;

public enum Message {
    PLUGIN_PREFIX,
    ENGINE_IS_HALTED,
    CANDR_COMMAND_USAGE,
    PLAYERS_ONLY_COMMAND,
    ROBBERS_ONLY_COMMAND,
    IN_GAME_ONLY_COMMAND,
    NOT_TIME_FOR_COMMAND,
    GAME_DOES_NOT_EXIST,
    GAME_CURRENTLY_FULL,
    JOINED_GAME,
    ALREADY_IN_GAME,
    LEFT_GAME,
    JAIL_COULD_USE_COPS,
    ALREADY_A_COP,
    NOT_ACCEPTING_APPLICATIONS,
    ALREADY_APPLIED_FOR_COP,
    APPLIED_FOR_COP,
    FIRST_COP_SELECTED,
    FIRST_COPS_SELECTED_2,
    FIRST_COPS_SELECTED_3,
    COP_RETIRED,
    PRISON_ACCESS_GRANTED,
    REPORT_BUGS,
    VULNERABILITY_DETECTED,
    DOORS_MALFUNCTIONED,
    ROBBER_ESCAPED,
    NO_ESCAPEES,
    LOBBY_NOT_FOUND,
    CELLS_NOT_FOUND;

    private final String path = toString().toLowerCase().replace("_", "-");

    @NotNull
    public String getPath() {
        return path;
    }

}
