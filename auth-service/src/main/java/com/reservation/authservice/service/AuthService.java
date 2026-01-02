package com.reservation.authservice.service;


import com.reservation.authservice.dto.LoginRequest;
import com.reservation.authservice.dto.LoginResponse;
import com.reservation.authservice.entity.User;
import com.reservation.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole());

        return new LoginResponse(token, user.getUsername(), user.getRole());
    }

    public boolean validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            return userRepository.findByUsername(username).isPresent();
        } catch (Exception e) {
            return false;
        }
    }
}