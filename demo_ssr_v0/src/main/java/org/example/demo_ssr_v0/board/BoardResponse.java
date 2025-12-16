package org.example.demo_ssr_v0.board;

import lombok.Data;
import org.example.demo_ssr_v0._core.utils.MyDateUtil;

/**
 * 응답 DTO
 */
public class BoardResponse {

    /**
     * 게시글 목록 응답 DTO
     */
    @Data
    public static class ListDTO {
        private Long id;
        private String title;
        private String username; // 작성자명 (평탄화 -> {{board.username}}) (평탄화 X -> {{board.user.username}})
        private String createdAt;

        public ListDTO(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            // 쿼리 --> JOIN FETCH로 가져오면 문제 없음
            if (board.getUser() != null) { // 방어적 코드
                this.username = board.getUser().getUsername();
            }
            // 날짜 포맷팅
            if (board.getCreatedAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(board.getCreatedAt());
            }
        }
    } // end of static inner class

    /**
     * 게시글 상세 응답 DTO
     */
    @Data
    public static class DetailDTO {
        private Long id;
        private String title;
        private String content;
        private Long userId;
        private String username;
        private String createdAt;

        public DetailDTO(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
            // JOIN FETCH 활용 (한번에 JOIN 에서 Repository 에서 가지고 올 예정)
            if (board.getUser() != null) {
                this.userId = board.getUser().getId();
                this.username = board.getUser().getUsername();
            }
            if (board.getCreatedAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(board.getCreatedAt());
            }
        }
    } // end of static inner class

    /**
     * 게시글 수정화면 응답 DTO
     */
    @Data
    public static class UpdateFormDTO {
        private Long id;
        private String title;
        private String content;

        public UpdateFormDTO(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
        }
    }

}
