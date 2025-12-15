package org.example.demo_ssr_v0.board;

import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v0._core.errors.exception.Exception400;
import org.example.demo_ssr_v0._core.errors.exception.Exception403;
import org.example.demo_ssr_v0._core.errors.exception.Exception404;
import org.example.demo_ssr_v0.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    /**
     * 게시글 목록 조회
     * 트랜잭션
     *  - 읽기 전용 트랜잭션 - 성능 최적화
     * @return 게시글 목록 (생성ㅇ칠 기준으로 내림차순)
     */
    public List<Board> 게시글목록조회() {
        return boardRepository.findAllByOrderByCreatedAtDesc();
    }

    public Board 게시글상세조회(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception400("해당 게시글을 찾을 수 없습니다."));
    }

    // 1. 트랜잭션 처리
    // 2. repository 저장
    @Transactional
    public Board 게시글작성(BoardRequest.SaveDto saveDto, User sessionUser) {
        // DTO 에서 직접 new 해서 생성한 Board 객체 일 뿐 아직 영속화 된 객체는 아님
        Board board = saveDto.toEntity(sessionUser); // repository 에는 객체로 넣어줘야하니까
        return boardRepository.save(board);
    }

    // 1. 게시글 조회
    // 2. 인가 처리
    public Board 게시글수정화면(Long boardId, Long sessionUserId) {
        // 1
        Board boardEntity = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("해당 게시글을 찾을 수 없습니다."));

        // 2 인가 처리
        if (!boardEntity.isOwner(sessionUserId)) {
            throw new Exception403("게시글 수정 권한이 없습니다.");
        }
        return boardEntity;
    }

    // 1. 트랜잭션 처리(udpate)
    // 2. DB 에서 조회 - 더티체킹 쓸거니까
    // 3. 인가 처리
    // 4. 조회된 board 에 상태값 변경 (더티체킹)
    @Transactional
    public void 게시글수정(BoardRequest.UpdateDto updateDto, Long boardId, Long sessionUserId) {
        // 2 (조회부터 해야 DB에 있는 Board에 user_id 값을 확인 할 수 있음)
        Board boardEntity = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("해당 게시글을 찾을 수 없습니다."));

        // 3
        if (!boardEntity.isOwner(sessionUserId)) {
            throw new Exception403("게시글 수정 권한이 없습니다.");
        }

        // 4
        boardEntity.update(updateDto); // 상태값 변경
    }

    // 1. 트랜잭션 처리
    // 2. 게시글 조회
    // 3. 인가 처리
    // 4. repository 에게 삭제 요청
    @Transactional
    public void 게시글삭제(Long boardId, Long sessionUserId) {
        // 2 (조회부터 해야 DB에 있는 Board에 user_id 값을 확인 할 수 있음)
        Board boardEntity = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("해당 게시글을 찾을 수 없습니다."));

        // 3
        if (!boardEntity.isOwner(sessionUserId)) {
            throw new Exception403("게시글 삭제 권한이 없습니다.");
        }

        // 4
        boardRepository.deleteById(boardId);
    }


}
