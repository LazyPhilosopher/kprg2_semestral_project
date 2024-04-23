package com.uhk.sergede1.webgameappbackend.rest.tokenized.chat;

import com.uhk.sergede1.webgameappbackend.database_service.DatabaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    private final DatabaseService databaseService;

    public ChatController(DatabaseService databaseService){
        this.databaseService = databaseService;
    }

    @GetMapping("/api/get-chat-id/{senderId}/{receiverId}")
    public ResponseEntity<Long> getTwoUserChatId(
            @PathVariable("senderId") Long senderId,
            @PathVariable("receiverId") Long receiverId) {

        System.out.println("Sender: " + senderId + " Receiver: " + receiverId);
        Long chatId = databaseService.getTwoUserChatID(senderId, receiverId);  // replace with your logic

        return ResponseEntity.ok(chatId);
    }
}
