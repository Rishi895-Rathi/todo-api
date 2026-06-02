package com.rishi.taskmanager.auth;

import com.rishi.taskmanager.DTO.AuthResponse;
import com.rishi.taskmanager.DTO.LoginRequest;
import com.rishi.taskmanager.DTO.RegisterRequest;
import com.rishi.taskmanager.model.User;
import com.rishi.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${admin.registration.secret}")
    private String adminSecret;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    // ── REGISTER ──────────────────────────────────────────────
    public AuthResponse register(RegisterRequest request) {

        // 1. Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already registered"
            );
        }

        // 2. Validate password strength
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password must be at least 8 characters"
            );
        }

        // 3. Determine role — ADMIN only if correct secret provided
        User.Role role = User.Role.USER;
        if (request.getRole() == User.Role.ADMIN) {
            if (request.getAdminSecret() == null ||
                    !request.getAdminSecret().equals(adminSecret)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Invalid admin secret"
                );
            }
            role = User.Role.ADMIN;
        }

        // 4. Build and save user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Could not create user. Try again."
            );
        }

        // 5. Generate token using user ID
        String token = jwtUtil.generateToken(
                String.valueOf(user.getId()),
                user.getRole().name()
        );

        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    // ── LOGIN ─────────────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {

        // 1. Authenticate credentials
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid email or password"
            );
        }

        // 2. Load user from DB
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"
                ));

        // 3. Generate token
        String token = jwtUtil.generateToken(
                String.valueOf(user.getId()),
                user.getRole().name()
        );

        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }
}