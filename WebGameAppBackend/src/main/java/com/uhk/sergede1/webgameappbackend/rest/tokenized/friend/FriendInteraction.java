package com.uhk.sergede1.webgameappbackend.rest.tokenized.friend;

import com.uhk.sergede1.webgameappbackend.database_service.DatabaseOperationException;
import com.uhk.sergede1.webgameappbackend.database_service.DatabaseService;
import com.uhk.sergede1.webgameappbackend.model.User;
import com.uhk.sergede1.webgameappbackend.rest.tokenized.search.UserSearchRequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class FriendInteraction {
    private final DatabaseService databaseService;

    public FriendInteraction(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @PostMapping(path = "/api/invite-user-to-friendlist")
    public void invite_user_to_friendlist(@RequestBody FriendRequestResuestBody friendRequestResuestBody){
        Long sender_user_id = friendRequestResuestBody.senderUserID();
        Long receiver_user_id = friendRequestResuestBody.receiverUserID();
//        System.out.println("searchUserByUsername:"+username);
//        List<User> users = new ArrayList<>();
        databaseService.addPendingFriendInvitation(sender_user_id, receiver_user_id);
//        try {
//            users = databaseService.searchUserByUsername(username);
//        } catch (DatabaseOperationException e){
//            System.out.println("Error occured during search for user '"+username+"'! Exception: "+e);
//        }
//        return users;
    }

    @PostMapping(path = "/api/get-pending-friend-invitations")
    public ResponseEntity<List<User>> get_pending_invitations(@RequestBody NotificationListRequestBody notificationListRequestBody){
        Long sender_user_id = notificationListRequestBody.senderUserID();
        List<Long> request_user_ids = databaseService.getFriendInvitationList(sender_user_id);

        List<User> users = new ArrayList<>();

        for (Long userId : request_user_ids) {
            Optional<User> userOptional = databaseService.findById(userId, User.class);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setPassword(null); // Set password to null
                users.add(user);
            }
        }

        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(users);
        }
    }
}
