package com.uhk.sergede1.webgameappbackend.database_service;


import com.uhk.sergede1.webgameappbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}