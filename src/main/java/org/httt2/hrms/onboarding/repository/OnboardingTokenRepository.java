package org.httt2.hrms.onboarding.repository;

import org.httt2.hrms.onboarding.entity.OnboardingToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OnboardingTokenRepository extends JpaRepository<OnboardingToken, Long> {

    Optional<OnboardingToken> findByToken(String token);

    Optional<OnboardingToken> findByEmployeeIdAndUsedFalse(Long employeeId);
}

