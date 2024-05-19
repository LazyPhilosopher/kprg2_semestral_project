package com.uhk.sergede1.webgameappbackend.rest.tokenized.game;

public class TopPlayerStats {
    private String username;
    private int wins;

    public TopPlayerStats(String username, int wins) {
        this.username = username;
        this.wins = wins;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }
}
