package com.uhk.sergede1.webgameappbackend.rest.tokenized.game;

class BoardResponse {
    private char[][] board;
    private boolean isActive;

    private Long lastMove;

    // Constructor
    public BoardResponse(char[][] board, boolean isActive, Long lastMove) {
        this.board = board;
        this.isActive = isActive;
        this.lastMove = lastMove;
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
}