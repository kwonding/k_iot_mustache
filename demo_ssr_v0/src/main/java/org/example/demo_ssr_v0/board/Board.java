package org.example.demo_ssr_v0.board;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo_ssr_v0.user.User;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Table(name = "board_tb")
@Entity
public class Board {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    @ColumnDefault("false")
    private Boolean premium = false;

    // N : 1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // pc --> db
    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public Board(String title, String content, User user, Boolean premium) {
        this.title = title;
        this.content = content;
        this.user = user;
        // 체크박스에 체크를 안하면 null로 들어오는 것을 방지, 체크하면 true
        this.premium = (premium != null) ? premium : false;
    }

    // Board 상태값 수정하는 로직
    public void update(BoardRequest.UpdateDto updateDto) {
        // updateDto 유효성 검사 처리
        updateDto.validate();

        this.title = updateDto.getTitle();
        this.content = updateDto.getContent();
        // 게시글 수정은 작성자를 변경할 수 없다. - this.user = updateDto.getUsername();
        // 체크 박스 null 방지
        this.premium = (updateDto.getPremium() != null) ? updateDto.getPremium() : false;
    }

    // 게시글 소유자 확인 로직
    public boolean isOwner(Long userId) {
        return this.user.getId().equals(userId);
    }

    // 개별 필드 수정 - title
    public void updateTitle(String newTitle) {
        // 방어적 코드
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 비워질 수 없습니다.");
        }
        this.title = newTitle;
    }

    // 개별 필드 수정 - content
    public void updateContent(String newContent) {
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 비워질 수 없습니다.");
        }
        this.content = newContent;
    }

}
