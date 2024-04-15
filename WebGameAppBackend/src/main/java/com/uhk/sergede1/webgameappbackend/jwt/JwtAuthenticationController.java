package com.uhk.sergede1.webgameappbackend.jwt;

import com.uhk.sergede1.webgameappbackend.model.User;
import com.uhk.sergede1.webgameappbackend.database_service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import java.util.Optional;

@RestController
public class JwtAuthenticationController {

    private final JwtTokenService tokenService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    private DatabaseService databaseService;

    public JwtAuthenticationController(JwtTokenService tokenService,
                                       AuthenticationManager authenticationManager,
                                       DatabaseService databaseService) {
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.databaseService = databaseService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> generateToken(@RequestBody JwtTokenRequest jwtTokenRequest) {

        Optional<User> userOptional = databaseService.findUserByUsername(jwtTokenRequest.username());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println("Found user: " + user);

            // Verify password
//            if (passwordEncoder.matches(jwtTokenRequest.password(), user.getPassword())) {
            System.out.println("Backend: " + user.getPassword() + " Frontend password:  " + jwtTokenRequest.password());
            if (jwtTokenRequest.password().equals(user.getPassword())) {
                var authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                jwtTokenRequest.username(),
                                jwtTokenRequest.password());
                String token = tokenService.generateToken(authenticationToken);
                System.out.println("Generated token: " + token);
                return ResponseEntity.ok(new JwtTokenResponse(token, user.getId()));
            } else {
                System.out.println("Invalid password for user: " + user.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
            }
        } else {
            System.out.println("User not found: " + jwtTokenRequest.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
    }
}
