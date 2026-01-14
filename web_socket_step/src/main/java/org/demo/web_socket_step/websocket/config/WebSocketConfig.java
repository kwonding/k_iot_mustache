package org.demo.web_socket_step.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration // 객체로만듬
@EnableWebSocketMessageBroker // STOMP 프로토콜 사용을 위한 어노테이션 - 필수
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /*
    * 1. 웹 소켓 연결을 위한 엔드포인트 설정(Handshake)
    * - 클라이언트가 서버와 최초 연결을 맺는 주소를 정의함(실시간 통신하기 위해)
    * */

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ws-stomp 엔드포인트로 결정
        // CORS: 모든 도메인에서 접속 가능하도록 허용(다른 출처에서 온 요청을 브라우저가 막을지 말지 설정하는 보안규칙)
        // SockJS 지원: 웹 소켓을 지원하지 않는 브라우저에서도 HTTP Polling 등으로 비슷하게 동작하도록 함 (라이브러리)
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*") // CORS
                .withSockJS();
    }

    /*
    * 2. 메세지 브로커 설정 (Publish / Subscribe)
    * - 추가적으로 보통 Prefix 설정을 미리 해둠
    * - /pub <-- 설정
    * - /sub <-- 설정
    * --> 구독시 ex) /sub/chat/room1
    * --> 메세지 보낼때 ex) /pub/chat/send
    * */

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. 클라이언트가 구독 할 수 있는 경로를 지정해 준다.
        // ex) /sub/chat/room1
        registry.enableSimpleBroker("/sub");

        // 2. 클라이언트가 서버 측으로 메세지를 보낼 수 있는 경로를 설정해 준다
        // ex) /pub/chat/message
        registry.setApplicationDestinationPrefixes("/pub");
    }

}
