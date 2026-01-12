package org.demo.web_socket_step.polling.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service("PollingChatService") // 빈 이름 명시
public class ChatService {

    private final PollingChatRepository pollingChatRepository;

    @Transactional
    public void save(String message, String sender) {
        Chat chat = Chat.builder()
                .message(message)
                .sender(sender)
                .build();

        pollingChatRepository.save(chat);
    }

    @Transactional(readOnly = true)
    public List<Chat> findAll() {
        Sort  sort = Sort.by(Sort.Direction.ASC, "id"); // 정렬
        return pollingChatRepository.findAll();
    }

}
