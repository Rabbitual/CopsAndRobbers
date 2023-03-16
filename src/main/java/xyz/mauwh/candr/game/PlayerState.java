package xyz.mauwh.candr.game;

public enum PlayerState {
    NOT_PLAYING,
    ROBBER,
    ROBBER_UNRESTRICTED,
    COP,
    ;

    public boolean isRobber() {
        return this == ROBBER || this == ROBBER_UNRESTRICTED;
    }

    public boolean isPlaying() {
        return this != NOT_PLAYING;
    }

    public boolean hasPrisonAccess() {
        return this == ROBBER_UNRESTRICTED || this == COP;
    }
}
