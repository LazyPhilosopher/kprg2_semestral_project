package com.uhk.sergede1.webgameappbackend.rest.tokenized.chat;

import com.uhk.sergede1.webgameappbackend.database_service.DatabaseOperationException;
import com.uhk.sergede1.webgameappbackend.database_service.DatabaseService;
import com.uhk.sergede1.webgameappbackend.rest.tokenized.friend.MessageRequestBody;
import com.uhk.sergede1.webgameappbackend.rest.tokenized.friend.TwoUserResuestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MessageController {
    private final DatabaseService databaseService;

    public MessageController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @PostMapping(path = "/api/send-message")
    public  void  send_message(@RequestBody MessageRequestBody messageRequestBody) throws DatabaseOperationException {
        Long sender_id = messageRequestBody.senderUserID();
        Long chat_id = messageRequestBody.chatID();
        String text = messageRequestBody.message();

        databaseService.appendNewMessage(sender_id, chat_id, text);

    }
}
