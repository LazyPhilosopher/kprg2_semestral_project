package com.uhk.sergede1.webgameappbackend.service;

import com.uhk.sergede1.webgameappbackend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DatabaseService {

    private final JdbcTemplate jdbcTemplate;
    private static final SecureRandom random = new SecureRandom();

    @Autowired
    public DatabaseService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
            String sql = "INSERT INTO USERS (username, password, role) VALUES (?, ?, 'User')";
            try {
                jdbcTemplate.update(sql, user.getUsername(), user.getPassword());
            } catch (DataAccessException e) {
                throw new DatabaseOperationException("Error inserting user into database", e);
            }
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
}
