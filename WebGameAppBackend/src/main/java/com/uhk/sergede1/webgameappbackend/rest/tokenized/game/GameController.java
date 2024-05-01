package com.uhk.sergede1.webgameappbackend.rest.tokenized.game;

import com.uhk.sergede1.webgameappbackend.database_service.DatabaseOperationException;
import com.uhk.sergede1.webgameappbackend.database_service.DatabaseService;
import com.uhk.sergede1.webgameappbackend.database_service.exceptions.UserNotFoundException;
import com.uhk.sergede1.webgameappbackend.model.Chat;
import com.uhk.sergede1.webgameappbackend.model.GameRound;
import com.uhk.sergede1.webgameappbackend.model.Message;
import com.uhk.sergede1.webgameappbackend.model.User;
import com.uhk.sergede1.webgameappbackend.rest.tokenized.friend.MessageRequestBody;
import com.uhk.sergede1.webgameappbackend.rest.tokenized.friend.SingleUserRequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class GameController {
    private final DatabaseService databaseService;

    public GameController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @GetMapping("/api/new-game/{user1id}/{user2id}")
    public void newGameRequest(@PathVariable("user1id")  Long user1_id,
                                    @PathVariable("user2id")  Long user2_id) throws DatabaseOperationException {
        GameRound game = databaseService.fetchTwoPlayerRound(user1_id, user2_id);
        if(game == null){
            databaseService.performNewGameRequest(user1_id, user2_id);
        }
    }

    @GetMapping("/api/surrender/{user1id}/{user2id}")
    public void SurrenderRequest(@PathVariable("user1id")  Long user1_id,
                               @PathVariable("user2id")  Long user2_id) throws DatabaseOperationException {
//        GameRound game = databaseService.fetchTwoPlayerRound(user1_id, user2_id);
//        if(game != null){
            databaseService.performSurrenderRequest(user1_id, user2_id);
//        }
    }

    @GetMapping("/api/get-board/{user1id}/{user2id}")
    public BoardResponse getChatMessages(@PathVariable("user1id")  Long user1_id,
                                         @PathVariable("user2id")  Long user2_id) throws DatabaseOperationException {
        GameRound game = databaseService.fetchTwoPlayerRound(user1_id, user2_id);
        if(game == null){
//            databaseService.createTwoPlayerRound(user1_id, user2_id);
//            game = databaseService.fetchTwoPlayerRound(user1_id, user2_id);
            return null;
        }
        return new BoardResponse(databaseService.getBoardConfiguration(game), game.isActive(), game.getLastMove());
    }

    @PostMapping("/api/set-board-cell")
    public void set_board_cell(@RequestBody BoardControlRequestBody boardControlRequestBody){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt token = (Jwt) authentication.getCredentials();
        System.out.println("Message sent with token: "+token.getTokenValue() );

        try {
            Long token_user_id = this.databaseService.getUserIdFromToken(token.getTokenValue());

            Long request_user_id = boardControlRequestBody.userID();

            // Missing security checks to implement
            if (token_user_id.equals(request_user_id)){
                GameRound game = databaseService.fetchTwoPlayerRound(request_user_id, boardControlRequestBody.opponentID());
                if(game.getLastMove().equals(request_user_id)){
                    // player already moved
                }
                Long game_id = game.getId();
                int row = boardControlRequestBody.row();
                int column = boardControlRequestBody.column();
                databaseService.takeBoardCell(game_id, request_user_id, row, column);
            }

        } catch(UserNotFoundException e){
            System.out.println("ERROR: User token not found in token database");
        }
    }

    @GetMapping(path = "/api/get-pending-new-game-invitations/{userid}")
    public ResponseEntity<List<User>> get_pending_game_invitations(@PathVariable("userid")  Long userID){
        List<Long> request_user_ids = databaseService.getNewGameInvitationList(userID);

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
}
