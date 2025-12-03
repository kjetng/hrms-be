package org.httt2.hrms.auth.controller.dto;

public record LoginRequest(
    String email,
    String password
) {}
