package com.jys.smartbudget.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // 웹소켓 메시지 핸들링 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 웹소켓 연결을 시작할 경로 (예: ws://localhost:8080/ws-connect)
        registry.addEndpoint("/ws-connect")
                .setAllowedOriginPatterns("*") // 실무에서는 특정 도메인만 허용하도록 제한 필요
                .withSockJS(); // 웹소켓을 지원하지 않는 브라우저를 위한 폴백(Fallback) 처리
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /topic과 /queue 두 경로 모두 메시지 브로커가 처리할 수 있도록 허용
        config.enableSimpleBroker("/topic", "/queue");
    }
}