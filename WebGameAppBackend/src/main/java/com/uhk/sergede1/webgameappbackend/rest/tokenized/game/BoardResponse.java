package com.uhk.sergede1.webgameappbackend.rest.tokenized.game;

class BoardResponse {
    private char[][] board;
    private boolean isActive;

    private Long lastMove;

    private Long victor;
    private Long XPlayerID;
    private Long OPlayerID;

    // Constructor
    public BoardResponse(char[][] board, boolean isActive, Long lastMove, Long victor, Long XPlayerID, Long OPlayerID) {
        this.board = board;
        this.isActive = isActive;
        this.lastMove = lastMove;
        this.victor = victor;
        this.XPlayerID = XPlayerID;
        this.OPlayerID = OPlayerID;
    }

    // Getters and setters
    public char[][] getBoard() {
        return board;
    }

    public void setBoard(char[][] board) {
        this.board = board;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Long getLastMove() {
        return lastMove;
    }

    public void setLastMove(Long lastMove){
        this.lastMove = lastMove;
    }

    public Long getVictor() {
        return victor;
    }

    public void setVictor(Long victor){
        this.victor = victor;
    }

    public Long getXPlayerID() {
        return XPlayerID;
    }

    public void setXPlayerID(Long XPlayerID){
        this.XPlayerID = XPlayerID;
    }

    public Long getOPlayerID() {
        return OPlayerID;
    }

    public void setOPlayerID(Long OPlayerID){
        this.OPlayerID = OPlayerID;
    }
}