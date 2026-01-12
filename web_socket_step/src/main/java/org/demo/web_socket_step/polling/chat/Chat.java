package org.demo.web_socket_step.polling.chat;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Table(name = "chat_tb")
@Entity(name = "PollingChat")
public class Chat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;

    @Column(nullable = false)
    private String message;

    @Builder
    public Chat(Long id, String sender, String message) {
        this.id = id;
        this.sender = sender;
        this.message = message;
    }
}
