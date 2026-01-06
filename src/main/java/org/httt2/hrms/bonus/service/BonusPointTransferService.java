package org.httt2.hrms.bonus.service;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.auth.config.JwtService;
import org.httt2.hrms.auth.entity.Role;
import org.httt2.hrms.auth.entity.User;
import org.httt2.hrms.bonus.dto.TransferRequestDto;
import org.httt2.hrms.bonus.dto.TransferTransactionDto;
import org.httt2.hrms.bonus.entity.BonusPointAccount;
import org.httt2.hrms.bonus.entity.TransferType;
import org.httt2.hrms.bonus.entity.TransferTransaction;
import org.httt2.hrms.bonus.repository.BonusPointAccountRepository;
import org.httt2.hrms.bonus.repository.TransferTransactionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class BonusPointTransferService {

    private final BonusPointAccountRepository accountRepo;
    private final TransferTransactionRepository transferRepo;
    private final JwtService jwtService;

    @Transactional
    public TransferTransactionDto transfer(TransferRequestDto request) {
        Long senderId = extractEmpIdFromRequest();
        if (senderId == null) {
            throw new IllegalStateException("User not authenticated or empId not found in token");
        }

        User currentUser = extractUserFromRequest();
        TransferType transferType = request.getType() != null ? request.getType() : TransferType.TRANSFER;

        // ===== AUTHORIZATION CHECKS =====
        // Only MANAGER can create AWARD or DEDUCT transfers
        if ((transferType == TransferType.AWARD || transferType == TransferType.DEDUCT) &&
                (currentUser == null || currentUser.getRole() != Role.MANAGER)) {
            throw new SecurityException("Only MANAGER role can create AWARD or DEDUCT transfers");
        }

        // ===== DETERMINE SENDER AND RECEIVER BASED ON TYPE =====
        Long actualSenderId;
        Long actualReceiverId;

        if (transferType == TransferType.AWARD) {
            // AWARD: Manager awards points to employee (manager as sender, only employee balance changes)
            actualSenderId = senderId; // Manager's empId
            actualReceiverId = request.getReceiverId();
        } else if (transferType == TransferType.DEDUCT) {
            // DEDUCT: Manager deducts points from employee (manager as receiver, only employee balance changes)
            actualSenderId = request.getReceiverId();
            actualReceiverId = senderId; // Manager's empId
        } else {
            // TRANSFER: Normal transfer between employees
            actualSenderId = senderId;
            actualReceiverId = request.getReceiverId();
        }

        BonusPointAccount sender = null;
        BonusPointAccount receiver = null;

        // For AWARD/DEDUCT, load manager account for record but don't modify their balance
        if (transferType == TransferType.AWARD) {
            sender = accountRepo.findById(actualSenderId)
                    .orElseThrow(() -> new IllegalStateException("Manager account not found"));
            receiver = accountRepo.findById(actualReceiverId)
                    .orElseThrow(() -> new IllegalStateException("Receiver account not found"));

            if (request.getPoints() < 0) {
                throw new IllegalStateException("Points must be positive for AWARD");
            }

            // Only modify employee's balance
            receiver.setBonusPoint(receiver.getBonusPoint() + request.getPoints());
        } else if (transferType == TransferType.DEDUCT) {
            sender = accountRepo.findById(actualSenderId)
                    .orElseThrow(() -> new IllegalStateException("Sender account not found"));
            receiver = accountRepo.findById(actualReceiverId)
                    .orElseThrow(() -> new IllegalStateException("Manager account not found"));

            int available = sender.getBonusPoint();
            int requested = request.getPoints();
            if (available < requested) {
                throw new IllegalStateException(
                        "Insufficient points to deduct: requested " + requested + ", available " + available);
            }

            // Only modify employee's balance
            sender.setBonusPoint(available - requested);
        } else {
            // TRANSFER: Normal flow with both accounts
            sender = accountRepo.findById(actualSenderId)
                    .orElseThrow(() -> new IllegalStateException("Sender account not found"));
            receiver = accountRepo.findById(actualReceiverId)
                    .orElseThrow(() -> new IllegalStateException("Receiver account not found"));

            int available = sender.getBonusPoint();
            int requested = request.getPoints();
            if (available < requested) {
                throw new IllegalStateException(
                        "Insufficient points to transfer: requested " + requested + ", available " + available);
            }

            sender.setBonusPoint(available - requested);
            receiver.setBonusPoint(receiver.getBonusPoint() + requested);
        }

        // Save only the accounts that were actually modified
        if (transferType == TransferType.AWARD) {
            // Only save employee receiver, not manager sender
            accountRepo.save(receiver);
        } else if (transferType == TransferType.DEDUCT) {
            // Only save employee sender, not manager receiver
            accountRepo.save(sender);
        } else {
            // TRANSFER: Save both
            accountRepo.save(sender);
            accountRepo.save(receiver);
        }

        // Create transaction record with manager info for AWARD/DEDUCT
        TransferTransaction savedTransaction = transferRepo.save(
                TransferTransaction.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .numberPoint(request.getPoints())
                        .note(request.getNote())
                        .type(transferType)
                        .build());

        TransferTransactionDto dto = TransferTransactionDto.from(savedTransaction);
        dto.setRole(extractRoleFromRequest());
        return dto;
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

    private User extractUserFromRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return user;
            }
        }
        return null;
    }

    private String extractRoleFromRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails && userDetails instanceof User user) {
                return user.getRole() != null ? user.getRole().name() : null;
            }
        }
        return null;
    }
}
