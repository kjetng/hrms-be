package org.httt2.hrms.bonus.service;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.auth.config.JwtService;
import org.httt2.hrms.auth.entity.User;
import org.httt2.hrms.bonus.dto.RedeemRequestDto;
import org.httt2.hrms.bonus.dto.RedemptionTransactionDto;
import org.httt2.hrms.bonus.entity.BonusPointAccount;
import org.httt2.hrms.bonus.entity.BonusCreditSetting;
import org.httt2.hrms.bonus.entity.RedemptionTransaction;
import org.httt2.hrms.bonus.repository.BonusPointAccountRepository;
import org.httt2.hrms.bonus.repository.RedemptionTransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BonusPointRedemptionService {

    private final BonusPointAccountRepository accountRepo;
    private final RedemptionTransactionRepository redemptionRepo;
    private final BonusCreditSettingService creditSettingService;
    private final JwtService jwtService;

    @Transactional
    public RedemptionTransactionDto redeem(RedeemRequestDto request) {
        // Get the authenticated user's employee ID
        Long empId = extractEmpIdFromRequest();
        if (empId == null) {
            throw new IllegalStateException("User not authenticated or empId not found in token");
        }

        // Validate request
        if (request.getPoints() == null || request.getPoints() <= 0) {
            throw new IllegalArgumentException("Points must be positive");
        }

        // Get the user's account
        BonusPointAccount account = accountRepo.findById(empId)
                .orElseThrow(() -> new IllegalStateException("Bonus point account not found"));

        // Check if user has enough points
        if (account.getBonusPoint() < request.getPoints()) {
            throw new IllegalStateException(
                    "Insufficient points: requested " + request.getPoints() +
                            ", available " + account.getBonusPoint());
        }

        // Get conversion rate from settings
        BonusCreditSetting setting = creditSettingService.getCurrentSetting();
        if (setting == null || setting.getConversionRate() == null || setting.getConversionRate() <= 0) {
            throw new IllegalStateException("Bonus credit conversion rate is not configured");
        }

        // Calculate the amount to receive (points * conversion rate)
        BigDecimal amountReceived = BigDecimal.valueOf(request.getPoints())
                .multiply(BigDecimal.valueOf(setting.getConversionRate()));

        // Deduct points from account
        account.setBonusPoint(account.getBonusPoint() - request.getPoints());
        accountRepo.save(account);

        // Generate formatted note
        String note = buildRedemptionNote(request.getNote(), request.getPoints(), amountReceived);

        // Create redemption transaction
        RedemptionTransaction transaction = RedemptionTransaction.builder()
                .convertedPoint(request.getPoints())
                .amountReceived(amountReceived)
                .note(note)
                .account(account)
                .build();

        RedemptionTransaction savedTransaction = redemptionRepo.save(transaction);

        return RedemptionTransactionDto.from(savedTransaction);
    }

    private String buildRedemptionNote(String note, Integer points, BigDecimal amount) {
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        currencyFormat.setGroupingUsed(true);
        String formattedAmount = currencyFormat.format(amount);

        String systemNote = String.format("Withdrawal on %s | %d points → %s VND",
                LocalDate.now(),
                points,
                formattedAmount);

        if (note != null) {
            String trimmed = note.trim();
            if (!trimmed.isEmpty()) {
                // Only user note; enforce max 500 chars
                return trimmed.length() <= 500 ? trimmed : trimmed.substring(0, 500);
            }
        }

        // No user note provided; use system-generated note
        return systemNote;
    }

    private Long extractEmpIdFromRequest() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            Long empId = jwtService.extractEmpIdFromRequest(request);
            if (empId != null) {
                return empId;
            }
        }

        // Fallback: try to get from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails userDetails) {
            if (userDetails instanceof User user) {
                return user.getEmpId();
            }
        }

        return null;
    }
}
