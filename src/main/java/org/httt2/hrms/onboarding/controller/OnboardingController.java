package org.httt2.hrms.onboarding.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.onboarding.controller.dto.TokenValidationResponse;
import org.httt2.hrms.onboarding.service.OnboardingTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for onboarding-related operations.
 */
@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
@Slf4j
public class OnboardingController {

    private final OnboardingTokenService onboardingTokenService;

    /**
     * Validates an onboarding token.
     *
     * @param token the token to validate
     * @return token validation response with employee info if valid
     */
    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestParam String token) {
        log.info("Validating onboarding token");

        return onboardingTokenService.validateToken(token)
                .map(onboardingToken -> ResponseEntity.ok(
                        TokenValidationResponse.builder()
                                .valid(true)
                                .employeeId(onboardingToken.getEmployeeId())
                                .email(onboardingToken.getEmail())
                                .message("Token is valid")
                                .build()
                ))
                .orElseGet(() -> ResponseEntity.ok(
                        TokenValidationResponse.builder()
                                .valid(false)
                                .message("Token is invalid or expired")
                                .build()
                ));
    }

    /**
     * Marks an onboarding token as used after successful form submission.
     *
     * @param token the token to mark as used
     * @return success response
     */
    @PostMapping("/complete")
    public ResponseEntity<TokenValidationResponse> completeOnboarding(@RequestParam String token) {
        log.info("Completing onboarding for token");

        return onboardingTokenService.validateToken(token)
                .map(onboardingToken -> {
                    onboardingTokenService.markTokenAsUsed(token);
                    return ResponseEntity.ok(
                            TokenValidationResponse.builder()
                                    .valid(true)
                                    .employeeId(onboardingToken.getEmployeeId())
                                    .message("Onboarding completed successfully")
                                    .build()
                    );
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(
                        TokenValidationResponse.builder()
                                .valid(false)
                                .message("Token is invalid or expired")
                                .build()
                ));
    }
}

