package com.uhk.sergede1.webgameappbackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "PENDING")
public class PendingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "senderUserID", nullable = false)
    private Long senderUserID;

    @Column(name = "receiverUserID", nullable = false)
    private Long receiverUserID;

    @Column(name = "type_int", nullable = false)
    private Integer typeInt;

    public PendingRequest() {
    }

    public PendingRequest(Long senderUserID, Long receiverUserID, Integer typeInt) {
        this.senderUserID = senderUserID;
        this.receiverUserID = receiverUserID;
        this.typeInt = typeInt;
    }

    public Long getSenderUserID() {
        return senderUserID;
    }

    public void setSenderUserID(Long senderUserID) {
        this.senderUserID = senderUserID;
    }

    public Long getReceiverUserID() {
        return receiverUserID;
    }

    public void setReceiverUserID(Long receiverUserID) {
        this.receiverUserID = receiverUserID;
    }

    public Integer getTypeInt() {
        return typeInt;
    }

    public void setTypeInt(Integer typeInt) {
        this.typeInt = typeInt;
    }
}
