package com.uhk.sergede1.webgameappbackend.rest.tokenized.chat;

import com.uhk.sergede1.webgameappbackend.database_service.DatabaseOperationException;
import com.uhk.sergede1.webgameappbackend.database_service.DatabaseService;
import com.uhk.sergede1.webgameappbackend.database_service.exceptions.UserNotFoundException;
import com.uhk.sergede1.webgameappbackend.model.Chat;
import com.uhk.sergede1.webgameappbackend.model.Message;
import com.uhk.sergede1.webgameappbackend.rest.tokenized.friend.MessageRequestBody;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MessageController {
    private final DatabaseService databaseService;

    public MessageController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @PostMapping(path = "/api/send-message")
    public  void  send_message(@RequestBody MessageRequestBody messageRequestBody) {

        // Get the token used for authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt token = (Jwt) authentication.getCredentials();
        System.out.println("Message sent with token: "+token.getTokenValue() );
        try {
            Long user_id = this.databaseService.getUserIdFromToken(token.getTokenValue());

            Long chat_id = messageRequestBody.chatID();
            Chat chat = this.databaseService.getChatByID(chat_id);
            if (chat.userIDPresentInChat(user_id)){
                // check if user is present in chat
                Long sender_id = messageRequestBody.senderUserID();
                if (sender_id.equals(user_id)){
                    // avoid user impersonation
                    String text = messageRequestBody.message();
                    databaseService.appendNewMessage(sender_id, chat_id, text);
                } else {
                    System.out.println("WARNING: User[" + user_id + "] is trying to steal another user identity!");
                }
            }

        } catch(UserNotFoundException e){
            System.out.println("ERROR: User token not found in token database");
        }



    }

    @GetMapping("/api/get-chat-messages/{chatId}")
    public List<Message> getChatMessages(@PathVariable("chatId")  Long chat_id) throws DatabaseOperationException {
        Chat chat = this.databaseService.getChatByID(chat_id);

        // Get the token used for authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt token = (Jwt) authentication.getCredentials();
        try {
            Long user_id = this.databaseService.getUserIdFromToken(token.getTokenValue());
            if(chat.userIDPresentInChat(user_id)){
                return databaseService.getMessagesForChat(chat_id);
            }
            System.out.println("WARNING: User["+user_id+"] is trying load someone else's chat!");
            return null;
        } catch(UserNotFoundException e){
            System.out.println("ERROR: User token not found in token database");
        }
        return null;
    }
}
