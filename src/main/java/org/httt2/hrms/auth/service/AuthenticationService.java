package org.httt2.hrms.auth.service;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.auth.config.JwtService;
import org.httt2.hrms.auth.controller.dto.AuthResponse;
import org.httt2.hrms.auth.controller.dto.LoginRequest;
import org.httt2.hrms.auth.controller.dto.RegisterRequest;
import org.httt2.hrms.auth.controller.dto.UserInfoResponse;
import org.httt2.hrms.auth.entity.Role;
import org.httt2.hrms.auth.entity.User;
import org.httt2.hrms.auth.repository.UserRepository;
import org.httt2.hrms.exception.EmailAlreadyExistsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  /**
   * REGISTER:
   * 1. Checks if a user already exists.
   * 2. Checks if there is an Employee with this email in the database.
   * 3. If Employee exists -> Links the new User to that Employee.
   * 4. If No Employee -> Creates a standalone User (like an Admin).
   */
  public AuthResponse register(RegisterRequest request) {
    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new EmailAlreadyExistsException(request.email());
    }

    var user = User.builder()
        .email(request.email())
        .password(passwordEncoder.encode(request.password()))
        .role(Optional.ofNullable(request.role()).orElse(Role.USER))
        .empId(request.empId())
        .build();

    userRepository.save(user);

    var jwtToken = jwtService.generateToken(user);
    return new AuthResponse(jwtToken);
  }

  /**
   * LOGIN:
   * Standard username/password check.
   */
  public AuthResponse authenticate(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.email(),
            request.password()));

    var user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new UsernameNotFoundException("User not found after authentication"));

    var jwtToken = jwtService.generateToken(user);
    return new AuthResponse(jwtToken);
  }

  public UserInfoResponse getCurrentUserInfo() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UsernameNotFoundException("No authenticated user found");
    }

    var email = authentication.getName();
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return new UserInfoResponse(
        user.getId(),
        user.getEmail(),
        List.of(user.getRole().name()),
        user.getEmpId());
  }
}