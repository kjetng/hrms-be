package org.httt2.hrms.auth.controller.dto;

import org.httt2.hrms.auth.entity.Role;

public record RegisterRequest(
    String email,
    String password,
    Role role
) {}
