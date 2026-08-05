package com.tenanthub.auth.service;

import com.tenanthub.auth.dto.AuthResponse;
import com.tenanthub.auth.dto.LoginRequest;
import com.tenanthub.auth.dto.RegisterRequest;
import com.tenanthub.auth.entity.Role;
import com.tenanthub.auth.entity.User;
import com.tenanthub.auth.exception.EmailAlreadyExistsException;
import com.tenanthub.auth.exception.InvalidCredentialsException;
import com.tenanthub.auth.repository.RoleRepository;
import com.tenanthub.auth.repository.UserRepository;
import com.tenanthub.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String DEFAULT_ROLE = "MEMBER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.email());
        }

        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseGet(() -> roleRepository.save(Role.builder().name(DEFAULT_ROLE).build()));

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .roles(Set.of(defaultRole))
                .build();
        userRepository.save(user);

        return new AuthResponse(jwtService.generateToken(user));
    }

    public AuthResponse login(LoginRequest request) {
        // Same exception/message for "no such user" and "wrong password" - don't let
        // a login attempt reveal whether an email is registered.
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return new AuthResponse(jwtService.generateToken(user));
    }
}
