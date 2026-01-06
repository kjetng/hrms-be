package org.httt2.hrms.auth.service;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.auth.config.JwtService;
import org.httt2.hrms.auth.controller.dto.AuthResponse;
import org.httt2.hrms.auth.controller.dto.ChangePasswordRequest;
import org.httt2.hrms.auth.controller.dto.ChangePasswordResponse;
import org.httt2.hrms.auth.controller.dto.LoginRequest;
import org.httt2.hrms.auth.controller.dto.RegisterRequest;
import org.httt2.hrms.auth.controller.dto.UserInfoResponse;
import org.httt2.hrms.auth.entity.Role;
import org.httt2.hrms.auth.entity.User;
import org.httt2.hrms.auth.repository.UserRepository;
import org.httt2.hrms.exception.EmailAlreadyExistsException;
import org.httt2.hrms.exception.IncorrectPasswordException;
import org.httt2.hrms.exception.InvalidPasswordException;
import org.httt2.hrms.exception.PasswordMismatchException;
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

  /**
   * CHANGE PASSWORD:
   * 1. Get the current authenticated user.
   * 2. Validate that newPassword matches confirmPassword.
   * 3. Validate that newPassword is different from currentPassword.
   * 4. Validate password strength (at least 8 chars, uppercase, lowercase,
   * number).
   * 5. Verify currentPassword matches the stored password.
   * 6. Update the password in the database.
   */
  public ChangePasswordResponse changePassword(ChangePasswordRequest request) {
    // Get current authenticated user
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UsernameNotFoundException("No authenticated user found");
    }

    var email = authentication.getName();
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    // Validate that newPassword matches confirmPassword
    if (!request.newPassword().equals(request.confirmPassword())) {
      throw new PasswordMismatchException("Passwords do not match");
    }

    // Validate that newPassword is different from currentPassword
    if (request.newPassword().equals(request.currentPassword())) {
      throw new InvalidPasswordException("New password must be different from current password");
    }

    // Validate password strength: at least 6 characters, uppercase, lowercase, and
    // number
    String newPassword = request.newPassword();
    if (newPassword.length() < 6) {
      throw new InvalidPasswordException(
          "Password must be at least 6 characters and contain uppercase, lowercase, and a number");
    }

    boolean hasUpperCase = false;
    boolean hasLowerCase = false;
    boolean hasNumber = false;

    for (char c : newPassword.toCharArray()) {
      if (Character.isUpperCase(c)) {
        hasUpperCase = true;
      } else if (Character.isLowerCase(c)) {
        hasLowerCase = true;
      } else if (Character.isDigit(c)) {
        hasNumber = true;
      }
    }

    if (!hasUpperCase || !hasLowerCase || !hasNumber) {
      throw new InvalidPasswordException(
          "Password must be at least 6 characters and contain uppercase, lowercase, and a number");
    }

    // Verify current password
    if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
      throw new IncorrectPasswordException("Current password is incorrect");
    }

    // Update password
    try {
      user.setPassword(passwordEncoder.encode(request.newPassword()));
      userRepository.save(user);
      return new ChangePasswordResponse("Password has been successfully updated!");
    } catch (Exception e) {
      throw new RuntimeException("Failed to update password. Please try again.", e);
    }
  }
}