package org.demo.web_socket_step.websocket.chat;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StompChatRepository extends JpaRepository<Chat, Long> {
}
