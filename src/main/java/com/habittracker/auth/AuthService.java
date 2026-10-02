package com.habittracker.auth;

import com.habittracker.auth.dto.AuthResponse;
import com.habittracker.auth.dto.LoginRequest;
import com.habittracker.auth.dto.SignupRequest;
import com.habittracker.user.User;
import com.habittracker.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException(email);
        }
        User user = new User(email, passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(user.getEmail()), user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return new AuthResponse(jwtService.generateToken(user.getEmail()), user.getEmail());
    }
}
