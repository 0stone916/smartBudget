package com.jys.smartbudget.consumer;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jys.smartbudget.dto.NotiRequestDto;
import com.jys.smartbudget.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService; // 기존 서비스 주입
    private final SimpMessagingTemplate messagingTemplate; // 웹소켓 알림용

    @KafkaListener(topics = "payment-notif", groupId = "smartbudget-group")
    public void consume(String message) throws JsonProcessingException {
        
        log.info(">>>> [Kafka] 결제 알림 수신: {}", message);

        // 1. JSON 메시지를 기존에 쓰던 DTO(NotiRequestDto)로 변환
        NotiRequestDto notiRequestDto = objectMapper.readValue(message, NotiRequestDto.class);

        // 2. 기존 PaymentService의 비즈니스 로직 호출 (Redis 락 + DB 저장 포함)
        paymentService.processPaymentNotification(notiRequestDto);

        // 3. 웹소켓 실시간 알림 전송 (기존 컨트롤러 로직)
        String personalDestination = "/topic/payment/" + notiRequestDto.getUserId();
        messagingTemplate.convertAndSend(personalDestination, notiRequestDto);

        log.info(">>>> [Kafka] 처리 완료 및 웹소켓 전송 완료: {}", notiRequestDto.getApprovalNo());
    }
}