package com.uhk.sergede1.webgameappbackend.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "MESSAGES")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "SENDERUSERID", nullable = false)
    private Long senderUserID;

    @Column(name = "TEXT", nullable = false, columnDefinition = "nvarchar(MAX)")
    private String text;

    @Column(name = "TIMESTAMP", nullable = false)
    private Timestamp timestamp;

    @Column(name = "CHATID", nullable = false)
    private Long chatID;

    // Constructors
    public Message() {
    }

    public Message(Long senderUserID, String text, Timestamp timestamp, Long chatID) {
        this.senderUserID = senderUserID;
        this.text = text;
        this.timestamp = timestamp;
        this.chatID = chatID;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderUserID() {
        return senderUserID;
    }

    public void setSenderUserID(Long senderUserID) {
        this.senderUserID = senderUserID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Long getChatID() {
        return chatID;
    }

    public void setChatID(Long chatID) {
        this.chatID = chatID;
    }
}
