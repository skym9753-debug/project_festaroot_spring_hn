package com.study.app.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // 웹소켓 서버를 활성화하고 STOMP 메시지 브로커를 사용하도록 설정
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 프론트엔드에서 웹소켓에 최초로 연결(Handshake)할 때 사용할 엔드포인트 주소입니다.
        // 앞뒤 슬래시(/) 처리에 주의하세요! 주소는 "/ws-stomp" 입니다.
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*"); // CORS 허용 (테스트 및 실제 통신용)
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. 메시지를 구독(Subscribe)하는 클라이언트들에게 메시지를 전달할 브로커의 접두사(Prefix) 설정
        // 클라이언트는 "/sub/chat/room/{roomId}" 주소를 구독하여 실시간 메시지를 받게 됩니다.
        registry.enableSimpleBroker("/sub");

        // 2. 클라이언트가 메시지를 발행(Publish)할 때 보낼 목적지 접두사 설정
        // 클라이언트가 "/pub/chat/message"로 메시지를 전송하면 백엔드의 @MessageMapping으로 라우팅됩니다.
        registry.setApplicationDestinationPrefixes("/pub");
    }
}
