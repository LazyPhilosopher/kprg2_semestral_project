package com.uhk.sergede1.webgameappbackend.rest.tokenized.chat;

import com.uhk.sergede1.webgameappbackend.database_service.DatabaseOperationException;
import com.uhk.sergede1.webgameappbackend.database_service.DatabaseService;
import com.uhk.sergede1.webgameappbackend.database_service.exceptions.UserNotFoundException;
import com.uhk.sergede1.webgameappbackend.model.Chat;
import com.uhk.sergede1.webgameappbackend.model.Message;
import com.uhk.sergede1.webgameappbackend.rest.tokenized.friend.MessageRequestBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageControllerTest {

    @InjectMocks
    private MessageController messageController;

    @Mock
    private DatabaseService databaseService;

    @Mock
    private Authentication authentication = Mockito.mock(Authentication.class);

    private Long chatId;
    private Long userId;
    private List<Message> messages;
    private Chat userChat;
    private Jwt token = Mockito.mock(Jwt.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);

    @Before
    public void setUp() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        chatId = 1L;
        userId = 1000L;
        messages = new ArrayList<>();
        messages.add(new Message());
        userChat = Mockito.mock(Chat.class);
        when(token.getTokenValue()).thenReturn("mock_user_token");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Set the SecurityContext to the SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testGetChatMessages_UserPresentInChat() throws UserNotFoundException, DatabaseOperationException {
        // Get token POST request was sent with
        when(authentication.getCredentials()).thenReturn(token);

        // User is present in token database
        when(databaseService.getUserIdFromToken(anyString())).thenReturn(userId);

        when(databaseService.getChatByID(chatId)).thenReturn(userChat);

        // token-based user identity is present in chat
        when(userChat.userIDPresentInChat(userId)).thenReturn(true);

        // chat contains messages
        when(databaseService.getMessagesForChat(chatId)).thenReturn(messages);

        // Call the method
        List<Message> result = messageController.getChatMessages(chatId);

        // Verify interactions and assert results
        assertEquals(messages, result);
    }

    @Test
    public void testGetChatMessages_UserNotPresentInChat() throws UserNotFoundException, DatabaseOperationException {
        // Get token POST request was sent with
        when(authentication.getCredentials()).thenReturn(token);

        // User is present in token database
        when(databaseService.getUserIdFromToken(anyString())).thenReturn(userId);

        when(databaseService.getChatByID(chatId)).thenReturn(userChat);

        // token-based user identity is NOT present in chat
        when(userChat.userIDPresentInChat(userId)).thenReturn(false);

        // Call the method
        List<Message> result = messageController.getChatMessages(chatId);

        // Verify interactions and assert results
        assertEquals(null, result);
    }

    @Test
    public void testGetChatMessages_UserNotPresentTokenDatabase() throws UserNotFoundException, DatabaseOperationException {
        // Get token POST request was sent with
        when(authentication.getCredentials()).thenReturn(token);

        // User is NOT present in token database
        when(databaseService.getUserIdFromToken(anyString())).thenThrow(new UserNotFoundException("User not found"));

        // Call the method
        List<Message> result = messageController.getChatMessages(chatId);

        // Verify interactions and assert results
        assertEquals(null, result);
    }

    @Test
    public void testSendMessage_Success() throws UserNotFoundException, DatabaseOperationException {
        // Mock authentication
        when(authentication.getCredentials()).thenReturn(token);

        // Mock SecurityContext
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // User is present in token database
        when(databaseService.getUserIdFromToken(anyString())).thenReturn(userId);

        // Mock database service behavior
        when(databaseService.getChatByID(chatId)).thenReturn(userChat);
        // token-based user identity is present in chat
        when(userChat.userIDPresentInChat(userId)).thenReturn(true);
        doNothing().when(databaseService).appendNewMessage(userId, chatId, "test message");

        // Call the method
        messageController.send_message(new MessageRequestBody(userId, chatId, "test message"));

        // Verify interactions
        verify(databaseService, times(1)).appendNewMessage(userId, chatId, "test message");
    }

    @Test
    public void testSendMessage_TokenUserIdNotEqualSenderId() throws UserNotFoundException, DatabaseOperationException {
        // Mock authentication
        when(authentication.getCredentials()).thenReturn(token);

        // Mock SecurityContext
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // User is present in token database
        when(databaseService.getUserIdFromToken(anyString())).thenReturn(userId);

        // Mock database service behavior
        when(databaseService.getChatByID(chatId)).thenReturn(userChat);
        // token-based user identity is present in chat
        when(userChat.userIDPresentInChat(userId)).thenReturn(true);

        // Call the method with different sender user ID
        messageController.send_message(new MessageRequestBody(5L, chatId, "test message"));

        // Verify interactions
        verify(databaseService, times(0)).appendNewMessage(userId, chatId, "test message");
    }

    @Test
    public void testSendMessage_UserTokenNotPresentInDatabase() throws UserNotFoundException, DatabaseOperationException {
        // Mock authentication
        when(authentication.getCredentials()).thenReturn(token);

        // Mock SecurityContext
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // User is present in token database
        when(databaseService.getUserIdFromToken(anyString())).thenThrow(new UserNotFoundException("User not found"));


        // Call the method
        messageController.send_message(new MessageRequestBody(userId, chatId, "test message"));

        // Verify interactions
        verify(databaseService, times(0)).appendNewMessage(userId, chatId, "test message");
    }
}
