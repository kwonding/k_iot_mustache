package org.example.demo_ssr_v0.reply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    // 댓글 목록 - 시간을 오름차순
    /**
     * SELECT r.*, b.*, u.*,
     * FROM reply_tb r
     * INNER JOIN board_tb b ON r.board_id = b.id
     * INNER JOIN user_tb u ON r.user_id = u.id
     * WHERE r.board_id = ?
     * ORDER BY r.created_at ASC
     */
    @Query("""
        SELECT r
        FROM Reply r
        JOIN FETCH r.board
        JOIN FETCH r.user
        WHERE r.board.id = :boardId
        ORDER BY r.createdAt ASC
    """)
    List<Reply> findByBoardIdWithUser(@Param("boardId") Long boardId);

    // 댓글 ID로 조회 (작성자 정보도 포함, JOIN FETCH 사용)
    @Query("""
        SELECT r
        FROM Reply r
        JOIN FETCH r.user
        JOIN FETCH r.board
        WHERE r.id = :id
    """)
    Optional<Reply> findByIdWithUser(@Param("id") Long userId);

    // 게시글 ID로 댓글 삭제
    void deleteByBoardId(Long boardId);
}
