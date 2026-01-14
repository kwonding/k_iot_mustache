package org.demo.web_socket_step.websocket.chat;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Table(name = "chat_stomp_tb")
@Entity(name = "StompChat")
public class Chat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    private String sender;

    @Builder
    public Chat(Long id, String message, String sender) {
        this.id = id;
        this.message = message;
        this.sender = sender;
    }
}
