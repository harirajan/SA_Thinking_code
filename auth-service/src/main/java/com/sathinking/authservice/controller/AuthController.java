package com.sathinking.authservice.controller;

import com.sathinking.authservice.dto.LoginRequest;
import com.sathinking.authservice.dto.SignupRequest;
import com.sathinking.authservice.entity.User;
import com.sathinking.authservice.repository.UserRepository;
import com.sathinking.authservice.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .build();

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User created: " + user.getUsername());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
                .map(user -> ResponseEntity.ok(jwtUtil.generateToken(user.getUsername())))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password"));
    }
}