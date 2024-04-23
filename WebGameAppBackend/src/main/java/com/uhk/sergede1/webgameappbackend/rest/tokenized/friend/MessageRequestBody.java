package com.uhk.sergede1.webgameappbackend.rest.tokenized.friend;

public record MessageRequestBody(Long senderUserID, Long chatID, String message){}
