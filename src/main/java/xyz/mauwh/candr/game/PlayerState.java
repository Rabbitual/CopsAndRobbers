package xyz.mauwh.candr.game;

public enum PlayerState {
    ROBBER,
    ROBBER_UNRESTRICTED,
    COP,
    ;

    public boolean isRobber() {
        return this == ROBBER || this == ROBBER_UNRESTRICTED;
    }

    public boolean hasPrisonAccess() {
        return this == ROBBER_UNRESTRICTED || this == COP;
    }
}
