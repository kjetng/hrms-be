package org.httt2.hrms.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.httt2.hrms.auth.controller.dto.AuthResponse;
import org.httt2.hrms.auth.controller.dto.LoginRequest;
import org.httt2.hrms.auth.controller.dto.RegisterRequest;
import org.httt2.hrms.auth.controller.dto.UserInfoResponse;
import org.httt2.hrms.auth.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthenticationController {

  private final AuthenticationService service;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(
      @RequestBody @Valid RegisterRequest request
  ) {
    return ResponseEntity.ok(service.register(request));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> authenticate(
      @RequestBody @Valid LoginRequest request
  ) {
    return ResponseEntity.ok(service.authenticate(request));
  }

  @GetMapping("/me")
  public ResponseEntity<UserInfoResponse> currentUser() {
    return ResponseEntity.ok(service.getCurrentUserInfo());
  }
}