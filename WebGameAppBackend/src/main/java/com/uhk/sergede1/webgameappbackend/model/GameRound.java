package com.uhk.sergede1.webgameappbackend.model;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "GAMEROUNDS")
public class GameRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "userPlayer1ID")
    private Long userPlayer1ID;

    @Column(name = "userPlayer2ID")
    private Long userPlayer2ID;

    @Column(name = "boardStatus", columnDefinition = "varchar(MAX)")
    private String boardStatus;

    @Column(name = "active")
    private boolean active;

    @Column(name = "victor", nullable = true)
    private Long victor;

    @Column(name = "lastMove")
    private Long lastMove;

    @Column(name = "timestamp", nullable = false)
    private Timestamp timestamp;

    // Constructors

    public GameRound() {
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserPlayer1ID() {
        return userPlayer1ID;
    }

    public Long getVictor(){
        return victor;
    }

    public Long getLastMove(){
        return lastMove;
    }

    public void setUserPlayer1ID(Long userPlayer1ID) {
        this.userPlayer1ID = userPlayer1ID;
    }

    public Long getUserPlayer2ID() {
        return userPlayer2ID;
    }

    public void setUserPlayer2ID(Long userPlayer2ID) {
        this.userPlayer2ID = userPlayer2ID;
    }

    public void setVictor(Long victor){
        this.victor = victor;
    }

    public void setLastMove(Long lastMove){
        this.lastMove = lastMove;
    }

    public String getBoardStatus() {
        return boardStatus;
    }

    public void setBoardStatus(String boardStatus) {
        this.boardStatus = boardStatus;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

}
