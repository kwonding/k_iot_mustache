package org.example.demo_ssr_v0.board;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// DB -- CRUD
@RequiredArgsConstructor
@Repository // IoC
public class BoardPersistRepository {

    // DI
    private final EntityManager entityManager;

    @Transactional
    public Board save(Board board) {
        // 엔티티 매니저 자동으로 insert 쿼리 만들어서 던져줌
        entityManager.persist(board);

        return board;
    }

    // 게시글 전체 조회
    public List<Board> findAll() {

        return entityManager
                .createQuery("SELECT b FROM Board b ORDER BY b.createdAt DESC")
                .getResultList();
    }

    // 단건 조회
    public Board findById(Long id) {

        Board board = entityManager.find(Board.class, id);

        return board;
    }

    // 수정
    @Transactional
    public Board updateById(Long id, BoardRequest.UpdateDto reqDto) {
        Board board = entityManager.find(Board.class, id);

        if (board == null) {
            throw new IllegalArgumentException("수정할 게시글을 찾을 수 없습니다.");
        }

        board.update(reqDto);
//        하단의 세 줄 코드는 위의 한줄로 대체 가능
//        board.setTitle(req.getTitle());
//        board.setContent(req.getContent());
//        board.setUsername(req.getUsername());

        // 더티 체킹
        // 1. 개발자가 직접 update 쿼리를 작성 안해도 됨
        // 2. 변경된 필드만 자동으로 update 됨
        // 3. 영속성 컨텍스트가 엔티티 상태를 자동 관리함
        // 4. 1차 캐시의 엔티티 정보도 자동 갱신

        return board;
    }

    // 삭제
    @Transactional // Rollback 해야 할 수도 있으니까
    public void deleteById(Long id) {
        Board board = entityManager.find(Board.class, id);

        if (board == null) {
            throw new IllegalArgumentException("삭제할 게시글이 없습니다.");
        }

        entityManager.remove(board);
    }

}
