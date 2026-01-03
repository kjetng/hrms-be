package org.httt2.hrms.auth.controller.dto;

import java.util.List;

public record UserInfoResponse(
    Long id,
    String email,
    List<String> roles,
    Long employeeIds
) {}
