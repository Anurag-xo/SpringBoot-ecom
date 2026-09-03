package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.dto.AuthResponse;
import in.anurag.CreatorStore.dto.LoginRequest;
import in.anurag.CreatorStore.dto.RegisterRequest;
import in.anurag.CreatorStore.entities.User;
import in.anurag.CreatorStore.exceptions.ResourceNotFoundException;
import in.anurag.CreatorStore.repositories.UserRepository;
import in.anurag.CreatorStore.security.JwtUtil;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final AuthenticationManager authenticationManager;

  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new RuntimeException("Username already exists");
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("Email already exists");
    }

    Set<String> roles = new HashSet<>();
    roles.add("ROLE_USER");

    User user =
        User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .roles(roles)
            .enabled(true)
            .build();

    userRepository.save(user);

    String token = jwtUtil.generateToken(user.getUsername());

    return new AuthResponse(token, user.getUsername(), "User registered successfully");
  }

  public AuthResponse login(LoginRequest request) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    User user =
        userRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    String token = jwtUtil.generateToken(user.getUsername());

    return new AuthResponse(token, user.getUsername(), "Login successful");
  }
}
