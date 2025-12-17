package org.example.demo_ssr_v0.reply;

import lombok.Data;
import org.example.demo_ssr_v0._core.errors.exception.Exception400;
import org.example.demo_ssr_v0.board.Board;
import org.example.demo_ssr_v0.user.User;

public class ReplyRequest {

    // 댓글 작성
    @Data
    public static class SaveDTO{
        private Long boardId;
        private String comment;

        // 유효성 검사 (형식검사)
        public void validate() {
            if (comment == null || comment.trim().isEmpty()) {
                throw new Exception400("댓글 내용을 입력해주세요");
            }
            if (comment.length() > 500) {
                throw new Exception400("댓글은 500자 이하만 작성 가능합니다.");
            }
            if (boardId == null) {
                throw new Exception400("게시글 아이디가 필요합니다.");
            }
        }

        // 서비스 단에서 DTO를 엔티티로 변환하는 편의 메서드 제공
        public Reply toEntity(Board board, User user) {
            return Reply.builder()
                    .comment(this.comment)
                    .board(board)
                    .user(user)
                    .build();
        }
    }

    // 댓글 수정
    @Data
    public static class UpdateDTO {
        private String comment;

        public void validate() {
            if (comment == null || comment.trim().isEmpty()) {
                throw new Exception400("댓글 내용을 입력해주세요");
            }
            if (comment.length() > 500) {
                throw new Exception400("댓글은 500자 이하만 작성 가능합니다.");
            }
        }
    }
}
