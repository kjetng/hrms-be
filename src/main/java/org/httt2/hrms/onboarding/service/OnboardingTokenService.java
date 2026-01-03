package org.httt2.hrms.onboarding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.onboarding.entity.OnboardingToken;
import org.httt2.hrms.onboarding.repository.OnboardingTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * Service for generating and validating secure onboarding tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingTokenService {

    private final OnboardingTokenRepository tokenRepository;

    @Value("${application.onboarding.base-url}")
    private String baseUrl;

    @Value("${application.onboarding.token-expiration}")
    private long tokenExpiration;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    /**
     * Generates a secure token for employee onboarding.
     *
     * @param employeeId    the employee ID
     * @param personalEmail the personal email of the employee
     * @return the generated secure form URL
     */
    @Transactional
    public String generateSecureFormLink(Long employeeId, String personalEmail) {
        // Invalidate any existing unused tokens for this employee
        tokenRepository.findByEmployeeIdAndUsedFalse(employeeId)
                .ifPresent(existingToken -> {
                    existingToken.setUsed(true);
                    tokenRepository.save(existingToken);
                    log.info("Invalidated existing token for employee: {}", employeeId);
                });

        // Generate a new secure token
        String token = generateSecureToken();

        OnboardingToken onboardingToken = OnboardingToken.builder()
                .token(token)
                .employeeId(employeeId)
                .email(personalEmail)
                .expiresAt(Instant.now().plusMillis(tokenExpiration))
                .build();

        tokenRepository.save(onboardingToken);
        log.info("Generated new onboarding token for employee: {}", employeeId);

        return buildSecureFormUrl(token);
    }

    /**
     * Validates an onboarding token and returns the associated employee ID if valid.
     *
     * @param token the token to validate
     * @return Optional containing employee ID if token is valid, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<OnboardingToken> validateToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(OnboardingToken::isValid);
    }

    /**
     * Marks a token as used after successful onboarding completion.
     *
     * @param token the token to mark as used
     */
    @Transactional
    public void markTokenAsUsed(String token) {
        tokenRepository.findByToken(token)
                .ifPresent(onboardingToken -> {
                    onboardingToken.setUsed(true);
                    tokenRepository.save(onboardingToken);
                    log.info("Marked token as used for employee: {}", onboardingToken.getEmployeeId());
                });
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    private String buildSecureFormUrl(String token) {
        return String.format("%s/onboarding?token=%s", baseUrl, token);
    }
}

