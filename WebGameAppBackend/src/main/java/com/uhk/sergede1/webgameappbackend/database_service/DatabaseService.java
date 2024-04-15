package com.uhk.sergede1.webgameappbackend.database_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.uhk.sergede1.webgameappbackend.model.PendingRequest;
import com.uhk.sergede1.webgameappbackend.model.User;
import com.uhk.sergede1.webgameappbackend.model.UserRequestType;
import com.uhk.sergede1.webgameappbackend.utils.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;


import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

@Service
public class DatabaseService {

    private final JdbcTemplate jdbcTemplate;
    private static final SecureRandom random = new SecureRandom();

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

    public void addFriendToUser(Long userId, Long friendId) throws DatabaseOperationException {
        String sql = "UPDATE FriendRelations SET friendUserIDs = ? WHERE UserID = ?";

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
            Serializer<Set<String>> serializer = new Serializer<>();
            Set<String> set = serializer.deserialize(serializedFriendUserIDs);

            set.add(friendId.toString());

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

    public void addPendingFriendInvitation(Long senderUserId, Long receiverUserId){

        UserRequestType friend_request_type = jdbcTemplate.queryForObject("SELECT * FROM Type WHERE abbreviation = 'friendrqst'",
                new BeanPropertyRowMapper<>(UserRequestType.class));
        Long friend_request_type_int = friend_request_type.getId();

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Pending WHERE senderUserID = ? AND receiverUserID = ? AND type_int = ?",
                Integer.class, senderUserId, receiverUserId, friend_request_type_int);

        if(count != null && count > 0){
            System.out.println("Friend invitation already present in Pending table");
        } else {
            jdbcTemplate.update("INSERT INTO Pending (senderUserID, receiverUserID, type_int) VALUES (?, ?, ?)",
                    senderUserId, receiverUserId, friend_request_type_int);
        }
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
}
