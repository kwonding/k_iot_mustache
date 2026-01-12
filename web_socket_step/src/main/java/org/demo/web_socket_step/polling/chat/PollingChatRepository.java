package org.demo.web_socket_step.polling.chat;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PollingChatRepository extends JpaRepository<Chat, Long> {

}
