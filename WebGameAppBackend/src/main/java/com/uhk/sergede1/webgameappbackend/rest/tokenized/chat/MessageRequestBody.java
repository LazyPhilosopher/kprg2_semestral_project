package com.uhk.sergede1.webgameappbackend.rest.tokenized.chat;

public record MessageRequestBody(Long senderUserID, Long chatID, String text){}
