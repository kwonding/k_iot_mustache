package org.example.demo_ssr_v0.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // 자동 제공 메서드(별도 구현 없이 사용 가능)
    // - save(T entity) : (Insert 또는 Update)
    // - findById(ID id) : ID로 엔티티 조회 (Optional<T> 반환)
    // - findAll()
    // - deleteById(ID id) : ID로 엔티티 삭제
    // - count() : 전체 개수 조회
    // - existsById(ID id) : ID 존재 여부 확인

    // 전체 조회
    // SELECT * FROM board_tb ORDER BY created_at DESC
    // LAZY 로딩이라서 한번에 username을 가져와야 함
    // JOIN FETCH

    // 게시글 전체 조회 (작성자 정보 포함, JOIN FETCH 사용)
//    @Query("SELECT b FROM Board b JOIN FETCH b.user ORDER BY b.createdAt DESC")
//    List<Board> findAllByWithUserOrderByCreatedAtDesc();

    // 게시글 전체 조회 (페이징 처리) - 검색어 없을때 사용
    // - 인수값은 우리가 생성한 Pageable 객체를 넣어 주면 됨
    /**
     *
     * @param pageable 페이징 정보있음 (페이지 번호, 크기, 정렬)
     * @return 페이징 된 BoardList 를 가지고 있음 (단, 작성자 정보 포함) Page타입으로 반환변경
     * JOIN FETCH 때문에 Hibernate가 쿼리를 이상하게 작성하는 것을 막는 처리
     * --> select 절에 DISTINCT 를 사용하면 정확한 count를 가져 올 수 있음
     * countQuery란? 전체 게시글에 개수를 빠르게 가져오기 위해 사용함, 성능 문제(Hibernate 제공)
     */
    @Query(value = "SELECT b FROM Board b JOIN FETCH b.user ORDER BY b.createdAt DESC",
    countQuery = "SELECT COUNT(DISTINCT b) FROM Board b")
    Page<Board> findAllByWithUserOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 게시글 검색(제목 또는 내용, 페이징 포함) - 검색어 있을 때 사용
     * @param keyword
     * @param pageable
     * @return
     */
    @Query(value = "SELECT DISTINCT b FROM Board b JOIN FETCH b.user " +
            "WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY b.createdAt DESC",
            countQuery = "SELECT COUNT(DISTINCT b) FROM Board b " +
                    "WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "   OR LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Board> findByTitleContainingOrContentContaining(@Param("keyword") String keyword, Pageable pageable);

    // 게시글 ID로 조회 (작성자 정보 포함 - JOIN FETCH 사용)
    @Query("SELECT b FROM Board b JOIN FETCH b.user WHERE b.id = :id")
    Optional<Board> findByIdWithUser(@Param("id") Long id);
}
