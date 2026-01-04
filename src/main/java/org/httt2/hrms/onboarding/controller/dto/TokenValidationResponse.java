package org.httt2.hrms.onboarding.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for token validation requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {

    /**
     * Whether the token is valid
     */
    private boolean valid;

    /**
     * The employee ID associated with the token (if valid)
     */
    private Long employeeId;

    /**
     * The email associated with the token (if valid)
     */
    private String email;

    /**
     * A message describing the validation result
     */
    private String message;
}

