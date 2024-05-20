package com.uhk.sergede1.webgameappbackend.rest.tokenized.game;

import com.uhk.sergede1.webgameappbackend.database_service.exceptions.DatabaseOperationException;
import com.uhk.sergede1.webgameappbackend.database_service.DatabaseService;
import com.uhk.sergede1.webgameappbackend.database_service.exceptions.UserNotFoundException;
import com.uhk.sergede1.webgameappbackend.model.GameRound;
import com.uhk.sergede1.webgameappbackend.model.User;
import com.uhk.sergede1.webgameappbackend.utils.Serializer;
import org.springframework.dao.EmptyResultDataAccessException;
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
        GameRound game;
        try{
            game = databaseService.fetchTwoPlayerRound(user1_id, user2_id);
        } catch (EmptyResultDataAccessException e){
            game = null;
        }
        if(game == null || !game.isActive()){
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
    public BoardResponse getGameStatus(@PathVariable("user1id")  Long user1_id,
                                         @PathVariable("user2id")  Long user2_id) throws DatabaseOperationException {
        GameRound game;
        try{
            game = databaseService.fetchTwoPlayerRound(user1_id, user2_id);
        } catch (EmptyResultDataAccessException e){
            game = null;
        }
        if(game == null){
//            databaseService.createTwoPlayerRound(user1_id, user2_id);
//            game = databaseService.fetchTwoPlayerRound(user1_id, user2_id);
            return null;
        }
        return new BoardResponse(databaseService.getBoardConfiguration(game),
                game.isActive(),
                game.getLastMove(),
                game.getVictor(),
                game.getUserPlayer1ID(),
                game.getUserPlayer2ID(),
                game.getTimestamp());
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
                if(!game.isActive()){
                    return;
                }
                if(!game.getLastMove().equals(request_user_id)){
                    Long game_id = game.getId();
                    int row = boardControlRequestBody.row();
                    int column = boardControlRequestBody.column();
                    game = databaseService.fetchTwoPlayerRound(request_user_id, boardControlRequestBody.opponentID());
                    //check if board cell is already taken
                    if (databaseService.getBoardConfiguration(game)[row][column] != ' '){
                        return;
                    }
                    databaseService.takeBoardCell(game_id, request_user_id, row, column);

                    Serializer<char[][]> serializer = new Serializer<>();
                    game = databaseService.fetchTwoPlayerRound(request_user_id, boardControlRequestBody.opponentID());
                    Long victor = checkGameVictor(serializer.deserialize(game.getBoardStatus()), game.getUserPlayer1ID(), game.getUserPlayer2ID());
                    if (victor != null) {
                        databaseService.setGameVictor(game_id, victor);
                        databaseService.setGameActive(game_id, false);
                    }

                }
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

    private Long checkGameVictor(char[][] array, long firstOutput, long secondOutput) {
        int rows = array.length;
        int cols = array[0].length;

        // Check rows
        for (int i = 0; i < rows; i++) {
            int countX = 0;
            int countO = 0;
            for (int j = 0; j < cols; j++) {
                if (array[i][j] == 'X') {
                    countX++;
                    countO = 0; // Reset countO
                } else if (array[i][j] == 'O') {
                    countO++;
                    countX = 0; // Reset countX
                } else {
                    countX = 0; // Reset countX
                    countO = 0; // Reset countO
                }

                // Check if countX or countO is 3 or more
                if (countX >= 3) {
                    return firstOutput;
                } else if (countO >= 3) {
                    return secondOutput;
                }
            }
        }

        // Check columns
        for (int j = 0; j < cols; j++) {
            int countX = 0;
            int countO = 0;
            for (int i = 0; i < rows; i++) {
                if (array[i][j] == 'X') {
                    countX++;
                    countO = 0; // Reset countO
                } else if (array[i][j] == 'O') {
                    countO++;
                    countX = 0; // Reset countX
                } else {
                    countX = 0; // Reset countX
                    countO = 0; // Reset countO
                }

                // Check if countX or countO is 3 or more
                if (countX >= 3) {
                    return firstOutput;
                } else if (countO >= 3) {
                    return secondOutput;
                }
            }
        }

        // Check diagonals (from top-left to bottom-right)
        for (int i = 0; i <= rows - 3; i++) {
            for (int j = 0; j <= cols - 3; j++) {
                if (array[i][j] == 'X' && array[i + 1][j + 1] == 'X' && array[i + 2][j + 2] == 'X') {
                    return firstOutput;
                } else if (array[i][j] == 'O' && array[i + 1][j + 1] == 'O' && array[i + 2][j + 2] == 'O') {
                    return secondOutput;
                }
            }
        }

        // Check diagonals (from top-right to bottom-left)
        for (int i = 0; i <= rows - 3; i++) {
            for (int j = 2; j < cols; j++) {
                if (array[i][j] == 'X' && array[i + 1][j - 1] == 'X' && array[i + 2][j - 2] == 'X') {
                    return firstOutput;
                } else if (array[i][j] == 'O' && array[i + 1][j - 1] == 'O' && array[i + 2][j - 2] == 'O') {
                    return secondOutput;
                }
            }
        }

        // If no sequence of 3 or more X or O is found, return null
        return null;
    }

    @GetMapping("/api/get-top-player-list")
    public ResponseEntity<List<TopPlayerStats>> getTopPlayerRanking() {
        List<TopPlayerStats> topPlayers = databaseService.getTopPlayerRankingView();

        return ResponseEntity.ok().body(topPlayers);
    }
}
