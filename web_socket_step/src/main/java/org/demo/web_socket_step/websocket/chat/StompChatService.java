package org.demo.web_socket_step.websocket.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class StompChatService {
    private final StompChatRepository stompChatRepository; // DI

    // 1.스프링에서 제공하는 메세지 전용 도구
    // - 특정 경로(/sub/...)를 구독하고 있는 클라이언트에게 메세지를 푸시(방송)할 수 있음
    private final SimpMessagingTemplate simpMessagingTemplate;

    // 채팅 비즈니스 로직 --> 저장하고 뿌림
    public void saveAndBroadcast(String message, String sender) {
        Chat chat = Chat.builder()
                .message(message)
                .sender(sender)
                .build(); // chat 이라는 객체 생성해서(메모리단에 올림)

        stompChatRepository.save(chat); // DB 저장

        // json 아니고 --> 우리만의 형식으로 보냄 --> sender:내용
        String formattedMessage = sender + ":" + message;
        simpMessagingTemplate.convertAndSend("/sub/chat/room1", formattedMessage);
    }

    // 전체 조회
    public List<Chat> findAll() {
        return stompChatRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }
}
