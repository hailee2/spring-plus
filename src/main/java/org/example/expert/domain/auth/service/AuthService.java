package org.example.expert.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public String signup(SignupRequest request) {
        User newUser = new User(request.getEmail(), request.getPassword(), UserRole.of(request.getUserRole()), request.getNickname());
        User savedUser = userRepository.save(newUser);

        return jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(),savedUser.getUserRole(), savedUser.getNickname());
    }

    public String signin(SigninRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole(), user.getNickname());
    }
}
