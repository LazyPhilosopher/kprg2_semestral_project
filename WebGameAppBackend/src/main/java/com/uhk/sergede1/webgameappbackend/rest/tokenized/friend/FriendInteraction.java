package com.uhk.sergede1.webgameappbackend.rest.tokenized.friend;

import com.uhk.sergede1.webgameappbackend.database_service.DatabaseOperationException;
import com.uhk.sergede1.webgameappbackend.database_service.DatabaseService;
import com.uhk.sergede1.webgameappbackend.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class FriendInteraction {
    private final DatabaseService databaseService;

    public FriendInteraction(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @PostMapping(path = "/api/invite-user-to-friendlist")
    public void invite_user_to_friendlist(@RequestBody TwoUserResuestBody twoUserResuestBody){
        Long sender_user_id = twoUserResuestBody.senderUserID();
        Long receiver_user_id = twoUserResuestBody.receiverUserID();
//        System.out.println("searchUserByUsername:"+username);
//        List<User> users = new ArrayList<>();
        databaseService.addPendingFriendRequest(sender_user_id, receiver_user_id);
//        try {
//            users = databaseService.searchUserByUsername(username);
//        } catch (DatabaseOperationException e){
//            System.out.println("Error occured during search for user '"+username+"'! Exception: "+e);
//        }
//        return users;
    }

    @PostMapping(path = "/api/get-pending-friend-invitations")
    public ResponseEntity<List<User>> get_pending_invitations(@RequestBody SingleUserRequestBody singleUserRequestBody){
        Long sender_user_id = singleUserRequestBody.senderUserID();
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
            return ResponseEntity.ok(new ArrayList<>());
        } else {
            return ResponseEntity.ok(users);
        }
    }

    @PostMapping(path = "/api/accept-friend-invitation")
    public void accept_friend_invitation(@RequestBody TwoUserResuestBody twoUserResuestBody) throws DatabaseOperationException {
        Long sender_user_id = twoUserResuestBody.senderUserID();
        Long receiver_user_id = twoUserResuestBody.receiverUserID();

        if(databaseService.checkFriendInvitationPresent(receiver_user_id, sender_user_id)){
            databaseService.addFriendToUser(receiver_user_id, sender_user_id);
            databaseService.addFriendToUser(sender_user_id, receiver_user_id);
            databaseService.dropPendingFriendRequest(receiver_user_id, sender_user_id);
        } else {
            System.out.println("Friend invitation doesn't exist in Pending table");
        }
    }

    @PostMapping(path = "/api/get-friend-list")
    public  ResponseEntity<List<User>>  accept_friend_invitation(@RequestBody SingleUserRequestBody singleUserResuestBody) throws DatabaseOperationException {
        Long sender_user_id = singleUserResuestBody.senderUserID();

        List<User> users =  databaseService.getUserFriendList(sender_user_id);

        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(users);
        }
    }
}
