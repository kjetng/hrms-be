package org.httt2.hrms.bonus.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.httt2.hrms.bonus.dto.BonusPointBalanceDto;
import org.httt2.hrms.bonus.dto.BonusPointViewDto;
import org.httt2.hrms.bonus.dto.TransferRequestDto;
import org.httt2.hrms.bonus.dto.TransferTransactionDto;
import org.httt2.hrms.bonus.dto.TeamMembersResponseDto;
import org.httt2.hrms.bonus.service.BonusPointTransferService;
import org.httt2.hrms.bonus.dto.BonusPointViewQueryDto;
import org.httt2.hrms.bonus.service.BonusPointViewService;
import org.httt2.hrms.bonus.service.TeamMemberViewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BonusPointController {

    private final BonusPointViewService viewService;
    private final TeamMemberViewService teamMemberViewService;
    private final BonusPointTransferService transferService;

    @GetMapping("/balance")
    public ResponseEntity<BonusPointBalanceDto> getBalance() {
        return ResponseEntity.ok(viewService.getMyBalance());
    }

    @PostMapping("/view")
    public BonusPointViewDto getBonusPointView(
            @RequestBody BonusPointViewQueryDto query) {
        return viewService.getMyBonusPointView(query);
    }

    @GetMapping("/team")
    public ResponseEntity<TeamMembersResponseDto> getTeamMembers(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(teamMemberViewService.getTeamMembers(page, size, search));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferTransactionDto> transferPoints(
            @Valid @RequestBody TransferRequestDto request) {
        return ResponseEntity.ok(transferService.transfer(request));
    }
}
