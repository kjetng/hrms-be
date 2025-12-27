package org.httt2.hrms.bonus.controller;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.bonus.dto.BonusPointViewDto;
import org.httt2.hrms.bonus.dto.RedeemRequestDto;
import org.httt2.hrms.bonus.dto.RedemptionResultDto;
import org.httt2.hrms.bonus.dto.TransferRequestDto;
import org.httt2.hrms.bonus.service.BonusPointRedemptionService;
import org.httt2.hrms.bonus.service.BonusPointTransferService;
import org.httt2.hrms.bonus.service.BonusPointViewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BonusPointController {

    private final BonusPointViewService viewService;
    private final BonusPointTransferService transferService;
    private final BonusPointRedemptionService redemptionService;

    @GetMapping("/view")
    public ResponseEntity<BonusPointViewDto> viewMyCredits() {
        return ResponseEntity.ok(viewService.getMyBonusPointView());
    }
//
//    @PostMapping("/transfer")
//    public ResponseEntity<Void> transferPoints(
//            @RequestBody TransferRequestDto request) {
//        transferService.transfer(request);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/redeem")
//    public ResponseEntity<RedemptionResultDto> redeemPoints(
//            @RequestBody RedeemRequestDto request) {
//        return ResponseEntity.ok(redemptionService.redeem(request));
//    }
}
