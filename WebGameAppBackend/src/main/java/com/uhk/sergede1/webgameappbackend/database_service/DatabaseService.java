package com.uhk.sergede1.webgameappbackend.database_service;

import com.uhk.sergede1.webgameappbackend.database_service.exceptions.UserNotFoundException;
import com.uhk.sergede1.webgameappbackend.model.*;
import com.uhk.sergede1.webgameappbackend.utils.Serializer;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DatabaseService {

    private final JdbcTemplate jdbcTemplate;
    private static final SecureRandom random = new SecureRandom();
    private HashMap<String, Long> token_user_id_map = new HashMap<>();

    @Autowired
    public DatabaseService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public void performInitialCheck(){
        String sql = "SELECT * FROM USERS";

        List<User> user_list;
        try {
            user_list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
            System.out.println(user_list);
            for (User user : user_list) {
                try{
                    getFriendUserIDs(user.getId());
                }catch (Exception e){
                    // no friend table for given user exists
                    // create new friend list for given user
                    Set<String> set = new HashSet<>();
                    Serializer<Set<String>> serializer = new Serializer<>();
                    jdbcTemplate.update("INSERT INTO FriendRelations (UserID, friendUserIDs) VALUES (?, ?)",
                            user.getId(), serializer.serialize(set));
                }
            }
        } catch (Exception e){
            System.out.println("Error occured during getting user list: " + e);
        }
    }

    public void saveUserToken(Long user_id, String token){
        this.token_user_id_map.put(token, user_id);
    }

    public Long getUserIdFromToken(String token) throws UserNotFoundException {
        Long userId = this.token_user_id_map.get(token);
        if (userId == null) {
            throw new UserNotFoundException("User not found for token: " + token);
        }
        return userId;
    }

    // Generic method to find a single record by ID
    public <T> Optional<T> findById(Long id, Class<T> type) {
        String tableName = type.getSimpleName().toLowerCase() + "s"; // Assuming table names are plural
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        return jdbcTemplate.query(sql, new Object[]{id}, new BeanPropertyRowMapper<>(type))
                .stream()
                .findFirst();
    }

    // Generic method to find records by a specific column
    public <T> Optional<T> findByColumn(String columnName, Object value, Class<T> type) {
        String tableName = type.getSimpleName().toLowerCase() + "s"; // Assuming table names are plural
        String sql = "SELECT * FROM " + tableName + " WHERE " + columnName + " = ?";
        return jdbcTemplate.query(sql, new Object[]{value}, new BeanPropertyRowMapper<>(type))
                .stream()
                .findFirst();
    }

    // Generic method to save or update a record
    public <T> void saveOrUpdate(T entity) {
        String tableName = entity.getClass().getSimpleName().toLowerCase() + "s"; // Assuming table names are plural
        String sql = "INSERT INTO " + tableName + " VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE username = ?, password = ?, role = ?";
        // Adjust the SQL query based on your table structure
        Object[] params = { /* Extract properties from the entity here */ };
        jdbcTemplate.update(sql, params);
    }

    public Optional<User> findUserByUsername(String username) {
        String sql = "SELECT * FROM USERS WHERE username = ?";
        Object[] params = {username};

        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(User.class))
                .stream()
                .findFirst();
    }

    public void saveUser(User user) throws DatabaseOperationException {
        Optional<User> userOptional = this.findUserByUsername(user.getUsername());

        if (userOptional.isPresent()) {
            throw new DatabaseOperationException("User already exists");
        } else {
            String users_insert_sql = "INSERT INTO Users (username, password, role) VALUES (?, ?, 'User')";
            String friend_relation_insert_sql = "INSERT INTO FriendRelations (UserID, friendUserIDs) VALUES (?, ?)";

            try {
                jdbcTemplate.update(users_insert_sql, user.getUsername(), user.getPassword());

                Optional<User> optional_new_user = findUserByUsername(user.getUsername());
                User new_user = optional_new_user.get();

                // Initialize friendUserIDs as an empty set and serialize it
                Set<String> set = new HashSet<>();
                Serializer<Set<String>> serializer = new Serializer<>();
                jdbcTemplate.update(friend_relation_insert_sql, new_user.getId(), serializer.serialize(set));

            } catch (DataAccessException e) {
                throw new DatabaseOperationException("Error inserting user into database", e);
            }
        }
    }

    public Set<String> getUserFriendIds(Long userId) throws DatabaseOperationException {
        List<String> currentFriends = jdbcTemplate.query(
                "SELECT friendUserIDs FROM FriendRelations WHERE UserID = ?",
                new Object[]{userId},
                (ResultSet rs, int rowNum) -> rs.getString("friendUserIDs")
        );

        if (currentFriends.isEmpty()) {
            throw new DatabaseOperationException("User not found");
        }

        String serializedFriendUserIDs = currentFriends.get(0);
        Serializer<Set<String>> serializer = new Serializer<>();
        return serializer.deserialize(serializedFriendUserIDs);
    }

    public void addFriendToUser(Long userId, Long friendId) throws DatabaseOperationException {
        String sql = "UPDATE FriendRelations SET friendUserIDs = ? WHERE UserID = ?";

        try {
            // Retrieve user's current friendUserIDs string
            Set<String> set = getUserFriendIds(userId);

            set.add(friendId.toString());
            Serializer<Set<String>> serializer = new Serializer<>();
            jdbcTemplate.update(sql, serializer.serialize(set), userId);

        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error adding friend to user", e);
        }
    }

    public void removeFriendFromUser(Long userId, Long friendId) throws DatabaseOperationException {
        String sql = "UPDATE FriendRelations SET friendUserIDs = ? WHERE UserID = ?";

        try {
            // Retrieve user's current friendUserIDs string
            Set<String> set = getUserFriendIds(userId);

            set.remove(friendId.toString());
            Serializer<Set<String>> serializer = new Serializer<>();
            jdbcTemplate.update(sql, serializer.serialize(set), userId);

        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error adding friend to user", e);
        }
    }

    public Set<String> getFriendUserIDs(Long userId) throws DatabaseOperationException {
        try {
            // Retrieve current friendUserIDs string
            List<String> currentFriends = jdbcTemplate.query(
                    "SELECT friendUserIDs FROM FriendRelations WHERE UserID = ?",
                    new Object[]{userId},
                    (ResultSet rs, int rowNum) -> rs.getString("friendUserIDs")
            );

            if (currentFriends.isEmpty()) {
                throw new DatabaseOperationException("User not found");
            }

            String serializedFriendUserIDs = currentFriends.get(0);
            byte[] bytes = java.util.Base64.getDecoder().decode(serializedFriendUserIDs);

            // Create a ByteArrayInputStream
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

            // Create an ObjectInputStream
            ObjectInputStream ois = new ObjectInputStream(bais);

            // Read the HashSet from the ObjectInputStream
            Set<String> set = (HashSet<String>) ois.readObject();

            return set;

        } catch (DataAccessException | NumberFormatException e) {
            throw new DatabaseOperationException("Error retrieving friendUserIDs from database", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> findAllUsers() throws DatabaseOperationException {
        String sql = "SELECT * FROM USERS";

        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error retrieving users from database", e);
        }
    }

    public List<User> searchUserByUsername(String username) throws DatabaseOperationException {
        String sql = "SELECT * FROM Users WHERE username LIKE ?";

        try {
            return jdbcTemplate.query(sql, new Object[]{"%" + username + "%"}, new BeanPropertyRowMapper<>(User.class));
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error retrieving users from database", e);
        }
    }

    public void addPendingFriendRequest(Long senderUserId, Long receiverUserId){
        if(checkFriendInvitationPresent(senderUserId, receiverUserId)){
            System.out.println("Friend invitation already present in Pending table");
        } else {
            UserRequestType friend_request_type = jdbcTemplate.queryForObject("SELECT * FROM Type WHERE abbreviation = 'friendrqst'",
                    new BeanPropertyRowMapper<>(UserRequestType.class));
            jdbcTemplate.update("INSERT INTO Pending (senderUserID, receiverUserID, type_int) VALUES (?, ?, ?)",
                    senderUserId, receiverUserId, friend_request_type.getId());
        }
    }

    public void dropPendingFriendRequest(Long senderUserId, Long receiverUserId){
        if(checkFriendInvitationPresent(senderUserId, receiverUserId)){
            UserRequestType friend_request_type = jdbcTemplate.queryForObject("SELECT * FROM Type WHERE abbreviation = 'friendrqst'",
                    new BeanPropertyRowMapper<>(UserRequestType.class));
            jdbcTemplate.update("DELETE FROM Pending WHERE senderUserID = ? AND receiverUserID = ? AND type_int = ?",
                    senderUserId, receiverUserId, friend_request_type.getId());
        } else {
            System.out.println("Friend invitation not present in Pending table");
        }
    }

    public void acceptFriendInvitation(Long senderUserId, Long receiverUserId) throws DatabaseOperationException {
        if(checkFriendInvitationPresent(receiverUserId, senderUserId)){
            addFriendToUser(senderUserId, receiverUserId);
        } else {
            System.out.println("Friend invitation doesn't exist in Pending table");
        }
    }

    public boolean checkFriendInvitationPresent(Long senderUserId, Long receiverUserId){
        UserRequestType friend_request_type = jdbcTemplate.queryForObject("SELECT * FROM Type WHERE abbreviation = 'friendrqst'",
                new BeanPropertyRowMapper<>(UserRequestType.class));
        Long friend_request_type_int = friend_request_type.getId();

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Pending WHERE senderUserID = ? AND receiverUserID = ? AND type_int = ?",
                Integer.class, senderUserId, receiverUserId, friend_request_type_int);

        return count != null && count > 0;
    }

    public List<Long> getFriendInvitationList(Long senderUserId){
        UserRequestType friend_request_type = jdbcTemplate.queryForObject("SELECT * FROM Type WHERE abbreviation = 'friendrqst'",
                new BeanPropertyRowMapper<>(UserRequestType.class));
        Long friend_request_type_int = friend_request_type.getId();

        List<PendingRequest> pending_requests = jdbcTemplate.query("SELECT * FROM Pending WHERE receiverUserID = ? AND type_int = ?",
                new BeanPropertyRowMapper<>(PendingRequest.class), senderUserId, friend_request_type_int);

        return pending_requests.stream()
                .map(PendingRequest::getSenderUserID)
                .collect(Collectors.toList());
    }

    public List<Long> getNewGameInvitationList(Long senderUserId){
        UserRequestType new_game_request_type = jdbcTemplate.queryForObject("SELECT * FROM Type WHERE abbreviation = 'nwgmrqst'",
                new BeanPropertyRowMapper<>(UserRequestType.class));
        Long new_game_request_type_int = new_game_request_type.getId();

        List<PendingRequest> pending_requests = jdbcTemplate.query("SELECT * FROM Pending WHERE receiverUserID = ? AND type_int = ?",
                new BeanPropertyRowMapper<>(PendingRequest.class), senderUserId, new_game_request_type_int);

        return pending_requests.stream()
                .map(PendingRequest::getSenderUserID)
                .collect(Collectors.toList());
    }
    public List<User> getUserFriendList(Long senderUserId) throws DatabaseOperationException {
        String sql = "SELECT * FROM Users WHERE id = ?";

        Set<String> userFriendIds = getUserFriendIds(senderUserId);
        List<User> userFriendList = new ArrayList<>();

        for (String friendId : userFriendIds) {
            userFriendList.add(jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class),  friendId));
        }
        return userFriendList;
    }

    @Transactional
    public Long getTwoUserChatID(Long user1, Long user2) {
        // Query for an existing chat between the two users
        Chat chat = findExistingChat(user1, user2);

        // If a chat exists, return its ID
        if (chat != null) {
            return chat.getId();
        }

        // If no chat exists, create a new chat and return its ID
        Chat newChat = createNewChat(user1, user2);

        return newChat.getId();
    }

    private Chat findExistingChat(Long user1, Long user2) {
        // Query for an existing chat between the two users
        try {
            Chat chat = jdbcTemplate.queryForObject(
                    "SELECT * FROM CHATS WHERE (USER1ID = ? AND USER2ID = ?) OR (USER1ID = ? AND USER2ID = ?)",
                    new BeanPropertyRowMapper<>(Chat.class), user1, user2, user2, user1);
            return chat;
        } catch (Exception e) {
            return null;
        }
    }

    private Chat createNewChat(Long user1, Long user2) {
        // Create a new chat and return its ID
        // Here you can implement your logic to generate a new chat ID
//        Long newChatId = generateNewChatId();  // Replace with your logic to generate a chat ID

        jdbcTemplate.update(
                "INSERT INTO CHATS (USER1ID, USER2ID) VALUES (?, ?)",
                user1, user2);

        // Retrieve the newly created Chat object from the database
        Chat newChat = jdbcTemplate.queryForObject(
                "SELECT * FROM CHATS WHERE (USER1ID = ? AND USER2ID = ?) OR (USER1ID = ? AND USER2ID = ?)",
                new BeanPropertyRowMapper<>(Chat.class),
                user1, user2, user2, user1);

        return newChat;
    }

    public Chat getChatByID(Long chat_id){
        try {
            Chat chat = jdbcTemplate.queryForObject(
                    "SELECT * FROM CHATS WHERE ID = ?",
                    new BeanPropertyRowMapper<>(Chat.class),chat_id);
            return chat;
        } catch (Exception e) {
            return null;
        }
    }

    public void appendNewMessage(Long sender_id, Long chat_id, String text){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        jdbcTemplate.update(
                "INSERT INTO MESSAGES (SENDERUSERID, TEXT, TIMESTAMP, CHATID) VALUES (?, ?, ?, ?)",
                sender_id, text, timestamp,  chat_id);
    }

    public List<Message> getMessagesForChat(Long chatId) throws DatabaseOperationException {

        String sql = "SELECT * FROM Messages WHERE chatID = ?";

        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Message.class), chatId);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Error retrieving users from database", e);
        }
    }

    public GameRound fetchTwoPlayerRound(Long user1id, Long user2id){
        try {
            GameRound game = jdbcTemplate.queryForObject(
                    "SELECT TOP 1 * FROM GAMEROUNDS WHERE (userPlayer1ID = ? AND userPlayer2ID = ?) OR (userPlayer1ID = ? AND userPlayer2ID = ?) ORDER BY timestamp DESC",
                    new BeanPropertyRowMapper<>(GameRound.class), user1id, user2id, user2id, user1id);
            return game;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public GameRound fetchTwoPlayerRound(Long game_id){
        try {
            GameRound game = jdbcTemplate.queryForObject(
                    "SELECT * FROM GAMEROUNDS WHERE (id = ?)",
                    new BeanPropertyRowMapper<>(GameRound.class), game_id);
            return game;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Transactional
    public void createTwoPlayerRound(Long user1id, Long user2id) {
        char[][] board_matrix = new char[8][8];
        Serializer<char[][]> serializer = new Serializer<>();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board_matrix[i][j] = ' ';
            }
        }

        try {
            // Delete previous game
//            String deleteQuery = "DELETE FROM GAMEROUNDS " +
//                    "WHERE (userPlayer1ID = ? AND userPlayer2ID = ?) OR " +
//                    "(userPlayer1ID = ? AND userPlayer2ID = ?)";
//            jdbcTemplate.update(deleteQuery, user1id, user2id, user2id, user1id);

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String insertQuery = "INSERT INTO GAMEROUNDS (userPlayer1ID, userPlayer2ID, boardStatus, active, lastMove, timestamp) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertQuery, user1id, user2id, serializer.serialize(board_matrix), true, user2id, timestamp);
        } catch (Exception e) {
            System.out.println(e);
            // Handle exception
        }
    }

    public char[][] getBoardConfiguration(GameRound game){
        Serializer<char[][]> serializer = new Serializer<>();
        return serializer.deserialize(game.getBoardStatus());
    }

    public void setBoardConfiguration(Long gameID, char sign, int row, int column){
        GameRound game = this.fetchTwoPlayerRound(gameID);

        Serializer<char[][]> serializer = new Serializer<>();
        char [][] game_configuration = serializer.deserialize(game.getBoardStatus());
        game_configuration[row][column] = sign;

        String sql = "UPDATE GameRounds SET BoardStatus = ? WHERE ID = ?";
        jdbcTemplate.update(sql, serializer.serialize(game_configuration), gameID);
    }

    public void takeBoardCell(Long gameID, Long playerID, int row, int column){
        GameRound game = this.fetchTwoPlayerRound(gameID);

        if(game.getUserPlayer1ID().equals(playerID)){
            setBoardConfiguration(gameID, 'X', row, column);
            String sql = "UPDATE GameRounds SET lastMove = ? WHERE ID = ?";
            jdbcTemplate.update(sql, game.getUserPlayer1ID(), gameID);
        } else {
            setBoardConfiguration(gameID, 'O', row, column);
            String sql = "UPDATE GameRounds SET lastMove = ? WHERE ID = ?";
            jdbcTemplate.update(sql, game.getUserPlayer2ID(), gameID);
        }
    }

    public boolean checkPendingItemPresent(String abbreviation, Long senderUserId, Long receiverUserId){
        UserRequestType friend_request_type = jdbcTemplate.queryForObject("SELECT * FROM Type WHERE abbreviation = ?",
                new BeanPropertyRowMapper<>(UserRequestType.class),  abbreviation);
        Long friend_request_type_int = friend_request_type.getId();

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Pending WHERE senderUserID = ? AND receiverUserID = ? AND type_int = ?",
                Integer.class, senderUserId, receiverUserId, friend_request_type_int);

        return count != null && count > 0;
    }
    public void performNewGameRequest(Long senderID, Long receiverID){
        String abbreviation = "nwgmrqst";
        UserRequestType new_game_request_type = jdbcTemplate.queryForObject("SELECT * FROM Type WHERE abbreviation = ?",
                new BeanPropertyRowMapper<>(UserRequestType.class), abbreviation);

        if (checkPendingItemPresent(abbreviation, receiverID, senderID)){
            // remove New Game Invitation
            jdbcTemplate.update("DELETE FROM Pending WHERE senderUserID = ? AND receiverUserID = ? AND type_int = ?",
                    receiverID, senderID, new_game_request_type.getId());
            jdbcTemplate.update("DELETE FROM Pending WHERE senderUserID = ? AND receiverUserID = ? AND type_int = ?",
                    senderID, receiverID, new_game_request_type.getId());

            // create new game
            createTwoPlayerRound(receiverID, senderID);

        } else if (!checkPendingItemPresent(abbreviation, senderID, receiverID)) {
            // create New Game Invitation
            jdbcTemplate.update("INSERT INTO Pending (senderUserID, receiverUserID, type_int) VALUES (?, ?, ?)",
                    senderID, receiverID, new_game_request_type.getId());
        }
    }

    public void performSurrenderRequest(Long senderID, Long receiverID){
        GameRound game = fetchTwoPlayerRound(senderID, receiverID);
        if(game != null){
            setGameVictor(game.getId(), receiverID);
            setGameActive(game.getId(), false);
        }
    }

    public void setGameVictor(Long GameID, Long victorID){
        jdbcTemplate.update("UPDATE GameRounds SET victor = ? WHERE ID = ?", victorID, GameID);
    }

    public void setGameActive(Long GameID, boolean status){
        jdbcTemplate.update("UPDATE GameRounds SET active = ? WHERE ID = ?", status, GameID);
    }
}
