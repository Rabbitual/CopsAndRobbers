package xyz.mauwh.message;

import org.jetbrains.annotations.NotNull;

public enum Message {
    PREFIX("plugin-prefix", "<dark_red>[CopsAndRobbers] </dark_red>"),
    CANDR_COMMAND_USAGE("candr-command-usage", "<red>Usage: /candr join [{0}], /candr leave, /candr quit"),
    PLAYERS_ONLY_COMMAND("players-only-command", "<red>Only players may use this command"),
    ROBBERS_ONLY_COMMAND("robbers-only-command", "<red>Only robbers may use this command"),
    IN_GAME_ONLY_COMMAND("robbers-only-command", "<red>You must join a game to use this command"),
    NOT_TIME_FOR_COMMAND("not-time-for-command", "<red>It is not the time to use this command"),
    GAME_DOES_NOT_EXIST("game-does-not-exist", "<red>That game does not exist"),
    GAME_CURRENTLY_FULL("game-currently-full", "<red>This game is currently full, please try again later"),
    JOINED_GAME("joined-game", "<green>You just joined the cops and robbers game! Please respect the rules and have fun!"),
    LEFT_GAME("left-game", "<gray>You have left jail #{0}"),
    JAIL_COULD_USE_COPS("jail-could-use-cops", "<blue>Your jail could use more cops! Type <yellow>'/cops'</yellow> to switch sides!"),
    FIRST_COP_SELECTED("first-cop-selected", "<red>{0} <green> is the new cop in jail <yellow>"),
    FIRST_COPS_SELECTED_2("first-cops-selected-2", "<green>{0} <dark_green>and <green>{1} <dark_green>are the new cops in jail <yellow>#{2}</yellow>!"),
    FIRST_COPS_SELECTED_3("first-cops-selected-3", "<dark_green><green>{0}</green>, <green>{1}</green, and <green>{2}</green> are the new cops in jail <yellow>#{3}</yellow>!"),
    COP_RETIRED("cop-retired", "<green>A cop from jail <yellow>#{0}</yellow> has retired! Anybody can fill the vacancy! Robbers, type <yellow>'/cops'</yellow> to switch sides!"),
    REPORT_BUGS("report-bugs", "<purple>Please report any bugs or glitches! This allows us to ensure that everybody can have fun!"),
    VULNERABILITY_DETECTED("vulnerability-detected", "<red>A vulnerability in jail <yellow>#{0}</yellow> security has been detected! Robbers, type <yellow>'/open cells' quickly!"),
    DOORS_MALFUNCTIONED("doors-malfunctioned", "<red>The cell doors in jail <yellow>#{0}</yellow> malfunctioned! Robbers, this is your chance!"),
    ROBBER_ESCAPED("robber-escaped", "<dark_purple>Congratulations, <red>{0}</red>! You escaped jail #{1} and won!"),
    NO_ESCAPEES("no-escapees", "<blue>Game #{0} ended with no escapees. Cops win!"),
    ;

    private final String path;
    private final String defaultValue;

    Message(@NotNull String path, @NotNull String defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @NotNull
    public String defaultValue() {
        return defaultValue;
    }

}
