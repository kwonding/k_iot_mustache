package org.demo.web_socket_step.sse.chat;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SseChatRepository extends JpaRepository<Chat, Long> {
}
