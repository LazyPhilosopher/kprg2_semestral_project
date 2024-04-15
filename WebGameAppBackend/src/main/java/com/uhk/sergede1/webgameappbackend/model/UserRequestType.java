package com.uhk.sergede1.webgameappbackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "TYPE")
public class UserRequestType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ABBREVIATION", nullable = false)
    private String abbreviation;

    @Column(name = "DESCRIPTION")
    private String description;

    public UserRequestType() {
    }

    public UserRequestType(String abbreviation, String description) {
        this.abbreviation = abbreviation;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }
}
