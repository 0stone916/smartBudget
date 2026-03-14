package com.jys.smartbudget.consumer;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.dto.NotiRequestDto;
import com.jys.smartbudget.mapper.ExpenseMapper;
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
    private final ExpenseMapper expenseMapper;
    private final PaymentService paymentService; 
    private final SimpMessagingTemplate messagingTemplate; // 웹소켓 알림용

    @KafkaListener(topics = "payment-notif", groupId = "Financial CMS")
    public void consume(String message) throws JsonProcessingException {
        
        log.info(">>>> [Kafka] 결제 정보 수신: {}", message);

        // 1. JSON 메시지를 NotiRequestDto로 변환
        NotiRequestDto notiRequestDto = objectMapper.readValue(message, NotiRequestDto.class);

        ExpenseDTO existingExpense = expenseMapper.findByApprovalNo(notiRequestDto.getApprovalNo());

        // 2. 데이터가 없을 때만 Redis 락 획득 및 DB 저장 로직 수행
        if (existingExpense == null) {
            paymentService.processPaymentNotification(notiRequestDto);
        }

        // 3. 웹소켓 실시간 알림 전송
        String personalDestination = "/topic/payment/" + notiRequestDto.getUserId();
        messagingTemplate.convertAndSend(personalDestination, notiRequestDto);

        log.info(">>>> [Kafka] 처리 완료 및 웹소켓 전송 완료: {}", notiRequestDto.getApprovalNo());
    }
}