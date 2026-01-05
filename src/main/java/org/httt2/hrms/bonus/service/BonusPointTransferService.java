package org.httt2.hrms.bonus.service;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.auth.config.JwtService;
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

        BonusPointAccount sender = accountRepo.findById(senderId)
                .orElseThrow(() -> new IllegalStateException("Sender account not found"));

        BonusPointAccount receiver = accountRepo.findById(request.getReceiverId())
                .orElseThrow(() -> new IllegalStateException("Receiver account not found"));

        if (sender.getBonusPoint() < request.getPoints()) {
            throw new IllegalStateException("Insufficient points");
        }

        sender.setBonusPoint(sender.getBonusPoint() - request.getPoints());
        receiver.setBonusPoint(receiver.getBonusPoint() + request.getPoints());

        TransferTransaction savedTransaction = transferRepo.save(
                TransferTransaction.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .numberPoint(request.getPoints())
                        .note(request.getNote())
                        .type(request.getType() != null ? request.getType() : TransferType.TRANSFER)
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
