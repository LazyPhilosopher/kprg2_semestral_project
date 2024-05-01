package com.uhk.sergede1.webgameappbackend.rest.tokenized.game;

public record BoardControlRequestBody(Long userID, Long opponentID, Integer row, Integer column){}