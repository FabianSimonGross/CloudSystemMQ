package de.nimzan.node.enums;

public enum GameServerTemplate {

    BOW_BASH("templates/servers/BowBash"),
    GUESS_IT("templates/servers/GuessIT"),
    HIDE_AND_SEEK("templates/servers/HideAndSeek"),
    LOBBY("templates/servers/Lobby");

    private final String path;

    GameServerTemplate(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}

