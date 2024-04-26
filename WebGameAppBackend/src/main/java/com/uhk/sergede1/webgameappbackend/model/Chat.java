package com.uhk.sergede1.webgameappbackend.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "CHATS")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USER1ID", nullable = false)
    private Long user1ID;

    @Column(name = "USER2ID", nullable = false)
    private Long user2ID;

    // Constructors
    public Chat() {
    }

    public Chat(Long user1ID, Long user2ID) {
        this.user1ID = user1ID;
        this.user2ID = user2ID;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUser1ID() {
        return user1ID;
    }

    public void setUser1ID(Long user1ID) {
        this.user1ID = user1ID;
    }

    public Long getUser2ID() {
        return user2ID;
    }

    public void setUser2ID(Long user2ID) {
        this.user2ID = user2ID;
    }

    public boolean userIDPresentInChat(Long userID){
        return Objects.equals(this.user1ID, userID) || Objects.equals(this.user2ID, userID);
    }

}
