package com.jys.smartbudget.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import com.jys.smartbudget.dto.NotiRequestDto;
import com.jys.smartbudget.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/noti/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentNotificationController {

    private final PaymentService paymentService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ResponseEntity<String> receiveNotification(HttpServletRequest req, @RequestBody NotiRequestDto notiRequestDto) {
        log.info("은행 결제 알림 수신: {}", notiRequestDto.getUserId());

        try {
            // 실시간 장부 기록 및 예산 반영
            paymentService.processPaymentNotification(notiRequestDto);

            String personalDestination = "/topic/payment/" + notiRequestDto.getUserId();
            
            log.info("알림 전송 경로: {}", personalDestination);
            
            messagingTemplate.convertAndSend(personalDestination, notiRequestDto);

            return ResponseEntity.ok("SUCCESS");
            
        } catch (DuplicateKeyException e) {
            // 동일한 승인번호가 이미 저장된 경우 (중복 알림 방지)
            log.warn("이미 처리된 결제 건입니다: {}", notiRequestDto.getApprovalNo());
            return ResponseEntity.ok("ALREADY_PROCESSED");
            
        } catch (Exception e) {
            log.error("장부 기록 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("FAIL");
        }
    }
}